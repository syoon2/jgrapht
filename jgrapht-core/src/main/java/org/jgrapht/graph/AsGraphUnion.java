/*
 * (C) Copyright 2009-2023, by Ilya Razenshteyn and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.graph;

import org.jgrapht.*;
import org.jgrapht.util.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * Read-only union of two graphs.
 * 
 * <p>
 * Given two graphs $G_1 = (V_1, E_1)$ and $G_2 = (V_2, E_2)$, their
 * union is $G = (V, E)$, where $V = V_1 \cup V_2$ and $E = E_1 \cup E_2$.
 * A {@link WeightCombiner} is used in order to calculate the combined edge weights.
 * This union is <i>read-only</i>, and any method that tries to modify
 * throws an {@link UnsupportedOperationException}.
 * 
 * @param <V> the vertex type
 * @param <E> the edge type
 * 
 * @author Ilya Razenshteyn
 * 
 * @see WeightCombiner
 */
public class AsGraphUnion<V, E>
    extends AbstractGraph<V, E>
    implements Serializable
{
    private static final long serialVersionUID = -3848082143382987713L;

    private static final String READ_ONLY = "union of graphs is read-only";

    private final Graph<V, E> g1;
    private final GraphType type1;
    private final Graph<V, E> g2;
    private final GraphType type2;
    private final GraphType type;
    private final WeightCombiner operator;

    /**
     * Construct a new graph union.
     * 
     * @param g1 the first graph
     * @param g2 the second graph
     * @param operator the weight combiner (policy for edge weight calculation)
     */
    public AsGraphUnion(Graph<V, E> g1, Graph<V, E> g2, WeightCombiner operator)
    {
        this.g1 = GraphTests.requireDirectedOrUndirected(g1);
        this.type1 = g1.getType();

        this.g2 = GraphTests.requireDirectedOrUndirected(g2);
        this.type2 = g2.getType();

        if (g1 == g2) {
            throw new IllegalArgumentException("g1 is equal to g2");
        }
        this.operator = Objects.requireNonNull(operator, "Weight combiner cannot be null");

        // compute result type
        DefaultGraphType.Builder builder = new DefaultGraphType.Builder();
        if (type1.isDirected() && type2.isDirected()) {
            builder = builder.directed();
        } else if (type1.isUndirected() && type2.isUndirected()) {
            builder = builder.undirected();
        } else {
            builder = builder.mixed();
        }
        this.type = builder
            .allowSelfLoops(type1.isAllowingSelfLoops() || type2.isAllowingSelfLoops())
            .allowMultipleEdges(true).weighted(true).modifiable(false).build();
    }

    /**
     * Construct a new graph union. The union will use the {@link WeightCombiner#SUM} weight
     * combiner.
     * 
     * @param g1 the first graph
     * @param g2 the second graph
     */
    public AsGraphUnion(Graph<V, E> g1, Graph<V, E> g2)
    {
        this(g1, g2, WeightCombiner.SUM);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> getAllEdges(V sourceVertex, V targetVertex)
    {
        boolean inG1 = g1.containsVertex(sourceVertex) && g1.containsVertex(targetVertex);
        boolean inG2 = g2.containsVertex(sourceVertex) && g2.containsVertex(targetVertex);

        if (inG1 && inG2) {
            return new UnmodifiableUnionSet<>(
                g1.getAllEdges(sourceVertex, targetVertex),
                g2.getAllEdges(sourceVertex, targetVertex));
        } else if (inG1) {
            return Collections.unmodifiableSet(g1.getAllEdges(sourceVertex, targetVertex));
        } else if (inG2) {
            return Collections.unmodifiableSet(g2.getAllEdges(sourceVertex, targetVertex));
        }
        return Collections.emptySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E getEdge(V sourceVertex, V targetVertex)
    {
        E res = null;
        if (g1.containsVertex(sourceVertex) && g1.containsVertex(targetVertex)) {
            res = g1.getEdge(sourceVertex, targetVertex);
        }
        if ((res == null) && g2.containsVertex(sourceVertex) && g2.containsVertex(targetVertex)) {
            res = g2.getEdge(sourceVertex, targetVertex);
        }
        return res;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException always, since operation is unsupported
     */
    @Override
    public Supplier<V> getVertexSupplier()
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException always, since operation is unsupported
     */
    @Override
    public Supplier<E> getEdgeSupplier()
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException always, since operation is unsupported
     */
    @Override
    public E addEdge(V sourceVertex, V targetVertex)
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException always, since operation is unsupported
     */
    @Override
    public boolean addEdge(V sourceVertex, V targetVertex, E e)
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException always, since operation is unsupported
     */
    @Override
    public V addVertex()
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException always, since operation is unsupported
     */
    @Override
    public boolean addVertex(V v)
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsEdge(E e)
    {
        return g1.containsEdge(e) || g2.containsEdge(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsVertex(V v)
    {
        return g1.containsVertex(v) || g2.containsVertex(v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> edgeSet()
    {
        return new UnmodifiableUnionSet<>(g1.edgeSet(), g2.edgeSet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> edgesOf(V vertex)
    {
        boolean inG1 = g1.containsVertex(vertex);
        boolean inG2 = g2.containsVertex(vertex);

        if (inG1 && inG2) {
            return new UnmodifiableUnionSet<>(g1.edgesOf(vertex), g2.edgesOf(vertex));
        } else if (inG1) {
            return Collections.unmodifiableSet(g1.edgesOf(vertex));
        } else if (inG2) {
            return Collections.unmodifiableSet(g2.edgesOf(vertex));
        } else {
            throw new IllegalArgumentException("no such vertex in graph: " + vertex.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> incomingEdgesOf(V vertex)
    {
        boolean inG1 = g1.containsVertex(vertex);
        boolean inG2 = g2.containsVertex(vertex);

        if (inG1 && inG2) {
            return new UnmodifiableUnionSet<>(
                g1.incomingEdgesOf(vertex), g2.incomingEdgesOf(vertex));
        } else if (inG1) {
            return Collections.unmodifiableSet(g1.incomingEdgesOf(vertex));
        } else if (inG2) {
            return Collections.unmodifiableSet(g2.incomingEdgesOf(vertex));
        } else {
            throw new IllegalArgumentException("no such vertex in graph: " + vertex.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> outgoingEdgesOf(V vertex)
    {
        boolean inG1 = g1.containsVertex(vertex);
        boolean inG2 = g2.containsVertex(vertex);

        if (inG1 && inG2) {
            return new UnmodifiableUnionSet<>(
                g1.outgoingEdgesOf(vertex), g2.outgoingEdgesOf(vertex));
        } else if (inG1) {
            return Collections.unmodifiableSet(g1.outgoingEdgesOf(vertex));
        } else if (inG2) {
            return Collections.unmodifiableSet(g2.outgoingEdgesOf(vertex));
        } else {
            throw new IllegalArgumentException("no such vertex in graph: " + vertex.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int degreeOf(V vertex)
    {
        if (type.isMixed()) {
            int d = 0;
            if (g1.containsVertex(vertex)) {
                d += g1.degreeOf(vertex);
            }
            if (g2.containsVertex(vertex)) {
                d += g2.degreeOf(vertex);
            }
            return d;
        } else if (type.isUndirected()) {
            int degree = 0;
            Iterator<E> it = edgesOf(vertex).iterator();
            while (it.hasNext()) {
                E e = it.next();
                degree++;
                if (getEdgeSource(e).equals(getEdgeTarget(e))) {
                    degree++;
                }
            }
            return degree;
        } else {
            return incomingEdgesOf(vertex).size() + outgoingEdgesOf(vertex).size();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int inDegreeOf(V vertex)
    {
        if (type.isMixed()) {
            int d = 0;
            if (g1.containsVertex(vertex)) {
                d += g1.inDegreeOf(vertex);
            }
            if (g2.containsVertex(vertex)) {
                d += g2.inDegreeOf(vertex);
            }
            return d;
        } else if (type.isUndirected()) {
            return degreeOf(vertex);
        } else {
            return incomingEdgesOf(vertex).size();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int outDegreeOf(V vertex)
    {
        if (type.isMixed()) {
            int d = 0;
            if (g1.containsVertex(vertex)) {
                d += g1.outDegreeOf(vertex);
            }
            if (g2.containsVertex(vertex)) {
                d += g2.outDegreeOf(vertex);
            }
            return d;
        } else if (type.isUndirected()) {
            return degreeOf(vertex);
        } else {
            return outgoingEdgesOf(vertex).size();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException always, since operation is unsupported
     */
    @Override
    public E removeEdge(V sourceVertex, V targetVertex)
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException always, since operation is unsupported
     */
    @Override
    public boolean removeEdge(E e)
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException always, since operation is unsupported
     */
    @Override
    public boolean removeVertex(V v)
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<V> vertexSet()
    {
        return new UnmodifiableUnionSet<>(g1.vertexSet(), g2.vertexSet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getEdgeSource(E e)
    {
        if (g1.containsEdge(e)) {
            return g1.getEdgeSource(e);
        }
        if (g2.containsEdge(e)) {
            return g2.getEdgeSource(e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getEdgeTarget(E e)
    {
        if (g1.containsEdge(e)) {
            return g1.getEdgeTarget(e);
        }
        if (g2.containsEdge(e)) {
            return g2.getEdgeTarget(e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getEdgeWeight(E e)
    {
        if (g1.containsEdge(e) && g2.containsEdge(e)) {
            return operator.combine(g1.getEdgeWeight(e), g2.getEdgeWeight(e));
        }
        if (g1.containsEdge(e)) {
            return g1.getEdgeWeight(e);
        }
        if (g2.containsEdge(e)) {
            return g2.getEdgeWeight(e);
        }
        throw new IllegalArgumentException("no such edge in the union");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphType getType()
    {
        return type;
    }

    /**
     * Throws {@link UnsupportedOperationException} since graph union is read-only.
     */
    @Override
    public void setEdgeWeight(E e, double weight)
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }
}
