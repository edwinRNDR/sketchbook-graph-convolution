import lib.Graph
import lib.Link
import lib.Node
import lib.adjacencyMatrix

fun main() {
    class MyNode(override val id: Int): Node

    val g = Graph<MyNode>()

    for (i in 0 until 4) {
        g.nodes.add(MyNode(i))
    }

    for (i in 0 until g.nodes.size) {

        g.links.add(Link(i, (i + 1) % g.nodes.size))

    }

    val am = g.adjacencyMatrix()
    println(am.data.joinToString("\n") { it.joinToString(", ") })

}