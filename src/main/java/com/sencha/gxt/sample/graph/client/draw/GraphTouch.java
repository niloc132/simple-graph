package com.sencha.gxt.sample.graph.client.draw;

/*
 * #%L
 * simple-graph
 * %%
 * Copyright (C) 2013 Sencha Inc
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.sencha.gxt.core.client.util.BaseEventPreview;
import com.sencha.gxt.core.client.util.Point;
import com.sencha.gxt.sample.graph.client.model.Edge;
import com.sencha.gxt.sample.graph.client.model.Node;

public abstract class GraphTouch<N extends Node, E extends Edge> {
  private static final Logger log = Logger.getLogger(GraphTouch.class.getName());
  private class TouchHandler extends BaseEventPreview implements TouchStartHandler {
    @Override
    public void onTouchStart(TouchStartEvent event) {
      GraphTouch.this.onTouchStart(event);
    }
    @Override
    protected boolean onPreview(NativePreviewEvent pe) {
      Event e = pe.getNativeEvent().<Event> cast();
      e.preventDefault();
      switch (pe.getTypeInt()) {
      case Event.ONTOUCHMOVE:
        onTouchMove(e);
        break;
      case Event.ONTOUCHEND:
        onTouchEnd(e);
        break;
      case Event.ONTOUCHCANCEL:
        onTouchCancel(e);
      }
      return true;
    }
  }
  private final TouchHandler handler = new TouchHandler();
  private HandlerRegistration handlerReg;
  private final GraphComponent<N, E> graph;

  private final Map<Integer, Point> dragStartPosition = new HashMap<Integer, Point>();
  private final Map<Integer, Point> lastDragPosition = new HashMap<Integer, Point>();

  public GraphTouch(GraphComponent<N,E> graph) {
    this.graph = graph;
    handler.setAutoHide(false);

    attach();
  }
  public GraphComponent<N, E> getGraph() {
    return graph;
  }
  public void attach() {
    assert handlerReg == null : "Already attached";

    handlerReg = this.graph.addDomHandler(handler, TouchStartEvent.getType());
  }

  public void release() {
    assert handlerReg != null : "Already released";
    //if dragging, cancel
    if (!dragStartPosition.isEmpty()) {
      onCancel();

      dragStartPosition.clear();
      lastDragPosition.clear();
      handler.remove();
    }

    handlerReg.removeHandler();
    handlerReg = null;
  }

  protected void onTouchStart(TouchStartEvent event) {
    JsArray<Touch> touches = event.getChangedTouches();

    for (int i = 0; i < touches.length(); i++) {
      Touch t = touches.get(i);
      Point dragStartPosition = new Point(t.getRelativeX(graph.getElement()), t.getRelativeY(graph.getElement()));
      log.finer("Touch start: " + t.getIdentifier() + " @ " + dragStartPosition.toString());

      boolean start = onStartDrag(t.getIdentifier(), dragStartPosition.getX(), dragStartPosition.getY());

      if (!start) {
        //not actually dragging, give up on this one
        continue;
      }
      this.dragStartPosition.put(t.getIdentifier(),dragStartPosition);

      //watch for next move or up
      handler.add();
      event.preventDefault();

      //TODO fire an event about starting dragging
    }
  }

  protected void onTouchMove(Event e) {
    JsArray<Touch> touches = e.getChangedTouches();

    for (int i = 0; i < touches.length(); i++) {
      Touch t = touches.get(i);
      if (!dragStartPosition.containsKey(t.getIdentifier())) {
        //not an active drag
        continue;
      }
      //TODO fire an event about the move
      int x = t.getClientX() - graph.getElement().getAbsoluteTop() + graph.getElement().getScrollTop() + graph.getElement().getOwnerDocument().getScrollTop();
      int y = t.getClientY() - graph.getElement().getAbsoluteLeft() + graph.getElement().getScrollLeft() + graph.getElement().getOwnerDocument().getScrollLeft();

      lastDragPosition.put(t.getIdentifier(), new Point(x, y));
      //TODO consider getting the offset from the original mouse point to the object
      //     and using that here
      onDrag(t.getIdentifier(), x, y);
      e.preventDefault();
      //      log.finer("Touch move: " + t.getIdentifier() + " @ " + lastDragPosition.get(t.getIdentifier()).toString());
    }

  }
  protected void onTouchEnd(Event e) {
    JsArray<Touch> touches = e.getTouches();

    List<Integer> identifiers = new ArrayList<Integer>(dragStartPosition.keySet());
    log.finer("ending, currently tracking: " + identifiers);
    for (int i = 0; i < touches.length(); i++) {
      identifiers.remove(touches.get(i).getIdentifier());
    }
    log.finer("now down to " + identifiers);
    for (Integer identifier : identifiers) {
      log.finer("[End] event for " + identifier);
      Point lastPosition = lastDragPosition.get(identifier);
      onDrop(identifier, lastPosition.getX(), lastPosition.getY());

      log.finer("Touch end: " + identifier + " @ " + lastPosition.toString());
      dragStartPosition.remove(identifier);
      lastDragPosition.remove(identifier);
    }

    if (dragStartPosition.isEmpty()) {
      assert lastDragPosition.isEmpty();
      handler.remove();
    }
  }
  protected void onTouchCancel(Event e) {
    onTouchEnd(e);
  }

  //This api allows for several simultaneous dnd
  protected abstract boolean onStartDrag(int index, int x, int y);

  protected abstract void onDrag(int index, int x, int y);

  protected abstract void onDrop(int index, int x, int y);

  protected abstract void onCancel();
}
