package lib

import kotlin.test.Test
import kotlin.test.assertEquals

class TestGraph {

    @Test
    fun testAdjacencyMatrix() {
        class MyNode(override val id: Int): Node
        val g = Graph<MyNode>()
        g.nodes.add(MyNode(0))
        g.nodes.add(MyNode(1))
        g.nodes.add(MyNode(2))
        g.nodes.add(MyNode(3))
        g.links.add(Link(0, 1))
        g.links.add(Link(1, 2))
        g.links.add(Link(2, 3))
        g.links.add(Link(3, 0))

        val am = g.adjacencyMatrix()
        val asm = g.adjacencySparseMatrix()
        asm.checkIntegrity()

        assertEquals(4, am.rows)
        assertEquals(4, asm.rows)
        val adm = asm.toDenseMatrix()

        for (j in 0 until am.rows) {
            for (i in 0 until am.rows) {
                assertEquals(am[i, j], asm[i, j], 1e-12)
                assertEquals(am[i, j], adm[i, j], 1e-12)
            }
        }
    }

    @Test
    fun testNormalizeMatrix() {
        class MyNode(override val id: Int): Node
        val g = Graph<MyNode>()
        g.nodes.add(MyNode(0))
        g.nodes.add(MyNode(1))
        g.nodes.add(MyNode(2))
        g.nodes.add(MyNode(3))
        g.links.add(Link(0, 1))
        g.links.add(Link(1, 2))
        g.links.add(Link(2, 3))
        g.links.add(Link(3, 0))

        val am = g.adjacencyMatrix().normalize(g.degreeMatrix())
        val asm = g.adjacencySparseMatrix().normalize(g.degreeSparseMatrix())
        asm.checkIntegrity()

        assertEquals(4, am.rows)
        assertEquals(4, asm.rows)
        val adm = asm.toDenseMatrix()

        for (j in 0 until am.rows) {
            for (i in 0 until am.rows) {
                assertEquals(am[i, j], asm[i, j], 1e-12)
                assertEquals(am[i, j], adm[i, j], 1e-12)
            }
        }
    }

    @Test
    fun testDegreeMatrix() {
        class MyNode(override val id: Int): Node
        val g = Graph<MyNode>()
        g.nodes.add(MyNode(0))
        g.nodes.add(MyNode(1))
        g.nodes.add(MyNode(2))
        g.nodes.add(MyNode(3))
        g.links.add(Link(0, 1))
        g.links.add(Link(1, 2))
        g.links.add(Link(2, 3))
        g.links.add(Link(3, 0))

        val dm = g.degreeMatrix()
        val dsm = g.degreeSparseMatrix()
        dsm.checkIntegrity()
        val ddm = dsm.toDenseMatrix()

        for (j in 0 until dm.rows) {
            for (i in 0 until dm.rows) {
                assertEquals(dm[i, j], dsm[i, j], 1e-12)
                assertEquals(dm[i, j], ddm[i, j], 1e-12)
            }
        }
    }

    @Test
    fun testLaplacianMatrix() {
        class MyNode(override val id: Int): Node
        val g = Graph<MyNode>()
        g.nodes.add(MyNode(0))
        g.nodes.add(MyNode(1))
        g.nodes.add(MyNode(2))
        g.nodes.add(MyNode(3))
        g.links.add(Link(0, 1))
        g.links.add(Link(1, 2))
        g.links.add(Link(2, 3))
        g.links.add(Link(3, 0))

        val dm = g.laplacianMatrix()
        val dsm = g.laplacianSparseMatrix()
        val ddm = dsm.toDenseMatrix()

        for (j in 0 until dm.rows) {
            for (i in 0 until dm.rows) {
                assertEquals(dm[i, j], dsm[i, j], 1e-12)
                assertEquals(dm[i, j], ddm[i, j], 1e-12)
            }
        }
    }
}