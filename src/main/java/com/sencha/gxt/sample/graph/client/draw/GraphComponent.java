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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.animation.client.AnimationScheduler.AnimationCallback;
import com.google.gwt.animation.client.AnimationScheduler.AnimationHandle;
import com.sencha.gxt.chart.client.draw.DrawComponent;
import com.sencha.gxt.chart.client.draw.RGB;
import com.sencha.gxt.chart.client.draw.path.LineTo;
import com.sencha.gxt.chart.client.draw.path.MoveTo;
import com.sencha.gxt.chart.client.draw.path.PathSprite;
import com.sencha.gxt.chart.client.draw.sprite.CircleSprite;
import com.sencha.gxt.chart.client.draw.sprite.Sprite;
import com.sencha.gxt.chart.client.draw.sprite.SpriteList;
import com.sencha.gxt.core.client.util.PrecisePoint;
import com.sencha.gxt.sample.graph.client.model.Edge;
import com.sencha.gxt.sample.graph.client.model.Node;

public class GraphComponent<N extends Node, E extends Edge> extends DrawComponent {
  private static final Logger log = Logger.getLogger(GraphComponent.class.getName());
  /*  public interface RenderContext {
    void useSprite(Sprite s);
    void releaseSprite(Sprite s);
  }
  public interface EdgeRenderer<E extends Edge> {
    void render(E edge, Point start, Point end, RenderContext context);
  }
  public interface NodeRenderer<N extends Node> {
    void render(N node, Point coords, RenderContext context);
  }

  private class NodeRenderContext implements RenderContext {
    private final N node;
    public NodeRenderContext(N node) {
      this.node = node;
    }
    public void useSprite(Sprite s) {
      nodeSprites.get(node).add(s);
    }
    public void releaseSprite(Sprite s) {
      nodeSprites.get(node).remove(s);
    }
  }
  private class EdgeRenderContext implements RenderContext {
    private final E node;
    public EdgeRenderContext(E node) {
      this.node = node;
    }
    public void useSprite(Sprite s) {
      edgeSprites.get(node).add(s);
    }
    public void releaseSprite(Sprite s) {
      edgeSprites.get(node).remove(s);
    }
  }*/
  private static final double REPULSE_CONST = 500.0;
  private static final double ATTRACT_CONST = 0.002;
  private static final double FRICTION_CONST = 0.03;

  private final Map<N, SpriteList<Sprite>> nodeSprites = new HashMap<N, SpriteList<Sprite>>();
  private final Map<E, SpriteList<Sprite>> edgeSprites = new HashMap<E, SpriteList<Sprite>>();
  private List<N> nodes = new ArrayList<N>();
  private Map<N, PrecisePoint> locations = new HashMap<N, PrecisePoint>();
  private Map<N, PrecisePoint> vectors = new HashMap<N, PrecisePoint>();

  private AnimationHandle animationHandle;
  private AnimationCallback animationCallback = new AnimationCallback() {
    public void execute(double timestamp) {
      // Run the update to redraw
      update();

      // Automatically re-schedule
      animationHandle = AnimationScheduler.get().requestAnimationFrame(this);
    }
  };

  private CircleSprite nodeTemplate = new CircleSprite();
  private PathSprite edgeTemplate = new PathSprite();
  private double nodeDist = 50;

  private double lastPos = 100;

  private boolean animationEnabled = false;

  public GraphComponent() {
    nodeTemplate.setRadius(6);
    nodeTemplate.setStroke(RGB.RED);
    nodeTemplate.setStrokeWidth(2);

    edgeTemplate.setStroke(RGB.BLUE);
  }

  public void addNode(N n) {
    locations.put(n, new PrecisePoint(lastPos+=nodeDist, lastPos+=nodeDist));//TODO better jitter
    vectors.put(n, new PrecisePoint(0,0));
    nodes.add(n);

    nodeSprites.put(n, new SpriteList<Sprite>());
    nodeSprites.get(n).add(new CircleSprite(nodeTemplate));
    this.addSprite(nodeSprites.get(n).get(0));

    for (Edge e : n.getEdges()) {
      edgeSprites.put((E) e, new SpriteList<Sprite>());
      PathSprite sprite = new PathSprite(edgeTemplate);
      sprite.addCommand(new MoveTo());
      sprite.addCommand(new LineTo());
      edgeSprites.get(e).add(sprite);
      addSprite(sprite);
    }
  }

  public void setAnimationEnabled(boolean animationEnabled) {
    if (animationEnabled != this.animationEnabled) {
      this.animationEnabled = animationEnabled;
      if (isAttached()) {
        if (animationEnabled) {
          animationHandle = AnimationScheduler.get().requestAnimationFrame(animationCallback);
        } else if (animationHandle != null) {//i.e. not already running, this check shouldn't be needed
          animationHandle.cancel();
        }
      }
    }
  }
  public boolean isAnimationEnabled() {
    return animationEnabled;
  }

  public void update() {
    log.fine("Starting update.");
    //update forces acting on each node
    for (int i = 0; i < nodes.size(); i++) {
      N iNode = nodes.get(i);
      PrecisePoint iLoc = locations.get(iNode);
      PrecisePoint iVec = vectors.get(iNode);
      log.fine("Updating at node " + iNode.getId());
      log.fine("    pos " + iLoc);
      log.fine("    vec " + iVec);

      //push away from every other node
      for (int j = 0; j < nodes.size(); j++) {
        if (i == j) {
          continue;
        }
        N jNode = nodes.get(j);
        PrecisePoint jLoc = locations.get(jNode);
        double distance = distance(iLoc, jLoc);
        PrecisePoint bearing = bearingUnit(iLoc, jLoc);

        double force = REPULSE_CONST / (distance * distance);

        iVec.setX(iVec.getX() + bearing.getX() * force);
        iVec.setY(iVec.getY() + bearing.getY() * force);
      }

      //pull toward all connected nodes
      for (Edge e : iNode.getEdges()) {
        Node other = e.getFrom();
        PrecisePoint otherLoc = locations.get(other);
        PrecisePoint otherVec = vectors.get(other);

        double distance = distance(iLoc, otherLoc);
        PrecisePoint bearing = bearingUnit(iLoc, otherLoc);//TODO cache this

        double force = ATTRACT_CONST * Math.max(distance - nodeDist, 0);

        iVec.setX(iVec.getX() - bearing.getX() * force);
        iVec.setY(iVec.getY() - bearing.getY() * force);
        otherVec.setX(otherVec.getX() + bearing.getX() * force);
        otherVec.setY(otherVec.getY() + bearing.getY() * force);
      }

    }

    //update position of each node based on current forces
    for (int i = 0; i < nodes.size(); i++) {
      N iNode = nodes.get(i);
      PrecisePoint iLoc = locations.get(iNode);
      PrecisePoint iVec = vectors.get(iNode);

      //apply friction
      iVec.setX(iVec.getX() * (1 - FRICTION_CONST));
      iVec.setY(iVec.getY() * (1 - FRICTION_CONST));

      //update position
      iLoc.setX(iLoc.getX() + iVec.getX());
      iLoc.setY(iLoc.getY() + iVec.getY());
      log.fine("Moving " + iNode.getId());
      log.fine("    Pos " + iLoc);
      log.fine("    Vec " + iVec);
      //TODO ensure two are not in the same place

      ((CircleSprite)nodeSprites.get(iNode).get(0)).setCenterX(iLoc.getX());
      ((CircleSprite)nodeSprites.get(iNode).get(0)).setCenterY(iLoc.getY());
    }
    for (int i = 0; i < nodes.size(); i++) {
      N iNode = nodes.get(i);
      PrecisePoint iLoc = locations.get(iNode);
      for (Edge e : iNode.getEdges()) {
        PrecisePoint otherLoc = locations.get(e.getFrom());
        PathSprite sprite = (PathSprite) edgeSprites.get(e).get(0);
        ((MoveTo)sprite.getCommand(0)).setX(iLoc.getX());
        ((MoveTo)sprite.getCommand(0)).setY(iLoc.getY());
        ((LineTo)sprite.getCommand(1)).setX(otherLoc.getX());
        ((LineTo)sprite.getCommand(1)).setY(otherLoc.getY());

        //mark sprite command as dirty
        sprite.setCommands(sprite.getCommands());
      }
    }


    //redraw
    redrawSurface();
  }

  private PrecisePoint bearingUnit(PrecisePoint iLoc, PrecisePoint jLoc) {
    double dist = distance(iLoc, jLoc);
    PrecisePoint bearing = new PrecisePoint((iLoc.getX() - jLoc.getX())/dist, (iLoc.getY() - jLoc.getY())/dist);
    return bearing;
  }

  private double distance(PrecisePoint iLoc, PrecisePoint jLoc) {
    return Math.sqrt(Math.pow(iLoc.getX() - jLoc.getX(), 2) + 
        Math.pow(iLoc.getY() - jLoc.getY(), 2));
  }

  @Override
  protected void onAttach() {
    super.onAttach();
    if (isAnimationEnabled()) {
      animationHandle = AnimationScheduler.get().requestAnimationFrame(animationCallback);
    }
  }

  @Override
  protected void onDetach() {
    super.onDetach();
    if (isAnimationEnabled() && animationHandle != null) {
      animationHandle.cancel();
    }
  }
}
