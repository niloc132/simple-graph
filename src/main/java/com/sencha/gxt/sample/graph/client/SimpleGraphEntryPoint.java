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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
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
import com.sencha.gxt.sample.graph.client.draw.NodeConnectionDnD;
import com.sencha.gxt.sample.graph.client.draw.NodePositionDnD;
import com.sencha.gxt.sample.graph.client.model.Edge;
import com.sencha.gxt.sample.graph.client.model.Node;
import com.sencha.gxt.widget.core.client.Slider;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.button.ToggleButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.container.Viewport;
import com.sencha.gxt.widget.core.client.event.CheckChangeEvent;
import com.sencha.gxt.widget.core.client.event.CheckChangeEvent.CheckChangeHandler;
import com.sencha.gxt.widget.core.client.menu.CheckMenuItem;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import com.sencha.gxt.widget.core.client.tree.Tree.CheckState;

public class SimpleGraphEntryPoint implements EntryPoint {

  public void onModuleLoad() {
    Viewport vp = new Viewport();

    VerticalLayoutContainer vlc = new VerticalLayoutContainer();

    final GraphComponent<Node, Edge> graph = new GraphComponent<Node, Edge>();
    graph.setNodeRenderer(new NodeRenderer<Node>() {
      public void render(Node node, PrecisePoint coords, RenderContext context) {
        CircleSprite circleSprite = (CircleSprite)context.getSprites().get(0);
        if (circleSprite == null) {
          circleSprite = new CircleSprite();
          circleSprite.setRadius(6);
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


    createStartingGraph(graph);

    vlc.add(graph, new VerticalLayoutData(1,1));

    ToolBar controls = new ToolBar();
    ToggleButton animateBtn = new ToggleButton("Animate");
    animateBtn.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        graph.setAnimationEnabled(event.getValue());
      }
    });
    animateBtn.setValue(true, true);
    controls.add(animateBtn);

    Slider nodeDistance = new Slider();
    nodeDistance.setMaxValue(200);
    nodeDistance.setIncrement(5);
    nodeDistance.setToolTip("Distance between nodes");
    nodeDistance.addValueChangeHandler(new ValueChangeHandler<Integer>() {
      @Override
      public void onValueChange(ValueChangeEvent<Integer> event) {
        graph.setNodeDist(event.getValue());
      }
    });
    nodeDistance.setValue(50, true);
    controls.add(nodeDistance);

    TextButton tool = new TextButton("Tool");
    tool.setMenu(new Menu());
    controls.add(tool);

    final NodePositionDnD<Node, Edge> dragBehavior = new NodePositionDnD<Node, Edge>(graph);
    dragBehavior.release();
    CheckMenuItem drag = new CheckMenuItem("Drag Node");
    drag.setGroup("tools");
    drag.addCheckChangeHandler(new CheckChangeHandler<CheckMenuItem>() {
      @Override
      public void onCheckChange(CheckChangeEvent<CheckMenuItem> event) {
        if (event.getChecked().equals(CheckState.CHECKED)) {
          dragBehavior.attach();
        } else {
          dragBehavior.release();
        }
      }
    });
    tool.getMenu().add(drag);

    final NodeConnectionDnD<Node, Edge> connectBehavior = new NodeConnectionDnD<Node, Edge>(graph);
    connectBehavior.release();
    CheckMenuItem connect = new CheckMenuItem("Connect Existing Nodes");
    connect.setGroup("tools");
    connect.addCheckChangeHandler(new CheckChangeHandler<CheckMenuItem>() {
      @Override
      public void onCheckChange(CheckChangeEvent<CheckMenuItem> event) {
        if (event.getChecked().equals(CheckState.CHECKED)) {
          connectBehavior.attach();
        } else {
          connectBehavior.release();
        }
      }
    });
    tool.getMenu().add(connect);

    final CreateNodeDnD<Node, Edge> createBehavior = new CreateNodeDnD<Node, Edge>(graph);
    createBehavior.release();
    CheckMenuItem create = new CheckMenuItem("Create and Connect Node");
    create.setGroup("tools");
    create.addCheckChangeHandler(new CheckChangeHandler<CheckMenuItem>() {
      @Override
      public void onCheckChange(CheckChangeEvent<CheckMenuItem> event) {
        if (event.getChecked().equals(CheckState.CHECKED)) {
          createBehavior.attach();
        } else {
          createBehavior.release();
        }
      }
    });
    tool.getMenu().add(create);

//    tool.getMenu().add(new CheckMenuItem("Pan graph"));


    vlc.add(controls, new VerticalLayoutData(1, -1));

    vp.setWidget(vlc);

    RootPanel.get().add(vp);
  }

  private void createStartingGraph(final GraphComponent<Node, Edge> graph) {
    Node n1 = new Node();
    Node n2 = new Node();
    Node n3 = new Node();
    Node n4 = new Node();
    Node n5 = new Node();
    Node n6 = new Node();
    Node n7 = new Node();
    Node n8 = new Node();
    Node n9 = new Node();
    Node n10 = new Node();
    Node n11 = new Node();

    graph.addNode(n1);
    graph.addNode(n2);
    graph.addNode(n3);
    graph.addNode(n4);
    graph.addNode(n5);
    graph.addNode(n6);
    graph.addNode(n7);
    graph.addNode(n8);
    graph.addNode(n9);
    graph.addNode(n10);
    graph.addNode(n11);

    graph.addEdge(new Edge(n1, n2));
    graph.addEdge(new Edge(n1, n3));
    graph.addEdge(new Edge(n2, n3));
    graph.addEdge(new Edge(n3, n4));
    graph.addEdge(new Edge(n1, n5));
    graph.addEdge(new Edge(n2, n6));
    graph.addEdge(new Edge(n3, n7));
    graph.addEdge(new Edge(n4, n8));
    graph.addEdge(new Edge(n4, n9));
    graph.addEdge(new Edge(n4, n10));
    graph.addEdge(new Edge(n4, n11));
  }
}
