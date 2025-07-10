import lib.Graph
import lib.Link
import lib.Matrix
import lib.Node
import lib.degreeMatrix
import lib.laplacianMatrix
import lib.normalize

fun main() {
    class MyNode(override val id: Int): Node

    val g = Graph<MyNode>()

    for (i in 0 until 40) {
        g.nodes.add(MyNode(i))
    }

    for (i in 0 until g.nodes.size) {
        g.links.add(Link(i, (i + 1) % g.nodes.size))
    }

    val lm = g.laplacianMatrix().normalize(g.degreeMatrix())

    val signal = Matrix.zeros(g.nodes.size, 1)
    signal[0, 0] = 1.0

    val diffused = lm * signal
    println(diffused.data.joinToString("\n") { it.joinToString(", ") })

    val diffused1 = lm * diffused
    println("--")
    println(diffused1.data.joinToString("\n") { it.joinToString(", ") })

    val diffused2 = lm * diffused1
    println("--")
    println(diffused2.data.joinToString("\n") { it.joinToString(", ") })

}