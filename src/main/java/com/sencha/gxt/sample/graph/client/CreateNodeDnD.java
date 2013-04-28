package com.sencha.gxt.sample.graph.client;

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

import com.sencha.gxt.sample.graph.client.draw.GraphComponent;
import com.sencha.gxt.sample.graph.client.draw.GraphDnD;
import com.sencha.gxt.sample.graph.client.model.Edge;
import com.sencha.gxt.sample.graph.client.model.Node;

public class CreateNodeDnD<N extends Node, E extends Edge> extends GraphDnD<N, E> {

  private N newNode;
  public CreateNodeDnD(GraphComponent<N, E> graph) {
    super(graph);
  }

  @Override
  protected boolean onStartDrag(int x, int y) {
    if (getGraph().getNodeAtCoords(x, y) != null) {
      return false;
    }
    newNode = createNode(x, y);
    return true;
  }

  @Override
  protected void onDrag(int x, int y) {
    //no-op
  }

  @Override
  protected void onDrop(int x, int y) {
    N endNode = getGraph().getNodeAtCoords(x, y);
    if (endNode == null) {
      return;
    }
    E newEdge = createEdge(newNode, endNode);
    
    getGraph().addNode(newNode);
    getGraph().addEdge(newEdge);
    getGraph().setCoords(newNode, getDragStartPosition().getX(), getDragStartPosition().getY());
  }

  @Override
  protected void onCancel() {
    newNode = null;
  }
  
  protected N createNode(int x, int y) {
    return (N) new Node();
  }
  protected E createEdge(N startNode, N endNode) {
    return (E) new Edge(startNode, endNode);
  }

}
