import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.shapes.uniform
import org.openrndr.extra.triangulation.voronoiDiagram
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.shape.Circle
import org.openrndr.shape.ShapeContour

fun main() {
    application {
        configure {
            width = 720
            height = 720
        }
        program {

            val contours = (0 until 20).map {
                Circle(drawer.bounds.offsetEdges(-100.0).uniform(), 100.0).contour
            }

            val points = contours.flatMap { it.equidistantPositions(20) }.toMutableList()




            extend(ScreenRecorder()) {
                contentScale = 1.0
                maximumDuration = 60.0
                frameRate = 60
            }
            extend {

                val vd = points.voronoiDiagram(bounds = drawer.bounds)
                for (i in points.indices) {
                    points[i] = points[i].mix(vd.cellCentroid(i), 0.01)

                }

                val contours = points.windowed(20, 20).map {
                    ShapeContour.fromPoints(it, closed = true)

                }



                drawer.stroke = ColorRGBa.WHITE
                drawer.fill = null
                drawer.contours(contours)
            }

        }
    }
}