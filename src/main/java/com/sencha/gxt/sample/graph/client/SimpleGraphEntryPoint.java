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

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.sencha.gxt.sample.graph.client.draw.GraphComponent;
import com.sencha.gxt.sample.graph.client.model.Edge;
import com.sencha.gxt.sample.graph.client.model.Node;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.container.Viewport;

public class SimpleGraphEntryPoint implements EntryPoint {

  public void onModuleLoad() {
    Viewport vp = new Viewport();

    VerticalLayoutContainer vlc = new VerticalLayoutContainer();

    final GraphComponent<Node, Edge> graph = new GraphComponent<Node, Edge>();

    Node n1 = new Node();
    Node n2 = new Node();
    Node n3 = new Node();
    Node n4 = new Node();
    n1.linkTo(n2);
    n2.linkTo(n3);
    n3.linkTo(n1);
    n4.linkTo(n1);

    graph.addNode(n1);
    graph.addNode(n2);
    graph.addNode(n3);
    graph.addNode(n4);

    vlc.add(graph, new VerticalLayoutData(1,1));

    vp.setWidget(vlc);

    RootPanel.get().add(vp);
  }
}
