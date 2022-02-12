package indigo.shared.time

opaque type FPS = Int

object FPS:
  val `30`: FPS    = FPS(30)
  val `60`: FPS    = FPS(60)
  val Default: FPS = `60`

  inline def apply(fps: Int): FPS = fps

  extension (fps: FPS)
    def toInt: Int         = fps
    def toLong: Long       = fps.toLong
    def toFloat: Float     = fps.toFloat
    def toDouble: Double   = fps.toDouble
    def toSeconds: Seconds = Seconds(1.0d / fps.toDouble)
    def toMillis: Millis   = toSeconds.toMillis

  given CanEqual[FPS, FPS] = CanEqual.derived
