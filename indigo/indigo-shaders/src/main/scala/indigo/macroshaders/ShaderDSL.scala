package indigo.macroshaders

object ShaderDSL extends ShaderDSLOps:

  // Structure
  // final case class Uniform[T](name: String)
  // final case class UniformBlock(uniforms: List[Uniform[_]])

  // final case class ShaderEnv(uniformBlocks: List[UniformBlock])

  // opaque type IndigoFrag = Function1[ShaderEnv, rgba]
  // object IndigoFrag:
  //   inline def apply(f: ShaderEnv => rgba): IndigoFrag = f

  //   extension (inline frag: IndigoFrag)
  //     inline def toGLSL: String =
  //       ShaderMacros.toAST(frag).render

end ShaderDSL
