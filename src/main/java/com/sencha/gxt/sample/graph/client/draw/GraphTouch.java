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

import java.util.logging.Logger;

import com.google.gwt.dom.client.NativeEvent;
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
  private class Handler  extends BaseEventPreview implements TouchStartHandler {
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
  private final Handler handler = new Handler();
  private HandlerRegistration handlerReg;
  private final GraphComponent<N, E> graph;

  private Integer activeTouch = null;
  private Point dragStartPosition;
  private Point lastDragPosition;

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
    if (dragStartPosition != null) {
      onCancel();

      dragStartPosition = null;
      handler.remove();
    }

    handlerReg.removeHandler();
    handlerReg = null;
  }

  protected void onTouchStart(TouchStartEvent event) {
    if (activeTouch != null) {
      //already dragging
      return;
    }
    assert event.getTouches().length() == 1;
    Touch t = event.getTouches().get(0);

    Point dragStartPosition = new Point(t.getRelativeX(graph.getElement()), t.getRelativeY(graph.getElement()));
    log.finer("Touch start: " + dragStartPosition.toString());

    boolean start = onStartDrag(dragStartPosition.getX(), dragStartPosition.getY());

    if (!start) {
      //not actually dragging, give up
      return;
    }
    this.activeTouch = t.getIdentifier();
    this.dragStartPosition = dragStartPosition;

    //watch for next move or up
    handler.add();

    //TODO fire an event about starting dragging
  }
  private Touch findRelevantTouch(NativeEvent event) {
    if (event.getTouches().length() == 1) {
      assert activeTouch == null || (activeTouch.intValue() == event.getTouches().get(0).getIdentifier());
      return event.getTouches().get(0);
    }
    for (int i = 0; i < event.getTouches().length(); i++) {
      if (event.getTouches().get(i).getIdentifier() == activeTouch.intValue()) {
        return event.getTouches().get(i);
      }
    }
    assert false;
    return null;
  }
  protected void onTouchMove(Event e) {
    Touch t = findRelevantTouch(e);
    boolean found = false;
    for (int i = 0; i < e.getChangedTouches().length(); i++) {
      if (e.getChangedTouches().get(i).getIdentifier() == t.getIdentifier()) {
        found = true;
        break;
      }
    }
    if (!found) {
      return;
    }
    assert dragStartPosition != null : "onMouseMove called while not actually dragging!";
    //TODO fire an event about the move
    int x = t.getClientX() - graph.getElement().getAbsoluteTop() + graph.getElement().getScrollTop() + graph.getElement().getOwnerDocument().getScrollTop();
    int y = t.getClientY() - graph.getElement().getAbsoluteLeft() + graph.getElement().getScrollLeft() + graph.getElement().getOwnerDocument().getScrollLeft();

    lastDragPosition = new Point(x, y);
    //TODO consider getting the offset from the original mouse point to the object
    //     and using that here
    onDrag(x, y);
    log.finer("Touch move: " + dragStartPosition.toString());

  }
  protected void onTouchEnd(Event e) {
    //		Touch t = findRelevantTouch(e);
    if (lastDragPosition == null) {
      return;
    }

    assert dragStartPosition != null : "onMouseUp called while not actually dragging!";
    //TODO fire an event about the release

    onDrop(lastDragPosition.getX(), lastDragPosition.getY());

    log.finer("Touch end: " + dragStartPosition.toString());
    dragStartPosition = null;
    lastDragPosition = null;
    handler.remove();
    activeTouch = null;

  }
  protected void onTouchCancel(Event e) {
    onTouchEnd(e);
  }

  //This api assumes exactly one dnd at a time
  protected abstract boolean onStartDrag(int x, int y);

  protected abstract void onDrag(int x, int y);

  protected abstract void onDrop(int x, int y);

  protected abstract void onCancel();
}
