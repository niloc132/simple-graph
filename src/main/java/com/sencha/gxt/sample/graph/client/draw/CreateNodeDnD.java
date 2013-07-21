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

import java.util.HashMap;
import java.util.Map;

import com.sencha.gxt.chart.client.draw.RGB;
import com.sencha.gxt.chart.client.draw.path.LineTo;
import com.sencha.gxt.chart.client.draw.path.MoveTo;
import com.sencha.gxt.chart.client.draw.path.PathSprite;
import com.sencha.gxt.chart.client.draw.sprite.CircleSprite;
import com.sencha.gxt.core.client.util.PrecisePoint;
import com.sencha.gxt.sample.graph.client.model.Edge;
import com.sencha.gxt.sample.graph.client.model.Node;

public class CreateNodeDnD<N extends Node, E extends Edge> extends GraphDnD<N, E> {

  private final Map<String, N> newNodes = new HashMap<String, N>();
  private final Map<String, Boolean> oldIsAnimated = new HashMap<String, Boolean>();
  private final Map<String, CircleSprite> newNodesRendered = new HashMap<String, CircleSprite>();
  private final Map<String, PathSprite> newConnections = new HashMap<String, PathSprite>();

  public CreateNodeDnD(GraphComponent<N, E> graph) {
    super(graph);
  }

  @Override
  protected boolean onStartDrag(String key, int x, int y) {
    if (getGraph().getNodeAtCoords(x, y) != null) {
      return false;
    }
    newNodes.put(key, createNode(x, y));

    //TODO this tracking gets muuuch uglier with multiple concurrent moves
    oldIsAnimated.put(key, getGraph().isAnimationEnabled());
    getGraph().setAnimationEnabled(false);

    CircleSprite newNodeRendered = new CircleSprite();
    newNodeRendered.setCenterX(x);
    newNodeRendered.setCenterY(y);
    newNodeRendered.setRadius(3);
    newNodeRendered.setFill(RGB.RED);
    getGraph().addSprite(newNodeRendered);
    newNodeRendered.redraw();
    newNodesRendered.put(key, newNodeRendered);

    PathSprite newConnection = new PathSprite();
    newConnection.setStroke(RGB.RED);
    newConnection.addCommand(new MoveTo(x,y));
    getGraph().addSprite(newConnection);
    newConnection.redraw();
    newConnections.put(key, newConnection);

    return true;
  }

  @Override
  protected void onDrag(String key, int x, int y) {
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
    if (newConnections.get(key).getCommands().size() != 1) {
      assert newConnections.get(key).getCommands().size() == 2;
      newConnections.get(key).getCommands().remove(1);
    }
    newConnections.get(key).addCommand(line);
    newConnections.get(key).redraw();
  }

  @Override
  protected void onDrop(String key, int x, int y) {
    N endNode = getGraph().getNodeAtCoords(x, y);
    if (endNode != null) {
      E newEdge = createEdge(newNodes.get(key), endNode);

      getGraph().addNode(newNodes.get(key));
      getGraph().addEdge(newEdge);
      MoveTo move = (MoveTo) newConnections.get(key).getCommand(0);
      getGraph().setCoords(newNodes.get(key), (int) move.getX(), (int) move.getY());
    }

    getGraph().remove(newNodesRendered.get(key));
    newNodesRendered.remove(key);
    getGraph().remove(newConnections.get(key));
    newConnections.remove(key);

    newNodes.remove(key);
    getGraph().setAnimationEnabled(oldIsAnimated.remove(key));
  }

  @Override
  protected void onCancel() {
    for (String key : newNodesRendered.keySet()) {
      getGraph().remove(newNodesRendered.remove(key));
      getGraph().remove(newConnections.remove(key));

      //TODO particularly stupid
      getGraph().setAnimationEnabled(oldIsAnimated.get(key));
    }
    newNodes.clear();
  }

  protected N createNode(int x, int y) {
    return (N) new Node();
  }
  protected E createEdge(N startNode, N endNode) {
    return (E) new Edge(startNode, endNode);
  }

}
