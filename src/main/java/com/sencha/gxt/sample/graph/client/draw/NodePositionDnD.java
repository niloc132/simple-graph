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

import com.sencha.gxt.sample.graph.client.model.Edge;
import com.sencha.gxt.sample.graph.client.model.Node;

public class NodePositionDnD<N extends Node, E extends Edge> extends GraphDnD<N,E> {
  private Map<String, N> activeNodes = new HashMap<String, N>();

  public NodePositionDnD(GraphComponent<N, E> graph) {
    super(graph);
  }

  protected boolean onStartDrag(String key, int x, int y) {
    N activeNode = getGraph().getNodeAtCoords(x, y);
    if (activeNode != null) {
      activeNodes.put(key, activeNode);
      getGraph().setNodeLocked(activeNode, true);
      return true;
    }
    return false;
  }

  protected void onDrag(String key, int x, int y) {
    getGraph().setCoords(activeNodes.get(key), x, y);
  }

  protected void onDrop(String key, int x, int y) {
    getGraph().setNodeLocked(activeNodes.remove(key), false);
  }

  protected void onCancel() {
    for (String key : activeNodes.keySet()) {
      getGraph().setNodeLocked(activeNodes.remove(key), false);
    }
  }
}
