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

  private val initialBufferData: Float32Array =
    new Float32Array(RendererFunctions.uboDataSize)

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
      RendererFunctions.uboDataSize * Float32Array.BYTES_PER_ELEMENT
    )
    gl2.bufferData(ARRAY_BUFFER, initialBufferData, STATIC_DRAW)
    gl2.bufferSubData(ARRAY_BUFFER, 0, new Float32Array(projectionMatrix))

    var lastTextureName: String = ""

    RendererFunctions.sortByDepth(displayObjects).foreach { displayObject =>
      metrics.record(layer.metricStart)

      // Set all the uniforms
      RendererFunctions.updateUBOData(displayObject)
      gl2.bindBuffer(ARRAY_BUFFER, displayObjectUBOBuffer)
      gl2.bufferSubData(
        ARRAY_BUFFER,
        RendererFunctions.projectionMatrixUBODataSize * Float32Array.BYTES_PER_ELEMENT,
        new Float32Array(RendererFunctions.uboData)
      )

      // test
      gl2.bindBuffer(ARRAY_BUFFER, instanceDataBuffer)
      gl2.bufferData(ARRAY_BUFFER, new Float32Array(scalajs.js.Array[Double](20.0, 0.0, 0.0, 20.0)), STATIC_DRAW)

      for {
        offset <- RendererFunctions.bindInstanceAttibute(gl2, 2, 2, 0)
        _      <- RendererFunctions.bindInstanceAttibute(gl2, 3, 2, offset)
      } yield ()

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
