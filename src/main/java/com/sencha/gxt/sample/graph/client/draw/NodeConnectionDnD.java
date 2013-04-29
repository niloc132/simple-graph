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

import com.sencha.gxt.chart.client.draw.RGB;
import com.sencha.gxt.chart.client.draw.path.LineTo;
import com.sencha.gxt.chart.client.draw.path.MoveTo;
import com.sencha.gxt.chart.client.draw.path.PathSprite;
import com.sencha.gxt.core.client.util.PrecisePoint;
import com.sencha.gxt.sample.graph.client.model.Edge;
import com.sencha.gxt.sample.graph.client.model.Node;

public class NodeConnectionDnD<N extends Node, E extends Edge> extends GraphDnD<N, E> {

  private N startNode;
  private boolean oldIsAnimated;
  private PathSprite newConnection;
  public NodeConnectionDnD(GraphComponent<N, E> graph) {
    super(graph);
  }

  @Override
  protected boolean onStartDrag(int x, int y) {
    startNode = getGraph().getNodeAtCoords(x, y);
    if (startNode != null) {
      //stop animations for easier usability (consider replacing with locking nodes that we get near?)
      oldIsAnimated = getGraph().isAnimationEnabled();
      getGraph().setAnimationEnabled(false);

      newConnection = new PathSprite();
      newConnection.setStroke(RGB.RED);
      PrecisePoint actual = getGraph().getNodeCoords(startNode);
      newConnection.addCommand(new MoveTo(actual.getX(), actual.getY()));
      getGraph().addSprite(newConnection);
      newConnection.redraw();
      return true;
    }
    return false;
  }

  @Override
  protected void onDrag(int x, int y) {
    N endNode = getGraph().getNodeAtCoords(x, y);
    final LineTo line;
    if (endNode != null) {
      // lock to node if it exists
      PrecisePoint actual = getGraph().getNodeCoords(endNode);
      line = new LineTo(actual.getX(), actual.getY());
    } else {
      // just follow mouse if not
      line = new LineTo(x,y);
    }
    if (newConnection.getCommands().size() != 1) {
      assert newConnection.getCommands().size() == 2;
      newConnection.getCommands().remove(1);
    }
    newConnection.addCommand(line);
    newConnection.redraw();
  }

  @Override
  protected void onDrop(int x, int y) {
    getGraph().remove(newConnection);
    newConnection = null;

    N endNode = getGraph().getNodeAtCoords(x, y);
    if (endNode != null) {
      getGraph().addEdge(createEdge(startNode, endNode));
    }
    startNode = null;
    getGraph().setAnimationEnabled(oldIsAnimated);
  }

  protected E createEdge(N startNode, N endNode) {
    return (E) new Edge(startNode, endNode);
  }

  @Override
  protected void onCancel() {
    getGraph().remove(newConnection);
    newConnection = null;

    startNode = null;
    getGraph().setAnimationEnabled(oldIsAnimated);
  }

}
