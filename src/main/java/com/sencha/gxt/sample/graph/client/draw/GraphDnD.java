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
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.sencha.gxt.core.client.util.BaseEventPreview;
import com.sencha.gxt.core.client.util.Point;
import com.sencha.gxt.sample.graph.client.model.Edge;
import com.sencha.gxt.sample.graph.client.model.Node;

/**
 * Simple abstract class containing dnd wiring, with abstract methods to implement various drag/drop
 * behaviors on the GraphComponent
 * 
 * @param <N>
 * @param <E>
 */
public abstract class GraphDnD<N extends Node, E extends Edge> {
  private static final Logger log = Logger.getLogger(GraphDnD.class.getName());
  private class MouseHandler extends BaseEventPreview implements MouseDownHandler {
    @Override
    public void onMouseDown(MouseDownEvent event) {
      GraphDnD.this.onMouseDown(event);
    }
    @Override
    protected boolean onPreview(NativePreviewEvent pe) {
      Event e = pe.getNativeEvent().<Event> cast();
      e.preventDefault();
      switch (pe.getTypeInt()) {
      case Event.ONMOUSEMOVE:
        onMouseMove(e);
        break;
      case Event.ONMOUSEUP:
        onMouseUp(e);
        break;
      }
      return true;
    }
  }
  private class TouchHandler extends BaseEventPreview implements TouchStartHandler {
    @Override
    public void onTouchStart(TouchStartEvent event) {
      GraphDnD.this.onTouchStart(event);
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
  private final GraphComponent<N, E> graph;

  private final MouseHandler mouseHandler = new MouseHandler();
  private HandlerRegistration mouseHandlerReg;

  private final TouchHandler touchHandler = new TouchHandler();
  private HandlerRegistration touchHandlerReg;

  private Point mouseDragStartPosition;

  private final Map<Integer, Point> touchDragStartPositions = new HashMap<Integer, Point>();
  private final Map<Integer, Point> touchLastDragPositions = new HashMap<Integer, Point>();

  public GraphDnD(GraphComponent<N,E> graph) {
    this.graph = graph;
    mouseHandler.setAutoHide(false);
    touchHandler.setAutoHide(false);

    attach();
  }

  public GraphComponent<N, E> getGraph() {
    return graph;
  }

  public void attach() {
    assert mouseHandlerReg == null && touchHandlerReg == null : "Already attached";

    mouseHandlerReg = this.graph.addDomHandler(mouseHandler, MouseDownEvent.getType());
    touchHandlerReg = this.graph.addDomHandler(touchHandler, TouchStartEvent.getType());
  }

  public void release() {
    assert mouseHandlerReg != null && touchHandlerReg != null : "Already released";

    //if dragging, cancel
    if (mouseDragStartPosition != null) {
      onCancel();

      mouseDragStartPosition = null;
      mouseHandler.remove();
    }
    if (!touchDragStartPositions.isEmpty()) {
      onCancel();

      touchDragStartPositions.clear();
      touchLastDragPositions.clear();
      touchHandler.remove();
    }

    mouseHandlerReg.removeHandler();
    mouseHandlerReg = null;

    touchHandlerReg.removeHandler();
    touchHandlerReg = null;
  }
  protected void onMouseDown(MouseDownEvent event) {
    event.preventDefault();

    mouseDragStartPosition = new Point(event.getRelativeX(graph.getElement()), event.getRelativeY(graph.getElement()));

    boolean start = onStartDrag("mouse", mouseDragStartPosition.getX(), mouseDragStartPosition.getY());

    if (!start) {
      //not actually dragging, give up
      return;
    }

    //watch for next move or up
    mouseHandler.add();

    //TODO fire an event about starting dragging
  }

  protected void onMouseMove(Event event) {
    assert mouseDragStartPosition != null : "onMouseMove called while not actually dragging!";
    //TODO fire an event about the move
    int x = event.getClientX() - graph.getElement().getAbsoluteTop() + graph.getElement().getScrollTop() + graph.getElement().getOwnerDocument().getScrollTop();
    int y = event.getClientY() - graph.getElement().getAbsoluteLeft() + graph.getElement().getScrollLeft() + graph.getElement().getOwnerDocument().getScrollLeft();

    //TODO consider getting the offset from the original mouse point to the object
    //     and using that here
    onDrag("mouse", x, y);
  }

  protected void onMouseUp(Event event) {
    assert mouseDragStartPosition != null : "onMouseUp called while not actually dragging!";
    //TODO fire an event about the release
    int x = event.getClientX() - graph.getElement().getAbsoluteTop() + graph.getElement().getScrollTop() + graph.getElement().getOwnerDocument().getScrollTop();
    int y = event.getClientY() - graph.getElement().getAbsoluteLeft() + graph.getElement().getScrollLeft() + graph.getElement().getOwnerDocument().getScrollLeft();

    onDrop("mouse", x, y);

    mouseDragStartPosition = null;
    mouseHandler.remove();
  }

  protected void onTouchStart(TouchStartEvent event) {
    JsArray<Touch> touches = event.getChangedTouches();

    for (int i = 0; i < touches.length(); i++) {
      Touch t = touches.get(i);
      Point dragStartPosition = new Point(t.getRelativeX(graph.getElement()), t.getRelativeY(graph.getElement()));
      log.finer("Touch start: " + t.getIdentifier() + " @ " + dragStartPosition.toString());

      boolean start = onStartDrag("touch" + t.getIdentifier(), dragStartPosition.getX(), dragStartPosition.getY());

      if (!start) {
        //not actually dragging, give up on this one
        continue;
      }
      this.touchDragStartPositions.put(t.getIdentifier(),dragStartPosition);

      //watch for next move or up
      touchHandler.add();
      event.preventDefault();

      //TODO fire an event about starting dragging
    }
  }

  protected void onTouchMove(Event e) {
    JsArray<Touch> touches = e.getChangedTouches();

    for (int i = 0; i < touches.length(); i++) {
      Touch t = touches.get(i);
      if (!touchDragStartPositions.containsKey(t.getIdentifier())) {
        //not an active drag
        continue;
      }
      //TODO fire an event about the move
      int x = t.getClientX() - graph.getElement().getAbsoluteTop() + graph.getElement().getScrollTop() + graph.getElement().getOwnerDocument().getScrollTop();
      int y = t.getClientY() - graph.getElement().getAbsoluteLeft() + graph.getElement().getScrollLeft() + graph.getElement().getOwnerDocument().getScrollLeft();

      touchLastDragPositions.put(t.getIdentifier(), new Point(x, y));
      //TODO consider getting the offset from the original mouse point to the object
      //     and using that here
      onDrag("touch" + t.getIdentifier(), x, y);
      e.preventDefault();
      //      log.finer("Touch move: " + t.getIdentifier() + " @ " + lastDragPosition.get(t.getIdentifier()).toString());
    }

  }
  protected void onTouchEnd(Event e) {
    JsArray<Touch> touches = e.getTouches();

    Set<Integer> identifiers = new HashSet<Integer>(touchDragStartPositions.keySet());
    log.finer("ending, currently tracking: " + identifiers);
    for (int i = 0; i < touches.length(); i++) {
      identifiers.remove(touches.get(i).getIdentifier());
    }
    log.finer("now down to " + identifiers);
    for (Integer identifier : identifiers) {
      log.finer("[End] event for " + identifier);
      Point lastPosition = touchLastDragPositions.get(identifier);
      onDrop("touch" + identifier, lastPosition.getX(), lastPosition.getY());

      log.finer("Touch end: " + identifier + " @ " + lastPosition.toString());
      touchDragStartPositions.remove(identifier);
      touchLastDragPositions.remove(identifier);
    }

    if (touchDragStartPositions.isEmpty()) {
      assert touchLastDragPositions.isEmpty();
      touchHandler.remove();
    }
  }
  protected void onTouchCancel(Event e) {
    onTouchEnd(e);
  }

  protected abstract boolean onStartDrag(String key, int x, int y);

  protected abstract void onDrag(String key, int x, int y);

  protected abstract void onDrop(String key, int x, int y);

  protected abstract void onCancel();
}
