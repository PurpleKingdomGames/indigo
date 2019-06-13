package indigo.platform

import org.lwjgl._
import org.lwjgl.glfw._
import org.lwjgl.opengl._
import org.lwjgl.system._

import java.nio._

import org.lwjgl.glfw.Callbacks._
import org.lwjgl.glfw.GLFW._
import org.lwjgl.opengl.GL11._
import org.lwjgl.system.MemoryStack._
import org.lwjgl.system.MemoryUtil._
import indigo.shared.config.GameConfig

@SuppressWarnings(
  Array(
    "org.wartremover.warts.Equals",
    "org.wartremover.warts.NonUnitStatements",
    "org.wartremover.warts.Null",
    "org.wartremover.warts.Throw",
    "org.wartremover.warts.Var",
    "org.wartremover.warts.While"
  )
)
object Experiment {

  // The window handle
  private var window: Long = 0

  def run(gameConfig: GameConfig): Unit = {
    System.out.println("Hello LWJGL " + Version.getVersion() + "!")

    init(gameConfig)
    loop(gameConfig)

    // Free the window callbacks and destroy the window
    glfwFreeCallbacks(window)
    glfwDestroyWindow(window)

    // Terminate GLFW and free the error callback
    glfwTerminate()
    glfwSetErrorCallback(null).free()
  }

  def init(gameConfig: GameConfig): Unit = {
    // Setup an error callback. The default implementation
    // will print the error message in System.err.
    GLFWErrorCallback.createPrint(System.err).set()

    // Initialize GLFW. Most GLFW functions will not work before doing this.
    if (!glfwInit())
      throw new IllegalStateException("Unable to initialize GLFW")

    // Configure GLFW
    glfwDefaultWindowHints()                  // optional, the current window hints are already the default
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)  // the window will stay hidden after creation
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable

    // Create the window
    window = glfwCreateWindow(gameConfig.viewport.width, gameConfig.viewport.height, "Indigo!", NULL, NULL)
    if (window == NULL)
      throw new RuntimeException("Failed to create the GLFW window")

    // Setup a key callback. It will be called every time a key is pressed, repeated or released.
    glfwSetKeyCallback(
      window,
      // (window, key, scancode, action, mods) => {
      (window, key, _, action, _) => {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
          glfwSetWindowShouldClose(window, true) // We will detect this in the rendering loop
      }
    )

    // Get the thread stack and push a new frame
    // try {
    val stack: MemoryStack = stackPush()
    val pWidth: IntBuffer  = stack.mallocInt(1) // int*
    val pHeight: IntBuffer = stack.mallocInt(1) // int*

    // Get the window size passed to glfwCreateWindow
    glfwGetWindowSize(window, pWidth, pHeight)

    // Get the resolution of the primary monitor
    val vidmode: GLFWVidMode = glfwGetVideoMode(glfwGetPrimaryMonitor())

    // Center the window
    glfwSetWindowPos(
      window,
      (vidmode.width() - pWidth.get(0)) / 2,
      (vidmode.height() - pHeight.get(0)) / 2
    )

    stack.close()
    // } finally {
    //   if (stack != null) stack.close();
    // } // the stack frame is popped automatically

    // Make the OpenGL context current
    glfwMakeContextCurrent(window)
    // Enable v-sync
    glfwSwapInterval(1)

    // Make the window visible
    glfwShowWindow(window)
  }

  def loop(gameConfig: GameConfig): Unit = {
    // This line is critical for LWJGL's interoperation with GLFW's
    // OpenGL context, or any context that is managed externally.
    // LWJGL detects the context that is current in the current thread,
    // creates the GLCapabilities instance and makes the OpenGL
    // bindings available for use.
    GL.createCapabilities()

    // Set the clear color
    glClearColor(
      gameConfig.clearColor.r.toFloat,
      gameConfig.clearColor.g.toFloat,
      gameConfig.clearColor.b.toFloat,
      0.0f
    )

    // Run the rendering loop until the user has attempted to close
    // the window or has pressed the ESCAPE key.
    while (!glfwWindowShouldClose(window)) {
      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT) // clear the framebuffer

      glfwSwapBuffers(window) // swap the color buffers

      // Poll for window events. The key callback above will only be
      // invoked during this call.
      glfwPollEvents()
    }
  }

}
