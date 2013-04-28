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

import com.sencha.gxt.sample.graph.client.model.Edge;
import com.sencha.gxt.sample.graph.client.model.Node;

public class NodeConnectionDnD<N extends Node, E extends Edge> extends GraphDnD<N, E> {

  private N startNode;
  public NodeConnectionDnD(GraphComponent<N, E> graph) {
    super(graph);
  }

  @Override
  protected boolean onStartDrag(int x, int y) {
    startNode = getGraph().getNodeAtCoords(x, y);
    return startNode != null;
  }

  @Override
  protected void onDrag(int x, int y) {
    //TODO remove existing simple connect (and let it get animated...?)
    //TODO if over another node, draw a simple connect
  }

  @Override
  protected void onDrop(int x, int y) {
    //TODO remove existing simpleConnect
    N endNode = getGraph().getNodeAtCoords(x, y);
    if (endNode != null) {
      getGraph().addEdge(createEdge(startNode, endNode));
    }
    startNode = null;
  }

  protected E createEdge(N startNode, N endNode) {
    return (E) new Edge(startNode, endNode);
  }

  @Override
  protected void onCancel() {
    //TODO remove existing simple connect
    startNode = null;
  }

}