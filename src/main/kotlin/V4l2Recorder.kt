import org.openrndr.Extension
import org.openrndr.Program
import org.openrndr.draw.Drawer
import org.openrndr.draw.RenderTarget
import org.openrndr.draw.renderTarget
import org.openrndr.ffmpeg.VideoWriter
import org.openrndr.ffmpeg.VideoWriterProfile

/**
 * The recorder will use `ffmpeg` with `-f v4l2` option for recording. Can be used together with `v4l2loopback`.
 *
 * @param v4lDevice the v4l device used for recording, should be writeable.
 * @param pixelFormat `rgb24` by default, will make it compatible with many clients like Chrome browser
 *                      and use less CPU for transcoding. It can be also yuv420p, etc.
 *                      see ffmpeg pix_fmt documentation.
 */
class V4l2Recorder(
    private val v4lDevice: String,
    private val pixelFormat: String = "rgb24"
) : Extension {

    override var enabled: Boolean = true

    private lateinit var videoRenderTarget: RenderTarget

    private lateinit var videoWriter: VideoWriter

    override fun setup(program: Program) {
        videoRenderTarget = renderTarget(program.width, program.height) {
            colorBuffer()
        }
        videoWriter = VideoWriter.create()
            .profile(object: VideoWriterProfile() {
                override fun arguments(): Array<String> {
                    return arrayOf("-vf", "vflip", "-pix_fmt", pixelFormat, "-f", "v4l2")
                }
            })
            .size(program.width, program.height)
            .output(v4lDevice)
            .start()
    }

    override fun beforeDraw(drawer: Drawer, program: Program) {
        videoRenderTarget.bind()
        program.backgroundColor?.let {
            drawer.background(it)
        }
    }

    override fun afterDraw(drawer: Drawer, program: Program) {
        videoRenderTarget.unbind()
        val buffer = videoRenderTarget.colorBuffer(0)
        drawer.image(buffer)
        videoWriter.frame(buffer)
    }

}
