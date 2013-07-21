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
import com.sencha.gxt.core.client.util.PrecisePoint;
import com.sencha.gxt.sample.graph.client.model.Edge;
import com.sencha.gxt.sample.graph.client.model.Node;

public class NodeConnectionDnD<N extends Node, E extends Edge> extends GraphDnD<N, E> {

  private Map<String, N> startNodes = new HashMap<String, N>();
  private Map<String, Boolean> oldIsAnimated = new HashMap<String, Boolean>();
  private Map<String, PathSprite> newConnections = new HashMap<String, PathSprite>();

  public NodeConnectionDnD(GraphComponent<N, E> graph) {
    super(graph);
  }

  @Override
  protected boolean onStartDrag(String key, int x, int y) {
    N startNode = getGraph().getNodeAtCoords(x, y);
    if (startNode != null) {
      startNodes.put(key, startNode);
      //stop animations for easier usability (consider replacing with locking nodes that we get near?)
      //TODO this tracking gets muuuch uglier with multiple concurrent moves
      oldIsAnimated.put(key, getGraph().isAnimationEnabled());
      getGraph().setAnimationEnabled(false);

      PathSprite newConnection = new PathSprite();
      newConnection.setStroke(RGB.RED);
      PrecisePoint actual = getGraph().getNodeCoords(startNode);
      newConnection.addCommand(new MoveTo(actual.getX(), actual.getY()));
      getGraph().addSprite(newConnection);
      newConnection.redraw();
      newConnections.put(key, newConnection);
      return true;
    }
    return false;
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
    getGraph().remove(newConnections.remove(key));

    N endNode = getGraph().getNodeAtCoords(x, y);
    if (endNode != null) {
      getGraph().addEdge(createEdge(startNodes.get(key), endNode));
    }
    startNodes.remove(key);
    //TODO dumb
    getGraph().setAnimationEnabled(oldIsAnimated.get(key));
  }

  protected E createEdge(N startNode, N endNode) {
    return (E) new Edge(startNode, endNode);
  }

  @Override
  protected void onCancel() {
    for (String key : newConnections.keySet()) {
      getGraph().remove(newConnections.remove(key));
      //TODO dumb
      getGraph().setAnimationEnabled(oldIsAnimated.get(key));
    }

    startNodes.clear();
  }

}
