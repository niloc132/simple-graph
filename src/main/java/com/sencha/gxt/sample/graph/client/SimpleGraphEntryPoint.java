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
import com.sencha.gxt.chart.client.draw.RGB;
import com.sencha.gxt.chart.client.draw.path.LineTo;
import com.sencha.gxt.chart.client.draw.path.MoveTo;
import com.sencha.gxt.chart.client.draw.path.PathSprite;
import com.sencha.gxt.chart.client.draw.sprite.CircleSprite;
import com.sencha.gxt.core.client.util.PrecisePoint;
import com.sencha.gxt.sample.graph.client.draw.GraphComponent;
import com.sencha.gxt.sample.graph.client.draw.GraphComponent.EdgeRenderer;
import com.sencha.gxt.sample.graph.client.draw.GraphComponent.NodeRenderer;
import com.sencha.gxt.sample.graph.client.draw.GraphComponent.RenderContext;
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
    graph.setAnimationEnabled(true);
    graph.setNodeRenderer(new NodeRenderer<Node>() {
      public void render(Node node, PrecisePoint coords, RenderContext context) {
        CircleSprite circleSprite = (CircleSprite)context.getSprites().get(0);
        if (circleSprite == null) {
          circleSprite = new CircleSprite();
          circleSprite.setRadius(3);
          context.useSprite(circleSprite);
        }
        circleSprite.setCenterX(coords.getX());
        circleSprite.setCenterY(coords.getY());
      }
    });
    graph.setEdgeRenderer(new EdgeRenderer<Edge>() {
      public void render(Edge edge, PrecisePoint start, PrecisePoint end, RenderContext context) {
        PathSprite sprite = (PathSprite) context.getSprites().get(0);
        if (sprite == null) {
          sprite = new PathSprite();
          sprite.setStroke(RGB.BLUE);
          sprite.addCommand(new MoveTo());
          sprite.addCommand(new LineTo());
          context.useSprite(sprite);
        }
        ((MoveTo)sprite.getCommand(0)).setX(start.getX());
        ((MoveTo)sprite.getCommand(0)).setY(start.getY());
        ((LineTo)sprite.getCommand(1)).setX(end.getX());
        ((LineTo)sprite.getCommand(1)).setY(end.getY());

        //mark sprite command as dirty
        sprite.setCommands(sprite.getCommands());
      }
    });

    Node n1 = new Node();
    Node n2 = new Node();
    Node n3 = new Node();
    Node n4 = new Node();

    graph.addNode(n1);
    graph.addNode(n2);
    graph.addNode(n3);
    graph.addNode(n4);

    graph.addEdge(new Edge(n1, n2));
    graph.addEdge(new Edge(n1, n3));
    graph.addEdge(new Edge(n2, n3));
    graph.addEdge(new Edge(n3, n4));

    vlc.add(graph, new VerticalLayoutData(1,1));

    vp.setWidget(vlc);

    RootPanel.get().add(vp);
  }
}
