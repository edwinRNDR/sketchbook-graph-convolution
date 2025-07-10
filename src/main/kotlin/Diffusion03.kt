import lib.Graph
import lib.Link
import lib.Matrix
import lib.Node
import lib.adjacencyMatrix
import lib.degreeMatrix
import lib.laplacianMatrix
import lib.minus
import lib.normalize
import lib.plus
import lib.times
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.kdtree.kdTree
import org.openrndr.extra.noise.primitives.random
import org.openrndr.extra.noise.scatter
import org.openrndr.extra.noise.uniform
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.LineSegment

fun main() {
    application {
        configure {
            width = 720
            height = 720
        }

        program {
            class MyNode(override val id: Int, val position: Vector2) : Node

            val g = Graph<MyNode>()

            val pts = drawer.bounds.scatter(10.0)

            for (p in pts) {
                g.nodes.add(MyNode(g.nodes.size, p))
            }

            val map = pts.mapIndexed { index, vector2 ->Pair(vector2, index) }.toMap()
            val kd = pts.kdTree()

            for (p in pts) {

                val nearest = kd.findKNearest(p, 3)
                for (n in nearest) {
                    val source = map[p] ?: error("no source found")
                    val target = map[n] ?: error("no target found")
                    g.links.add(Link(source, target))
                }
            }

            val lm = g.adjacencyMatrix().normalize(g.degreeMatrix())

            val signal = Matrix.zeros(g.nodes.size, 1)

            var diffused = lm * signal

            fun filter(coeffs: DoubleArray, shift: Matrix, signal: Matrix): Matrix {
                val shifted = mutableListOf<Matrix>()

                var current = signal
                shifted.add(current)

                for (i in 0 until coeffs.size - 1) {
                    current = shift * current
                    shifted.add(current)
                }
                var result = Matrix.zeros(signal.rows, signal.cols)
                for (i in 0 until coeffs.size) {
                    result += (shifted[i] * coeffs[i])
                }
                return result
            }

            var diffusedHist = MutableList(20) { Matrix.zeros(g.nodes.size, 1) }
//            extend(ScreenRecorder()) {
//                frameRate = 60
//                maximumDuration = 20.0
//                contentScale = 1.0
//            }
            extend {
                drawer.clear(ColorRGBa.GRAY.shade(1.0))

                if (Double.uniform(0.0, 1.0) < 0.01) {
                    diffused.data[0][0] = 1.0
                }
                val newDiffused = filter(doubleArrayOf(1.0, 0.1),lm, diffused) - diffusedHist.last() * 0.062

                for (y in 0 until newDiffused.rows) {
                    newDiffused[y, 0] = newDiffused[y, 0].coerceIn(0.0, 1.0)
                }

                diffusedHist.add(0, diffused)
                diffusedHist.removeLast()
                diffused = newDiffused //* 0.99 + diffused * 0.0
                drawer.circles {
                    for (i in g.nodes.indices) {
                        fill = ColorRGBa.WHITE.shade(diffused[i, 0].coerceIn(0.0, 1.0))
                        circle(g.nodes[i].position, 9.0)
                    }
                }

                drawer.lineSegments(g.links.map { LineSegment(g.nodes[it.source].position, g.nodes[it.target].position) })
            }
        }
    }
}