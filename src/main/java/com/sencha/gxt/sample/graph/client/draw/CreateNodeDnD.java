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
import com.sencha.gxt.chart.client.draw.sprite.CircleSprite;
import com.sencha.gxt.core.client.util.PrecisePoint;
import com.sencha.gxt.sample.graph.client.model.Edge;
import com.sencha.gxt.sample.graph.client.model.Node;

public class CreateNodeDnD<N extends Node, E extends Edge> extends GraphDnD<N, E> {

  private N newNode;
  private boolean oldIsAnimated;
  private CircleSprite newNodeRendered;
  private PathSprite newConnection;
  public CreateNodeDnD(GraphComponent<N, E> graph) {
    super(graph);
  }

  @Override
  protected boolean onStartDrag(int x, int y) {
    if (getGraph().getNodeAtCoords(x, y) != null) {
      return false;
    }
    newNode = createNode(x, y);

    oldIsAnimated = getGraph().isAnimationEnabled();
    getGraph().setAnimationEnabled(false);

    newNodeRendered = new CircleSprite();
    newNodeRendered.setCenterX(x);
    newNodeRendered.setCenterY(y);
    newNodeRendered.setRadius(3);
    newNodeRendered.setFill(RGB.RED);
    getGraph().addSprite(newNodeRendered);
    newNodeRendered.redraw();

    newConnection = new PathSprite();
    newConnection.setStroke(RGB.RED);
    newConnection.addCommand(new MoveTo(x,y));
    getGraph().addSprite(newConnection);
    newConnection.redraw();

    return true;
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
    N endNode = getGraph().getNodeAtCoords(x, y);
    if (endNode != null) {
      E newEdge = createEdge(newNode, endNode);
      
      getGraph().addNode(newNode);
      getGraph().addEdge(newEdge);
      getGraph().setCoords(newNode, getDragStartPosition().getX(), getDragStartPosition().getY());
    }

    getGraph().remove(newNodeRendered);
    newNodeRendered = null;
    getGraph().remove(newConnection);
    newConnection = null;

    newNode = null;
    getGraph().setAnimationEnabled(oldIsAnimated);
  }

  @Override
  protected void onCancel() {
    getGraph().remove(newNodeRendered);
    newNodeRendered = null;
    getGraph().remove(newConnection);
    newConnection = null;

    newNode = null;
    getGraph().setAnimationEnabled(oldIsAnimated);
  }

  protected N createNode(int x, int y) {
    return (N) new Node();
  }
  protected E createEdge(N startNode, N endNode) {
    return (E) new Edge(startNode, endNode);
  }

}
