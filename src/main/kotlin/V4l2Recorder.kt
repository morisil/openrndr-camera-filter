import org.openrndr.Extension
import org.openrndr.Program
import org.openrndr.draw.Drawer
import org.openrndr.draw.RenderTarget
import org.openrndr.draw.renderTarget
import org.openrndr.ffmpeg.VideoWriter
import org.openrndr.ffmpeg.VideoWriterProfile

class V4l2Recorder(private val virtualCameraDevice: String) : Extension {

    override var enabled: Boolean = true

    private lateinit var virtualCameraRenderTarget: RenderTarget

    private lateinit var virtualCameraWriter: VideoWriter

    override fun setup(program: Program) {
        virtualCameraRenderTarget = renderTarget(program.width, program.height) {
            colorBuffer()
        }
        virtualCameraWriter = VideoWriter.create()
            .profile(object: VideoWriterProfile() {
                override fun arguments(): Array<String> {
                    return arrayOf("-vf", "vflip", "-pix_fmt", "yuv420p", "-f", "v4l2")
                }
            })
            .size(program.width, program.height).output(virtualCameraDevice).start()
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
        virtualCameraWriter.frame(buffer)
    }

}
