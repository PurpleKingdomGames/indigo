package indigo.platform

import org.lwjgl.opengl._

import org.lwjgl.glfw.Callbacks._
import org.lwjgl.glfw.GLFW._
import org.lwjgl.opengl.GL11._
import indigo.shared.config.GameConfig

@SuppressWarnings(
  Array(
    "org.wartremover.warts.NonUnitStatements",
    "org.wartremover.warts.Null",
    "org.wartremover.warts.While",
    "org.wartremover.warts.Var"
  )
)
final class IndigoJVMStartup {

  val startTime: Long =
    System.currentTimeMillis()

  val windowInstance: WindowOpenGL =
    new WindowOpenGL

  def run(gameConfig: GameConfig): Unit = {
    windowInstance.init(gameConfig)
    loop(gameConfig)

    glfwFreeCallbacks(windowInstance.window)
    glfwDestroyWindow(windowInstance.window)

    glfwTerminate()
    glfwSetErrorCallback(null).free()
  }

  def loop(gameConfig: GameConfig): Unit = {
    GL.createCapabilities()

    glClearColor(
      gameConfig.clearColor.r.toFloat,
      gameConfig.clearColor.g.toFloat,
      gameConfig.clearColor.b.toFloat,
      0.0f
    )

    while (!glfwWindowShouldClose(windowInstance.window)) {
      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

      IndigoJVMStartup.fetchCallback() match {
        case Some(loop) =>
          loop(System.currentTimeMillis() - startTime)
          IndigoJVMStartup.resetCallback()

        case None =>
          ()
      }

      glfwSwapBuffers(windowInstance.window)

      glfwPollEvents()
    }
  }

}

@SuppressWarnings(
  Array(
    "org.wartremover.warts.Var"
  )
)
object IndigoJVMStartup {

  private var currentFrameCallback: Option[Long => Unit] =
    None

  def requestAnimationFrame(loop: Long => Unit): Unit =
    currentFrameCallback = Some(loop)

  def fetchCallback(): Option[Long => Unit] =
    currentFrameCallback

  def resetCallback(): Unit =
    currentFrameCallback = None

}
