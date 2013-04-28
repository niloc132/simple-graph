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

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
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
  private class Handler extends BaseEventPreview implements MouseDownHandler {
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

  private final Handler handler = new Handler();
  private HandlerRegistration handlerReg;
  private final GraphComponent<N, E> graph;
  private Point dragStartPosition;

  public GraphDnD(GraphComponent<N,E> graph) {
    this.graph = graph;
    handler.setAutoHide(false);

    attach();
  }

  public GraphComponent<N, E> getGraph() {
    return graph;
  }

  public Point getDragStartPosition() {
    return dragStartPosition;
  }

  public void attach() {
    assert handlerReg == null : "Already attached";

    handlerReg = this.graph.addDomHandler(handler, MouseDownEvent.getType());
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
  protected void onMouseDown(MouseDownEvent event) {
    event.preventDefault();

    dragStartPosition = new Point(event.getRelativeX(graph.getElement()), event.getRelativeY(graph.getElement()));

    boolean start = onStartDrag(dragStartPosition.getX(), dragStartPosition.getY());

    if (!start) {
      //not actually dragging, give up
      return;
    }

    //watch for next move or up
    handler.add();

    //TODO fire an event about starting dragging
  }

  protected void onMouseMove(Event event) {
    assert dragStartPosition != null : "onMouseMove called while not actually dragging!";
    //TODO fire an event about the move
    int x = event.getClientX() - graph.getElement().getAbsoluteTop() + graph.getElement().getScrollTop() + graph.getElement().getOwnerDocument().getScrollTop();
    int y = event.getClientY() - graph.getElement().getAbsoluteLeft() + graph.getElement().getScrollLeft() + graph.getElement().getOwnerDocument().getScrollLeft();

    //TODO consider getting the offset from the original mouse point to the object
    //     and using that here
    onDrag(x, y);
  }

  protected void onMouseUp(Event event) {
    assert dragStartPosition != null : "onMouseUp called while not actually dragging!";
    //TODO fire an event about the release
    int x = event.getClientX() - graph.getElement().getAbsoluteTop() + graph.getElement().getScrollTop() + graph.getElement().getOwnerDocument().getScrollTop();
    int y = event.getClientY() - graph.getElement().getAbsoluteLeft() + graph.getElement().getScrollLeft() + graph.getElement().getOwnerDocument().getScrollLeft();

    onDrop(x,y);

    dragStartPosition = null;
    handler.remove();
  }
  protected abstract boolean onStartDrag(int x, int y);

  protected abstract void onDrag(int x, int y);

  protected abstract void onDrop(int x, int y);

  protected abstract void onCancel();
}
