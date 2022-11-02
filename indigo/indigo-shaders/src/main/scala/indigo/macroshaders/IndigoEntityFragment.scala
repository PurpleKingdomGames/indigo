package indigo.macroshaders

import ShaderDSL.*

@SuppressWarnings(Array("scalafix:DisableSyntax.var"))
trait IndigoEntityFragment:

  // Variables
  def UV: vec2                       // Unscaled texture coordinates
  def SIZE: vec2                     // Width / height of the objects
  def CHANNEL_0: vec4                // Pixel value from texture channel 0
  def CHANNEL_1: vec4                // Pixel value from texture channel 1
  def CHANNEL_2: vec4                // Pixel value from texture channel 2
  def CHANNEL_3: vec4                // Pixel value from texture channel 3
  def CHANNEL_0_TEXTURE_COORDS: vec2 // Scaled texture coordinates
  def CHANNEL_1_TEXTURE_COORDS: vec2 // Scaled texture coordinates
  def CHANNEL_2_TEXTURE_COORDS: vec2 // Scaled texture coordinates
  def CHANNEL_3_TEXTURE_COORDS: vec2 // Scaled texture coordinates
  def CHANNEL_0_POSITION: vec2       // top left position of this texture on the atlas in UV coords
  def CHANNEL_1_POSITION: vec2       // top left position of this texture on the atlas in UV coords
  def CHANNEL_2_POSITION: vec2       // top left position of this texture on the atlas in UV coords
  def CHANNEL_3_POSITION: vec2       // top left position of this texture on the atlas in UV coords
  def CHANNEL_0_SIZE: vec2           // size of this texture on the atlas in UV coords
  def SCREEN_COORDS: vec2
  def ROTATION: Float
  def TEXTURE_SIZE: vec2 // Size of the texture in pixels
  def ATLAS_SIZE: vec2   // Size of the atlas this texture is on, in pixels
  def INSTANCE_ID: Int   // The current instance id

  // Light information
  def LIGHT_INDEX: Int
  def LIGHT_COUNT: Int
  def LIGHT_ACTIVE: Int
  def LIGHT_TYPE: Int
  def LIGHT_FAR_CUT_OFF: Int
  def LIGHT_FALLOFF_TYPE: Int
  def LIGHT_COLOR: vec4
  def LIGHT_SPECULAR: vec4
  def LIGHT_POSITION: vec2
  def LIGHT_ROTATION: Float
  def LIGHT_NEAR: Float
  def LIGHT_FAR: Float
  def LIGHT_ANGLE: Float
  def LIGHT_INTENSITY: Float

  // Constants
  def PI: Float
  def PI_2: Float
  def PI_4: Float
  def TAU: Float
  def TAU_2: Float
  def TAU_4: Float
  def TAU_8: Float

  // Outputs
  var COLOR: vec4
