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
import org.openrndr.extra.noise.primitives.random
import org.openrndr.extra.noise.uniform
import org.openrndr.math.Polar
import org.openrndr.math.Vector2

fun main() {
    application {
        configure {
            width = 720
            height = 720
        }

        program {
            class MyNode(override val id: Int, val position: Vector2) : Node

            val g = Graph<MyNode>()

            for (i in 0 until 40) {
                g.nodes.add(MyNode(i, drawer.bounds.center + Polar(i * 360.0 / 40.0, 100.0).cartesian))
            }

            for (i in 0 until g.nodes.size) {
                g.links.add(Link(i, (i + 1) % g.nodes.size))
            }

            val lm = g.adjacencyMatrix()

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

            var oldDiffused = Matrix.zeros(g.nodes.size, 1)
            extend {
                drawer.clear(ColorRGBa.GRAY)

                if (Double.uniform(0.0, 1.0) < 0.01) {
                    diffused.data.random()[0] = 1.0
                }
                val c = 0.99
                val c2 = 0.995
                val newDiffused = filter(doubleArrayOf(1.0 - c, c), lm, diffused) - oldDiffused * c2


                oldDiffused = diffused
                diffused = newDiffused //* 0.99 + diffused * 0.0
                drawer.circles {
                    for (i in g.nodes.indices) {
                        fill = ColorRGBa.WHITE.shade(diffused[i, 0].coerceIn(0.0, 1.0))
                        circle(g.nodes[i].position, 9.0)
                    }
                }
            }
        }
    }
}