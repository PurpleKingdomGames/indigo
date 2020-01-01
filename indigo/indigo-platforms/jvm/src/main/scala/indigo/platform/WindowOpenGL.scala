package indigo.platform

import org.lwjgl.glfw._
import org.lwjgl.system._

import java.nio._

import org.lwjgl.glfw.GLFW._
import org.lwjgl.system.MemoryStack._
import org.lwjgl.system.MemoryUtil._

import indigo.shared.config.GameConfig
@SuppressWarnings(
  Array(
    "org.wartremover.warts.Equals",
    "org.wartremover.warts.NonUnitStatements",
    "org.wartremover.warts.Throw",
    "org.wartremover.warts.Var"
  )
)
final class WindowOpenGL {

  var window: Long = 0

  def init(gameConfig: GameConfig): Unit = {
    GLFWErrorCallback.createPrint(System.err).set()

    if (!glfwInit())
      throw new IllegalStateException("Unable to initialize GLFW")

    glfwDefaultWindowHints()
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

    window = glfwCreateWindow(gameConfig.viewport.width, gameConfig.viewport.height, "Indigo!", NULL, NULL)

    if (window == NULL)
      throw new RuntimeException("Failed to create the GLFW window")

    glfwSetKeyCallback(
      window,
      // (window, key, scancode, action, mods) => {
      (window, key, _, action, _) => {
        //TODO: Send key events to WorldEvent equivalent.
        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
          glfwSetWindowShouldClose(window, true) // We will detect this in the rendering loop
      }
    )

    val stack: MemoryStack = stackPush()

    try {
      val pWidth: IntBuffer  = stack.mallocInt(1)
      val pHeight: IntBuffer = stack.mallocInt(1)

      glfwGetWindowSize(window, pWidth, pHeight)

      val vidmode: GLFWVidMode = glfwGetVideoMode(glfwGetPrimaryMonitor())

      glfwSetWindowPos(
        window,
        (vidmode.width() - pWidth.get(0)) / 2,
        (vidmode.height() - pHeight.get(0)) / 2
      )

    } finally {
      if (stack != null) stack.close();
    }

    glfwMakeContextCurrent(window)
    glfwSwapInterval(1)
    glfwShowWindow(window)
  }

}
