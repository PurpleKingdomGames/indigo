package indigo.platform.renderer

import indigo.facades.WebGL2RenderingContext
import scala.scalajs.js.typedarray.Float32Array
import indigo.shared.display.DisplayObject
import indigo.shared.ClearColor
import org.scalajs.dom.raw.WebGLProgram
import indigo.shared.metrics.Metrics
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.WebGLBuffer
import indigo.shared.EqualTo._

class RendererLayer(gl2: WebGL2RenderingContext, textureLocations: List[TextureLookupResult]) {

  private val displayObjectUBOBuffer: WebGLBuffer = gl2.createBuffer()
  private val instanceDataBuffer: WebGLBuffer     = gl2.createBuffer()

  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.NonUnitStatements"))
  def drawLayer(
      displayObjects: List[DisplayObject],
      frameBufferComponents: FrameBufferComponents,
      clearColor: ClearColor,
      shaderProgram: WebGLProgram,
      layer: CurrentDrawLayer,
      metrics: Metrics
  ): Unit = {

    FrameBufferFunctions.switchToFramebuffer(gl2, frameBufferComponents.frameBuffer, clearColor)

    gl2.useProgram(shaderProgram)

    val textureLocation = gl2.getUniformLocation(shaderProgram, "u_texture")
    gl2.uniform1i(textureLocation, 0)

    // Projection
    val projectionMatrix: scalajs.js.Array[Double] =
      RendererFunctions.orthographicProjectionMatrix

    // Bind UBO buffer
    gl2.bindBuffer(ARRAY_BUFFER, displayObjectUBOBuffer)
    gl2.bindBufferRange(
      gl2.UNIFORM_BUFFER,
      0,
      displayObjectUBOBuffer,
      0,
      RendererFunctions.projectionMatrixUBODataSize * Float32Array.BYTES_PER_ELEMENT
    )
    gl2.bufferData(ARRAY_BUFFER, new Float32Array(projectionMatrix), STATIC_DRAW)

    // Instance attributes
    gl2.bindBuffer(ARRAY_BUFFER, instanceDataBuffer)
    RendererFunctions.bindInstanceAttributes(gl2, 2, List(2, 2, 4, 2, 2, 1, 1, 1))

    var lastTextureName: String = ""

    RendererFunctions.sortByDepth(displayObjects).foreach { displayObject =>
      metrics.record(layer.metricStart)

      // Set all the uniforms
      RendererFunctions.updateUBOData(displayObject)
      gl2.bufferData(ARRAY_BUFFER, new Float32Array(RendererFunctions.uboData), STATIC_DRAW)

      /*
// // position attribute
// glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(float), (void*)0);
// glEnableVertexAttribArray(0);
// // color attribute
// glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(float), (void*)(3* sizeof(float)));
// glEnableVertexAttribArray(1);

    // gl.vertexAttribPointer(
    //   indx = attributeLocation,
    //   size = size,
    //   `type` = FLOAT,
    //   normalized = false,
    //   stride = 0,
    //   offset = 0
    // )
    // gl.enableVertexAttribArray(attributeLocation)

    while nothing needs to change (i.e. images or < batch size), keep piling data into our array.
    On state change or batch size met:
    - bind the buffer
    - bind all the data to the buffer
    - set the attribute pointers (done?)
    - enable the arrays
    - set the divisor
    - draw the instance count.

       */

      // If needed, update texture state
      if (displayObject.imageRef !== lastTextureName) {
        textureLocations.find(t => t.name === displayObject.imageRef).foreach { textureLookup =>
          gl2.bindTexture(TEXTURE_2D, textureLookup.texture)
          lastTextureName = displayObject.imageRef
        }

        gl2.drawArraysInstanced(TRIANGLE_STRIP, 0, 4, 1)
      } else {
        gl2.drawArraysInstanced(TRIANGLE_STRIP, 0, 4, 1)
      }

      metrics.record(layer.metricDraw)

      metrics.record(layer.metricEnd)
    }

  }

}
