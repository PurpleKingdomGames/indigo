import indigo.macroshaders.ShaderDSL.*
import indigo.macroshaders.ShaderDSL.glsl.*
import indigo.macroshaders.ShaderMacros

ShaderMacros.toAST(IndigoFrag(_ => rgba(1.0f, 1.0f, 0.0f, 1.0f))).render
