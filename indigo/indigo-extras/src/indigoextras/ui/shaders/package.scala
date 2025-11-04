package indigoextras.ui.shaders

import indigo.shared.shader.ShaderProgram

val ui: Set[ShaderProgram] =
  Set(
    indigoextras.ui.shaders.LayerMask.shader
  )

val all: Set[ShaderProgram] =
  ui
