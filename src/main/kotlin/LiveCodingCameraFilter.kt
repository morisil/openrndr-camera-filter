import org.openrndr.Program
import org.openrndr.application
import org.openrndr.draw.*
import org.openrndr.extra.olive.Olive
import org.openrndr.ffmpeg.PlayMode
import org.openrndr.ffmpeg.VideoPlayerFFMPEG
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2

/*
 before running the program you need to install v4l2loopback, on ubuntu do:

 $ sudo apt-get install v4l2loopback-dkms v4l2loopback-utils
 $ sudo modprobe v4l2loopback devices=1 max_buffers=2 exclusive_caps=1 card_label="VirtualCam #0"
 $ sudo adduser $USER video
 $ v4l2-ctl --list-devices

 Adding the user to the group video might require to log out and log in again
 or even restarting the system
 */
fun main() = application {
    // values should be adjusted according to your system
    // $ v4l2-ctl --list-devices
    val cameraDevice = "/dev/video4"
    val virtualCameraDevice = "/dev/video6"
    /*
     the resolution of your physical camera, possibilities can be obtained with:

     $ ffmpeg -f v4l2 -list_formats all -i /dev/video4
     */
    val cameraWidth = 800
    val cameraHeight = 448

    val virtualCameraWidth = 1920
    val virtualCameraHeight = 1080

    // application becomes the preview window
    configure {
        width = virtualCameraWidth
        height = virtualCameraHeight
        hideWindowDecorations = true
        // my second preview screen is below my main screen
        position = IntVector2(0, 1920)
    }

    program(LiveCodingCameraProgram()) {
        realCamera = VideoPlayerFFMPEG.fromDevice(
            cameraDevice,
            PlayMode.VIDEO,
            imageWidth = cameraWidth,
            imageHeight = cameraHeight
        )
        virtualCameraBuffer = colorBuffer(width, height, format = ColorFormat.RGB, type = ColorType.UINT8)
        virtualCameraBuffer.flipV = true
        shader = Filter(watcher = filterWatcherFromUrl( "file:src/main/resources/camera.frag"))
        shader.parameters["resolution"] = Vector2(width.toDouble(), height.toDouble())

        realCamera.play()

        extend(V4l2Recorder(virtualCameraDevice))
        //extend(AkvcamRecorder(virtualCameraDevice))
        extend(Olive<Program>())
    }
}

class LiveCodingCameraProgram : Program() {
    lateinit var realCamera: VideoPlayerFFMPEG
    lateinit var virtualCameraBuffer: ColorBuffer
    lateinit var shader: Filter
}
