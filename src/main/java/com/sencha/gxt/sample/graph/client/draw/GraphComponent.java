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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
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

/**
 * Simple graph widget, implemented using the gxt draw library with a simple force-directed layout. Rendering
 * of nodes and edges is delegated to {@link EdgeRenderer}s and {@link NodeRenderer}s, and the layout is animated
 * to run as the browser is able to. 
 *
 * @param <N>
 * @param <E>
 */
public class GraphComponent<N extends Node, E extends Edge> extends DrawComponent {
  private static final Logger log = Logger.getLogger(GraphComponent.class.getName());

  /**
   * Allows specifying a way to draw a Node object to be centered at the given coordinates.
   *
   * @param <N>
   */
  public interface NodeRenderer<N extends Node> {
    void render(N node, PrecisePoint coords, RenderContext context);
  }

  /**
   * Allows specifying a way to draw an Edge object between the two given coordinates.
   *
   * @param <E>
   */
  public interface EdgeRenderer<E extends Edge> {
    void render(E edge, PrecisePoint start, PrecisePoint end, RenderContext context);
  }

  /**
   * Simple API to let renderers draw and delete sprites, as well as get information about which
   * sprites are drawn for a given object.
   *
   */
  public interface RenderContext {
    void useSprite(Sprite s);
    void releaseSprite(Sprite s);
    List<Sprite> getSprites();
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

  private double[] locArray = new double[10];
  private double[] vecArray = new double[10];

  private Set<N> locked = new HashSet<N>();

  private List<E> edges = new ArrayList<E>();

  private AnimationHandle animationHandle;
  private AnimationCallback animationCallback = new AnimationCallback() {
    public void execute(double timestamp) {
      // Run the update to redraw
      update();

      //redraw
      redrawSurfaceForced();

      // Automatically re-schedule
      animationHandle = AnimationScheduler.get().requestAnimationFrame(this);
    }
  };

  private EdgeRenderer<E> edgeRenderer;
  private NodeRenderer<N> nodeRenderer;


  private double nodeDist = 50;

  private PrecisePoint lastPoint = new PrecisePoint(2 * nodeDist, 2 * nodeDist);

  private boolean animationEnabled = false;



  /**
   * Declares that a node should be rendered the next time the layout update is run. 
   * 
   * Each edge in that node must also be added, see {@link #addEdge(Edge)}.
   *
   * @param n the node to draw
   */
  public void addNode(N n) {
    nodes.add(n);

    locArray = grow(locArray, lastPoint.getX(), lastPoint.getY());
    vecArray = grow(vecArray, 0.0, 0.0);

    int offsetWidth = getOffsetWidth();
    int offsetHeight = getOffsetHeight();
    if (((offsetWidth != 0) && (lastPoint.getX() > offsetWidth))
            || ((offsetHeight != 0) && (lastPoint.getY() > offsetHeight))) {
      lastPoint.setX(2 * nodeDist + nodeDist * Math.random());
      lastPoint.setY(2 * nodeDist + nodeDist * Math.random());
    } else if (lastPoint.getY() <= 2 * nodeDist) {
      lastPoint.setY(lastPoint.getX() + 2 * nodeDist);
      lastPoint.setX(2 * nodeDist + nodeDist * Math.random());
    } else if (lastPoint.getY() <= lastPoint.getX()) {
      lastPoint.setY(lastPoint.getY() - 2 * nodeDist);
    } else {
      lastPoint.setX(lastPoint.getX() + 2 * nodeDist);
    }
  }

  private double[] grow(double[] array, double x, double y) {
    if (nodes.size() * 2 > array.length) {
      double[] newArray = new double[array.length * 2];
      System.arraycopy(array, 0, newArray, 0, array.length);
      array = newArray;
    }
    set(array, nodes.size() - 1, x, y);
    return array;
  }
  private void set(double[] array, int pos, double x, double y) {
    array[pos * 2] = x;
    array[pos * 2 + 1] = y;
  }
  private double getX(double[] array, int pos) {
    return array[pos * 2];
  }
  private double getY(double[] array, int pos) {
    return array[pos * 2 + 1];
  }

  /**
   * Declares that a node should be rendered the next time the layout update is run.
   * 
   * Each node attached to the edge must also be added, see {@link #addNode(Node)}.
   * 
   * @param edge the edge to draw
   */
  public void addEdge(E edge) {
    edges.add(edge);
  }

  public void removeNode(N node) {
    nodes.remove(node);
    //TODO splice out contents from vecArray, locArray
    locked.remove(node);
    nodeSprites.remove(node).clear();
    for (E edge : edges) {
      if (edge.getFrom().equals(node) || edge.getTo().equals(node)) {
        edges.remove(edge);
        edgeSprites.remove(edge).clear();
      }
    }
  }

  public void removeEdge(E edge) {
    removeNode((N) edge.getFrom());
    removeNode((N) edge.getTo());
  }

  public void clear() {
    edges.clear();
    locked.clear();
    nodes.clear();

    nodeSprites.clear();
    edgeSprites.clear();
    clearSurface();
  }

  /**
   * Enables or disables animations. Enabled by default - if disabled, the layout will not be run automatically, 
   * {@link #update()} must be invoked to perform the layout operation.
   * @param animationEnabled
   */
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

  /**
   * Sets the renderer to use to draw edges.
   * @see EdgeRenderContext
   * @see EdgeRenderer
   * @param edgeRenderer the instance to use when rendering any edge
   */
  public void setEdgeRenderer(EdgeRenderer<E> edgeRenderer) {
    this.edgeRenderer = edgeRenderer;
  }

  /**
   * Sets the renderer to use to draw nodes.
   * @see NodeRenderContext
   * @see NodeRenderer
   * @param nodeRenderer the instance to use when rendering any node
   */
  public void setNodeRenderer(NodeRenderer<N> nodeRenderer) {
    this.nodeRenderer = nodeRenderer;
  }

  /**
   * Sets the length of the 'spring' to use on edges.
   *
   * @param nodeDist the length of the 'spring' to use on edges
   */
  public void setNodeDist(double nodeDist) {
    this.nodeDist = nodeDist;
  }

  /**
   * Allows external code to specify the position of a given node. When executed, will set the velocity
   * of that node to zero.
   *
   * @param node the node to move
   * @param x the new x position in the widget
   * @param y the new y position in the widget
   */
  public void setCoords(N node, int x, int y) {
    int index = nodes.indexOf(node);
    set(locArray, index, x, y);
    set(vecArray, index, 0, 0);//zero out the item's vector
  }

  /**
   * Gets a node (if any) present at the given coordinates by checking each sprite associated with that
   * node and checking its bounding box. 
   * 
   * @param x
   * @param y
   * @return
   */
  public N getNodeAtCoords(int x, int y) {
    for (Map.Entry<N, SpriteList<Sprite>> entry : nodeSprites.entrySet()) {
      //TODO should not iterate over all possible sprites, just start with the ones that are at
      //least possible
      List<Sprite> sprites = entry.getValue();
      for (int i = 0; i < sprites.size(); i++) {
        if (sprites.get(i).getBBox().contains(x, y)) {
          return entry.getKey();
        }
      }
    }
    return null;
  }

  /**
   * Gets the current exact position of the given node
   *
   * @param node
   * @return
   */
  public PrecisePoint getNodeCoords(N node) {
    int index = nodes.indexOf(node);
    return new PrecisePoint(getX(locArray, index), getY(locArray, index));
  }

  /**
   * Gets the current velocity of the given node
   * @param node
   * @return
   */
  public PrecisePoint getNodeVector(N node) {
    int index = nodes.indexOf(node);
    return new PrecisePoint(getX(vecArray, index), getY(vecArray, index));
  }

  public List<N> getNodes() {
    return Collections.unmodifiableList(nodes);
  }

  public Set<N> getLocked() {
    return Collections.unmodifiableSet(locked);
  }

  /**
   * Prevents the current node from having its position or velocity updated.
   * 
   * @param node
   * @param isLocked
   */
  public void setNodeLocked(N node, boolean isLocked) {
    if (isLocked) {
      locked.add(node);
    } else {
      locked.remove(node);
    }
  }

  /**
   * Performs an update of the force-directed layout. Automatically run if animations are enabled.
   */
  public void update() {
    log.finest("Starting update.");
    //update forces acting on each node
    for (int i = 0; i < nodes.size(); i++) {
      N iNode = nodes.get(i);
      if (locked.contains(iNode)) {//no need to update a locked node
        continue;
      }
      double ix = getX(locArray, i), iy = getY(locArray, i);
      double ivx = getX(vecArray, i), ivy = getY(vecArray, i);
      if (log.isLoggable(Level.FINEST)) {
        PrecisePoint iLoc = getNodeCoords(iNode);
        PrecisePoint iVec = getNodeVector(iNode);
        log.finest("Updating at node " + iNode.getId() + "\n" +
            "    pos " + iLoc + "\n" +
            "    vec " + iVec);
      }

      //push away from every other node
      for (int j = 0; j < nodes.size(); j++) {
        if (i == j) {
          continue;
        }
        double jx = getX(locArray, j), jy = getY(locArray, j);

        double distance = distance(ix, iy, jx, jy);
        double force = REPULSE_CONST / (distance * distance);

        //i's change in bearing
        double bx = (ix - jx) / distance * force,
               by = (iy - jy) / distance * force;

        // need to re-assign to the local so that the next iter through j updates
        ivx += bx;
        ivy += by;
        set(vecArray, i, ivx, ivy);
      }
    }

    //pull toward all connected nodes
    for (int i = 0; i < edges.size(); i++) {
      E e = edges.get(i);
      Node to = e.getTo();
      int toIndex = nodes.indexOf(to);
      double toX = getX(locArray, toIndex), toY = getY(locArray, toIndex);
      double toVX = getX(vecArray, toIndex), toVY = getY(vecArray, toIndex);

      Node from = e.getFrom();
      int fromIndex = nodes.indexOf(from);
      double fromX = getX(locArray, fromIndex), fromY = getY(locArray, fromIndex);
      double fromVX = getX(vecArray, fromIndex), fromVY = getY(vecArray, fromIndex);

      double distance = distance(toX, toY, fromX, fromY);
      double force = ATTRACT_CONST * Math.max(distance - nodeDist, 0);

      //to's change in bearing
      double bx = (toX - fromX) / distance * force,
             by = (toY - fromY) / distance * force;


      if (!locked.contains(to)) {//if locked, don't update the node's velocity
        set(vecArray, toIndex, toVX - bx * force, toVY - by * force);
      }
      if (!locked.contains(from)) {//if locked, don't update the node's velocity
        set(vecArray, fromIndex, fromVX + bx * force, fromVY + by * force);
      }
    }

    // update position of each node based on current forces
    for (int i = 0; i < nodes.size(); i++) {
      N iNode = nodes.get(i);
      if (!locked.contains(iNode)) {//if locked, don't apply velocity to position
        //apply some friction (probably should be done earlier)
        double ivx = getX(vecArray, i) * (1 - FRICTION_CONST), ivy = getY(vecArray, i) * (1 - FRICTION_CONST);
        set(vecArray, i, ivx, ivy);

        //update position
        double ix = getX(locArray, i), iy = getY(locArray, i);
        set(locArray, i, ix + ivx, iy + ivy);

        if (log.isLoggable(Level.FINEST)) {
          log.finest("Moving " + iNode.getId() + "\n" +
              "    Pos " + getNodeCoords(iNode) + "\n" +
              "    Vec " + getNodeVector(iNode));
        }
        //TODO ensure two are not in the same place
      }

      nodeRenderer.render(iNode, getNodeCoords(iNode), new NodeRenderContext(iNode));
    }

    // update position of each edge based on current nodes
    for (int i = 0; i < edges.size(); i++) {
      E e = edges.get(i);
      PrecisePoint toLoc = getNodeCoords((N) e.getTo());
      PrecisePoint fromLoc = getNodeCoords((N) e.getFrom());
      edgeRenderer.render(e, toLoc, fromLoc, new EdgeRenderContext(e));
    }
  }

  private static double distance(double x1, double y1, double x2, double y2) {
    return Math.max(10, Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)));
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
