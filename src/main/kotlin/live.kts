@file:Suppress("UNUSED_LAMBDA_EXPRESSION")

import org.openrndr.color.ColorRGBa
import java.lang.Math.*

{ program: LiveCodingCameraProgram ->
    program.apply {
        extend {
            shader.parameters["time"] = seconds
            realCamera.draw(drawer, blind = true)
            realCamera.colorBuffer?.let { cameraBuffer ->
                shader.apply(cameraBuffer, virtualCameraBuffer)
            }
            drawer.image(virtualCameraBuffer)
            drawer.fill = ColorRGBa.PINK
            drawer.circle(width * .2, height * .2, radius = abs(cos(seconds)) * height * 0.2)
        }
    }
}
