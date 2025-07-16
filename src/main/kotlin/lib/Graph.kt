package lib

import kotlin.math.sqrt


/**
 * Represents a weighted directed link (or edge) between two nodes in a graph.
 *
 * @property source The identifier of the source node for the link.
 * @property target The identifier of the target node for the link.
 * @property weight The weight associated with the link. Defaults to `1.0` if not specified.
 */
@JvmRecord
data class Link(val source: Int, val target: Int, val weight: Double = 1.0)

/**
 * Represents a basic interface for nodes within a data structure such as a graph.
 *
 * This interface defines a contract for any node class to include an identifying property of type `Int`,
 * which can be used for unique identification or indexing within a collection of nodes.
 *
 * Classes implementing the `Node` interface can be used in various graph or network structures
 * that rely on standardized node attributes.
 *
 * @property id An integer value uniquely identifying the node.
 */
interface Node {
    val id: Int
}

/**
 * Represents a graph data structure consisting of generic nodes and links (edges).
 * The graph allows storage and manipulation of nodes of a specified type and links
 * connecting them. Each node in the graph must implement the `Node` interface, and
 * the links represent connections between two nodes with an optional weight.
 *
 * @param N The type of nodes contained in the graph. Must implement the `Node` interface.
 *
 * @property nodes A mutable list containing all the nodes in the graph. Nodes are of type `N`.
 * @property links A mutable list containing all the links (edges) in the graph, represented as `Link` objects.
 */
class Graph<N : Node>(val directed: Boolean = false) {
    val nodes = mutableListOf<N>()
    val links = mutableListOf<Link>()
}

/**
 * Computes the adjacency matrix representation of the graph.
 *
 * The adjacency matrix is a square matrix where each element at position [i, j]
 * represents the weight of the edge between node `i` and node `j` in the graph.
 * If there is no edge between two nodes, the corresponding matrix element is 0.
 *
 * @return A Matrix representing the adjacency matrix of the graph. The matrix is symmetric
 *         if the graph is undirected, with the weight of edges assigned to their respective positions.
 */
fun Graph<*>.adjacencyMatrix(): Matrix {
    val n = nodes.size
    val matrix = Matrix.zeros(n, n)
    for (link in links) {
        matrix[link.source, link.target] = link.weight
        if (!directed) {
            matrix[link.target, link.source] = link.weight
        }
    }
    return matrix
}

fun Graph<*>.adjacencySparseMatrix(): SparseMatrix {
    val n = nodes.size
    val values = mutableMapOf<Pair<Int, Int>, Double>()

    for (link in links) {

        val weightST = values.getOrPut(Pair(link.source, link.target), { 0.0 })
        values[Pair(link.source, link.target)] = link.weight
        if (!directed) {
            val weightTS = values.getOrPut(Pair(link.target, link.source), { 0.0 })
            values[Pair(link.target, link.source)] = link.weight
        }
    }
    val entryList = values.entries.toList()
    return sparseMatrix(n, n, entryList.map { it.key }, entryList.map { it.value }, false)
}


/**
 * Computes the degree matrix of the graph.
 *
 * The degree matrix is a diagonal matrix where each diagonal entry represents the
 * sum of the weights of the edges connected to the corresponding node in the graph.
 *
 * @return A square matrix representing the degree matrix of the graph. The size of
 *         the matrix corresponds to the number of nodes in the graph.
 */
fun Graph<*>.degreeMatrix(): Matrix {
    val n = nodes.size
    val matrix = Matrix.zeros(n, n)
    for (link in links) {
        matrix[link.source, link.source] += link.weight
        matrix[link.target, link.target] += link.weight
    }
    return matrix
}

fun Graph<*>.degreeSparseMatrix(): SparseMatrix {
    val n = nodes.size
    val values = mutableMapOf<Pair<Int, Int>, Double>()
    for (link in links) {
        val weightST = values.getOrPut(Pair(link.source, link.source), { 0.0 })
        values[Pair(link.source, link.source)] = weightST + link.weight

        val weightTS = values.getOrPut(Pair(link.target, link.target), { 0.0 })
        values[Pair(link.target, link.target)] = weightTS + link.weight
    }

    val entryList = values.entries.toList()
    return sparseMatrix(n, n, entryList.map { it.key }, entryList.map { it.value }, false)
}

/**
 * Computes the Laplacian matrix of the graph.
 *
 * The Laplacian matrix is calculated as the difference between the degree matrix
 * and the adjacency matrix of the graph (L = D - A), where:
 * - The degree matrix (D) is a diagonal matrix where each diagonal element represents
 *   the sum of the weights of the edges connected to the corresponding node.
 * - The adjacency matrix (A) is a square matrix where each element represents the weight
 *   of the edge between two nodes.
 *
 * @return A square matrix representing the Laplacian matrix of the graph. The size of
 *         the matrix corresponds to the number of nodes in the graph.
 */
fun Graph<*>.laplacianMatrix(): Matrix {
    val dm = degreeMatrix()
    val am = adjacencyMatrix()
    return dm - am
}

/**
 * Computes the Laplacian sparse matrix of the graph.
 * The Laplacian matrix is defined as the difference between
 * the degree matrix and the adjacency matrix of the graph.
 * @see laplacianMatrix
 *
 * @return A SparseMatrix representing the Laplacian matrix of the graph.
 */
fun Graph<*>.laplacianSparseMatrix(): SparseMatrix {
    val dsm = degreeSparseMatrix()
    val asm = adjacencySparseMatrix()
    return dsm - asm
}

/**
 * Normalizes the current matrix using a given degree matrix and scaling factors.
 *
 * The normalization is based on the formula:
 * `result[j, i] = scale * this[j, i] / sqrt(degreeMatrix[j, j] * degreeMatrix[i, i])`,
 * where `scale` is determined by the diagonal or adjacency scaling factor.
 *
 * @param degreeMatrix The degree matrix used for normalization. Typically, a diagonal or near-diagonal matrix.
 * @param diagonalScale The scaling factor to be applied for diagonal elements. Default is 1.0.
 * @param adjacentScale The scaling factor to be applied for non-diagonal (adjacent) elements. Default is 1.0.
 * @return A new matrix representing the normalized version of the original matrix.
 */
fun Matrix.normalize(degreeMatrix: Matrix, diagonalScale: Double = 1.0, adjacentScale: Double = 1.0): Matrix {
    val result = Matrix(rows, cols)
    for (j in 0 until rows) {
        for (i in 0 until cols) {
            val scale = if (i == j) diagonalScale else adjacentScale
            result[j, i] = scale * this[j, i] / sqrt(degreeMatrix[j, j] * degreeMatrix[i, i])
        }
    }
    return result
}

/**
 * Normalizes the values of a sparse matrix using a given degree matrix and scaling factors.
 *
 * @param degreeMatrix The degree matrix used for normalization, represented as a sparse matrix.
 * @param diagonalScale The scaling factor applied to diagonal elements. Default is 1.0.
 * @param adjacentScale The scaling factor applied to off-diagonal (adjacent) elements. Default is 1.0.
 * @return A new sparse matrix with normalized values.
 */
fun SparseMatrix.normalize(
    degreeMatrix: SparseMatrix,
    diagonalScale: Double = 1.0,
    adjacentScale: Double = 1.0
): SparseMatrix {

    val newValues = DoubleArray(values.size)

    for (j in 0 until rows) {
        val start = rowPointers[j]
        val end = rowPointers.getOrNull(j + 1) ?: values.size
        for (pi in start until end) {
            val i = columnIndices[pi]
            val scale = if (i == j) diagonalScale else adjacentScale
            newValues[pi] = scale * values[i] / sqrt(degreeMatrix[j, j] * degreeMatrix[i, i])
        }
    }
    return SparseMatrix(rows, cols, newValues, columnIndices, rowPointers)
}