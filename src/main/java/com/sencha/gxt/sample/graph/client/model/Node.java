package com.sencha.gxt.sample.graph.client.model;

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


public class Node {
  private static int NEXT_ID = 0;

  //not final for rpc support
  private int id = NEXT_ID++;

  public int getId() {
    return id;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Node) {
      Node n = (Node) obj;
      return n.id == this.id;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return id;
  }
}
