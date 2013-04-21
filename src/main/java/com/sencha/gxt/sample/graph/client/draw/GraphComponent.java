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
import com.sencha.gxt.chart.client.draw.sprite.Sprite;
import com.sencha.gxt.chart.client.draw.sprite.SpriteList;
import com.sencha.gxt.core.client.util.PrecisePoint;
import com.sencha.gxt.sample.graph.client.model.Edge;
import com.sencha.gxt.sample.graph.client.model.Node;

public class GraphComponent<N extends Node, E extends Edge> extends DrawComponent {
  private static final Logger log = Logger.getLogger(GraphComponent.class.getName());
  public interface RenderContext {
    void useSprite(Sprite s);
    void releaseSprite(Sprite s);
    List<Sprite> getSprites();
  }
  public interface EdgeRenderer<E extends Edge> {
    void render(E edge, PrecisePoint start, PrecisePoint end, RenderContext context);
  }
  public interface NodeRenderer<N extends Node> {
    void render(N node, PrecisePoint coords, RenderContext context);
  }

  private class NodeRenderContext implements RenderContext {
    private final N node;
    public NodeRenderContext(N node) {
      this.node = node;
    }
    public void useSprite(Sprite s) {
      addSprite(s);
      nodeSprites.get(node).add(s);
    }
    public void releaseSprite(Sprite s) {
      remove(s);
      nodeSprites.get(node).remove(s);
    }
    public List<Sprite> getSprites() {
      SpriteList<Sprite> spriteList = nodeSprites.get(node);
      if (spriteList == null) {
        spriteList = new SpriteList<Sprite>();
        nodeSprites.put(node, spriteList);
      }
      return spriteList;
    }
  }
  private class EdgeRenderContext implements RenderContext {
    private final E edge;
    public EdgeRenderContext(E node) {
      this.edge = node;
    }
    public void useSprite(Sprite s) {
      addSprite(s);
      edgeSprites.get(edge).add(s);
    }
    public void releaseSprite(Sprite s) {
      remove(s);
      edgeSprites.get(edge).remove(s);
    }
    public List<Sprite> getSprites() {
      SpriteList<Sprite> spriteList = edgeSprites.get(edge);
      if (spriteList == null) {
        spriteList = new SpriteList<Sprite>();
        edgeSprites.put(edge, spriteList);
      }
      return spriteList;
    }
  }
  private static final double REPULSE_CONST = 500.0;
  private static final double ATTRACT_CONST = 0.002;
  private static final double FRICTION_CONST = 0.03;

  private final Map<N, SpriteList<Sprite>> nodeSprites = new HashMap<N, SpriteList<Sprite>>();
  private final Map<E, SpriteList<Sprite>> edgeSprites = new HashMap<E, SpriteList<Sprite>>();
  private List<N> nodes = new ArrayList<N>();
  private Map<N, PrecisePoint> locations = new HashMap<N, PrecisePoint>();
  private Map<N, PrecisePoint> vectors = new HashMap<N, PrecisePoint>();
  
  private List<E> edges = new ArrayList<E>();

  private AnimationHandle animationHandle;
  private AnimationCallback animationCallback = new AnimationCallback() {
    public void execute(double timestamp) {
      // Run the update to redraw
      update();

      // Automatically re-schedule
      animationHandle = AnimationScheduler.get().requestAnimationFrame(this);
    }
  };

  private EdgeRenderer<E> edgeRenderer;
  private NodeRenderer<N> nodeRenderer;


  private double nodeDist = 50;

  private double lastPos = 100;

  private boolean animationEnabled = false;



  public void addNode(N n) {
    locations.put(n, new PrecisePoint(lastPos+=nodeDist, lastPos+=nodeDist));//TODO better jitter
    vectors.put(n, new PrecisePoint(0,0));
    nodes.add(n);
  }
  public void addEdge(E edge) {
    edges.add(edge);
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

  public void setEdgeRenderer(EdgeRenderer<E> edgeRenderer) {
    this.edgeRenderer = edgeRenderer;
  }
  public void setNodeRenderer(NodeRenderer<N> nodeRenderer) {
    this.nodeRenderer = nodeRenderer;
  }
  public void setNodeDist(double nodeDist) {
    this.nodeDist = nodeDist;
  }

  public void setCoords(N node, int x, int y) {
    locations.put(node, new PrecisePoint(x,y));
    vectors.put(node, new PrecisePoint());//zero out the item's vector
  }

  public N getNodeAtCoords(int x, int y) {
    for (Map.Entry<N, SpriteList<Sprite>> entry : nodeSprites.entrySet()) {
      //TODO should not iterate over all possible sprites, just start with the ones that are at
      //least possible
      for (Sprite s : entry.getValue()) {
        if (s.getBBox().contains(x, y)) {
          return entry.getKey();
        }
      }
    }
    return null;
  }

  public void update() {
    log.finest("Starting update.");
    //update forces acting on each node
    for (int i = 0; i < nodes.size(); i++) {
      N iNode = nodes.get(i);
      PrecisePoint iLoc = locations.get(iNode);
      PrecisePoint iVec = vectors.get(iNode);
      log.finest("Updating at node " + iNode.getId());
      log.finest("    pos " + iLoc);
      log.finest("    vec " + iVec);

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


    }
    //pull toward all connected nodes
    for (E e : edges) {
      Node to = e.getTo();
      PrecisePoint toLoc = locations.get(to);
      PrecisePoint toVec = vectors.get(to);
      Node from = e.getFrom();
      PrecisePoint fromLoc = locations.get(from);
      PrecisePoint fromVec = vectors.get(from);

      double distance = distance(toLoc, fromLoc);
      PrecisePoint bearing = bearingUnit(toLoc, fromLoc);//TODO cache this

      double force = ATTRACT_CONST * Math.max(distance - nodeDist, 0);

      toVec.setX(toVec.getX() - bearing.getX() * force);
      toVec.setY(toVec.getY() - bearing.getY() * force);
      fromVec.setX(fromVec.getX() + bearing.getX() * force);
      fromVec.setY(fromVec.getY() + bearing.getY() * force);
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
      log.finest("Moving " + iNode.getId());
      log.finest("    Pos " + iLoc);
      log.finest("    Vec " + iVec);
      //TODO ensure two are not in the same place

      nodeRenderer.render(iNode, iLoc, new NodeRenderContext(iNode));
    }

    for (E e : edges) {
      PrecisePoint toLoc = locations.get(e.getTo());
      PrecisePoint fromLoc = locations.get(e.getFrom());
      edgeRenderer.render((E) e, toLoc, fromLoc, new EdgeRenderContext((E) e));
      
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
