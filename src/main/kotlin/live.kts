@file:Suppress("UNUSED_LAMBDA_EXPRESSION")

import org.openrndr.draw.isolated
import org.openrndr.draw.shadeStyle
import java.lang.Math.*

{ program: LiveCodingCameraProgram ->
    program.apply {
        extend {
            shader.parameters["time"] = seconds
            realCamera.draw(drawer, blind = true)
            realCamera.colorBuffer?.let { cameraBuffer ->
                shader.apply(arrayOf(cameraBuffer, previousVirtualCameraBuffer), virtualCameraBuffer)
            }
            drawer.image(virtualCameraBuffer)
            drawer.isolated {
                stroke = null
                shadeStyle = shadeStyle {
                    // vertexTransform = "x_viewMatrix = x_viewMatrix * i_transform;"
                    // FIXME assuming that I have my custom fragmentTransform, is there anyway to pass particle properties buffer to it?
//          fragmentTransform = """
//            float dist = length(v_worldPosition);
//            x_fill *= smoothstep(1, .9, dist) * pow(dist, 4);
//          """.trimIndent()
                    fragmentTransform = """
                        
#define SCALE 30.0
// rotation speed, might be negative to spin counter-clockwise
#define ROTATION_SPEED -1.5

#define INTENSITY_PULSE_SPEED 3
            float iTime = p_time;
           vec2 uv = (.5 - c_boundsPosition.xy) * 2;
           float dist = length(uv);
           float angle = atan(uv.x, uv.y);
           angle += p_index * .3;

           float newCol = (
sin(
      (dist * SCALE)
      + angle
      + (cos(dist * SCALE))
      - (iTime * ROTATION_SPEED)
  )
      - dist * (2.3 + sin(iTime * INTENSITY_PULSE_SPEED) * .3)
      + 0.3
           ) * smoothstep(1, .9, dist) * 2.;

            newCol = clamp(newCol, 0, 1);
         x_fill = vec4(newCol);
        """
                    parameter("time", seconds)
                }
                for (i in 0 .. 10) {
                    drawer.shadeStyle!!.parameter("index", i);
                    circle(
                        width * (sin(seconds * .3 + i * .02) + 1.0) * .5,
                        height * (sin(seconds * .4 + i * .028) + 1.0) * .5, radius = 180.0 + abs(cos(seconds * .2)) * height * 0.1)
                }

            }
            swapVirtualCameraBuffers()
        }

    }
}
