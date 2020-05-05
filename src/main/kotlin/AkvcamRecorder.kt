import org.openrndr.Extension
import org.openrndr.Program
import org.openrndr.draw.ColorFormat
import org.openrndr.draw.Drawer
import org.openrndr.draw.RenderTarget
import org.openrndr.draw.renderTarget
import java.io.FileOutputStream
import java.nio.ByteBuffer

/**
 * You might need to do something like:
 *
 * ```
 * echo "1" >/sys/devices/virtual/video4linux/video7/controls/vflip
 * ```
 */
class AkvcamRecorder(v4lDevice: String) : Extension {

    override var enabled: Boolean = true

    private lateinit var virtualCameraRenderTarget: RenderTarget

    private lateinit var virtualCameraBuffer: ByteBuffer

    private val virtualCameraOut = FileOutputStream(v4lDevice)

    override fun setup(program: Program) {
        virtualCameraBuffer = ByteBuffer.allocateDirect(program.width * program.height * 3)
        virtualCameraRenderTarget = renderTarget(program.width, program.height) {
            colorBuffer(ColorFormat.RGB)
        }
    }

    override fun beforeDraw(drawer: Drawer, program: Program) {
        virtualCameraRenderTarget.bind()
        program.backgroundColor?.let {
            drawer.background(it)
        }
    }

    override fun afterDraw(drawer: Drawer, program: Program) {
        virtualCameraRenderTarget.unbind()
        val buffer = virtualCameraRenderTarget.colorBuffer(0)
        drawer.image(buffer)
        buffer.read(virtualCameraBuffer)
        virtualCameraOut.channel.write(virtualCameraBuffer)
    }

    override fun shutdown(program: Program) {
        virtualCameraOut.close()
    }

}
