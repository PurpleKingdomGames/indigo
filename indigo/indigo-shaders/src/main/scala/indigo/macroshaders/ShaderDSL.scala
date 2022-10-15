package indigo.macroshaders

object ShaderDSL:

  // Structure
  final case class Uniform[T](name: String)
  final case class UniformBlock(uniforms: List[Uniform[_]])

  final case class ShaderEnv(uniformBlocks: List[UniformBlock])

  opaque type IndigoFrag = Function1[ShaderEnv, rgba]
  object IndigoFrag:
    inline def apply(f: ShaderEnv => rgba): IndigoFrag = f

    extension (inline frag: IndigoFrag)
      inline def toGLSL: String =
        ShaderMacros.toAST(frag).render

  // TODO: This is the same as Reader. Make Reader a type class and borrow the implementation?
  opaque type Fragment[Env, A] = Env => A
  object Fragment:
    def apply[Env, A](f: Env => A): Fragment[Env, A] = f
    def pure[Env, A](value: A): Fragment[Env, A]     = _ => value
    def ask[Env]: Fragment[Env, Env]                 = identity

    def join[Env, A](frag: Fragment[Env, Fragment[Env, A]]): Fragment[Env, A] =
      (env: Env) => frag(env)(env)

    extension [Env, A](frag: Fragment[Env, A])
      def map[B](f: A => B): Fragment[Env, B]                            = (e: Env) => f(run(e))
      def ap[B](f: Fragment[Env, A => B]): Fragment[Env, B]              = (e: Env) => f(e)(run(e))
      def flatten[B](using ev: A <:< Fragment[Env, B]): Fragment[Env, B] = join((env: Env) => ev(run(env)))
      def flatMap[B](f: A => Fragment[Env, B]): Fragment[Env, B]         = join(map(f))
      def ask: Fragment[Env, Env]                                        = identity
      def asks(f: Env => A): Fragment[Env, A]                            = f
      def run(env: Env): A                                               = frag(env)
      def |*|[B](other: Fragment[Env, B]): Fragment[Env, (A, B)]         = combine(other)
      def combine[B](other: Fragment[Env, B]): Fragment[Env, (A, B)]     = (e: Env) => (frag.run(e), other.run(e))
      def merge[B, C](other: Fragment[Env, B])(f: ((A, B)) => C): Fragment[Env, C] = combine(other).map(f)
      def |>[B](ff: FragmentFunction[Env, A, B]): Fragment[Env, B]                 = pipe(ff)
      def pipe[B](ff: FragmentFunction[Env, A, B]): Fragment[Env, B]               = ff.run(frag)

  opaque type FragmentFunction[Env, A, B] = Fragment[Env, A] => Fragment[Env, B]
  object FragmentFunction:

    import Fragment.*

    inline def apply[Env, A, B](f: A => B): FragmentFunction[Env, A, B] =
      lift(f)

    extension [Env, A, B](sf: FragmentFunction[Env, A, B])
      def run: Fragment[Env, A] => Fragment[Env, B] = sf

      def >>>[C](other: FragmentFunction[Env, B, C]): FragmentFunction[Env, A, C] =
        andThen(other)

      def andThen[C](other: FragmentFunction[Env, B, C]): FragmentFunction[Env, A, C] =
        sf andThen other

      def &&&[C](other: FragmentFunction[Env, A, C]): FragmentFunction[Env, A, (B, C)] =
        and(other)

      def and[C](other: FragmentFunction[Env, A, C]): FragmentFunction[Env, A, (B, C)] =
        FragmentFunction.parallel(sf, other)

    /** Equvilent to `pure` but for SignalFunctions
      */
    def arr[Env, A, B](f: A => B): FragmentFunction[Env, A, B] =
      lift[Env, A, B](f)

    def lift[Env, A, B](f: A => B): FragmentFunction[Env, A, B] =
      (fa: Fragment[Env, A]) => fa.map(f)

    def flatLift[Env, A, B](f: A => Fragment[Env, B]): FragmentFunction[Env, A, B] =
      (fa: Fragment[Env, A]) => fa.flatMap(f)

    def parallel[Env, A, B, C](
        fa: FragmentFunction[Env, A, B],
        fb: FragmentFunction[Env, A, C]
    ): FragmentFunction[Env, A, (B, C)] =
      (s: Fragment[Env, A]) => fa.run(s) |*| fb.run(s)

  // Operations

  inline def length(genType: vec2): Float =
    Math.sqrt(Math.pow(genType.x, 2.0f) + Math.pow(genType.y, 2.0f)).toFloat

  inline def step(edge: Float, x: Float): Float =
    if x < edge then 0.0f else 1.0f

  // Primitives

  // TODO: Generate primitives with swizzle ops and all operator permutations

  final case class vec2(x: Float, y: Float):
    def -(f: Float): vec2 = vec2(x - f, y - f)
  object vec2:
    inline def apply(xy: Float): vec2 =
      vec2(xy, xy)

  final case class vec3(x: Float, y: Float, z: Float):
    def *(f: Float): vec3 = vec3(x * f, y * f, z * f)
  object vec3:

    inline def apply(xyz: Float): vec3 =
      vec3(xyz, xyz, xyz)

    inline def apply(x: Float, yz: vec2): vec3 =
      vec3(x, yz.x, yz.y)

    inline def apply(xy: vec2, z: Float): vec3 =
      vec3(xy.x, xy.y, z)

  final case class vec4(x: Float, y: Float, z: Float, w: Float)
  object vec4:

    inline def apply(xyz: Float): vec4 =
      vec4(xyz, xyz, xyz, xyz)

    inline def apply(xy: vec2, zw: vec2): vec4 =
      vec4(xy.x, xy.y, zw.x, zw.y)

    inline def apply(x: Float, y: Float, zw: vec2): vec4 =
      vec4(x, y, zw.x, zw.y)

    inline def apply(xy: vec2, z: Float, w: Float): vec4 =
      vec4(xy.x, xy.y, z, w)

    inline def apply(x: Float, yzw: vec3): vec4 =
      vec4(x, yzw.x, yzw.y, yzw.z)

    inline def apply(xyz: vec3, w: Float): vec4 =
      vec4(xyz.x, xyz.y, xyz.z, w)

  final case class rgba(r: Float, g: Float, b: Float, a: Float):
    def rgb: vec3 = vec3(r, g, b)
  object rgba:

    inline def apply(rgb: Float): rgba =
      rgba(rgb, rgb, rgb, rgb)

    inline def apply(rg: vec2, ba: vec2): rgba =
      rgba(rg.x, rg.y, ba.x, ba.y)

    inline def apply(r: Float, g: Float, ba: vec2): rgba =
      rgba(r, g, ba.x, ba.y)

    inline def apply(rg: vec2, b: Float, a: Float): rgba =
      rgba(rg.x, rg.y, b, a)

    inline def apply(r: Float, gba: vec3): rgba =
      rgba(r, gba.x, gba.y, gba.z)

    inline def apply(rgb: vec3, a: Float): rgba =
      rgba(rgb.x, rgb.y, rgb.z, a)

    inline def apply(v4: vec4): rgba =
      rgba(v4.x, v4.y, v4.z, v4.y)

end ShaderDSL
