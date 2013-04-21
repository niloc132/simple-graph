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
import com.sencha.gxt.core.client.util.BaseEventPreview;
import com.sencha.gxt.core.client.util.Point;
import com.sencha.gxt.sample.graph.client.model.Edge;
import com.sencha.gxt.sample.graph.client.model.Node;

/**
 * Simple dnd impl for the graph component, directly translating mouse movement when dragged to
 * node position.
 * 
 * @todo factor out actual behavior to events to allow changing behavior (move, re-link, pan, etc)
 * 
 * @param <N>
 * @param <E>
 */
public class GraphDnD<N extends Node, E extends Edge> {
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
  private final GraphComponent<N, E> graph;
  private Point start;
  private N activeNode;

  public GraphDnD(GraphComponent<N,E> graph) {
    this.graph = graph;
    this.graph.addDomHandler(handler, MouseDownEvent.getType());

    handler.setAutoHide(false);
  }
  protected void onMouseDown(MouseDownEvent event) {
    //watch for next move or up
    handler.add();

    start = new Point(event.getRelativeX(graph.getElement()), event.getRelativeY(graph.getElement()));

    activeNode = graph.getNodeAtCoords(start.getX(), start.getY());
  }

  protected void onMouseMove(Event event) {
    //TODO fire an event about the move
    int x = event.getClientX() - graph.getElement().getAbsoluteTop() + graph.getElement().getScrollTop() + graph.getElement().getOwnerDocument().getScrollTop();
    int y = event.getClientY() - graph.getElement().getAbsoluteLeft() + graph.getElement().getScrollLeft() + graph.getElement().getOwnerDocument().getScrollLeft();

    //TODO consider getting the offset from the original mouse point to the object
    //     and using that here
    graph.setCoords(activeNode, x, y);
  }

  protected void onMouseUp(Event event) {
    //TODO fire an event about the release

    start = null;
    handler.remove();
  }
}
