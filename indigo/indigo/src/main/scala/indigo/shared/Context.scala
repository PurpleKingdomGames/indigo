package indigo.shared

import indigo.FontKey
import indigo.platform.renderer.ScreenCaptureConfig
import indigo.shared.assets.AssetType
import indigo.shared.collections.Batch
import indigo.shared.datatypes.Rectangle
import indigo.shared.dice.Dice
import indigo.shared.events.InputState
import indigo.shared.scenegraph.SceneNode
import indigo.shared.scenegraph.TextBox
import indigo.shared.scenegraph.TextLine
import indigo.shared.time.GameTime

final class Context[StartUpData](
    _startUpData: => StartUpData,
    val frame: Context.Frame,
    val services: Context.Services
):
  lazy val startUpData = _startUpData

object Context:

  def apply[StartUpData](
      gameTime: GameTime,
      dice: Dice,
      inputState: InputState,
      boundaryLocator: BoundaryLocator,
      startUpData: StartUpData,
      _captureScreen: Batch[ScreenCaptureConfig] => Batch[Either[String, AssetType.Image]]
  ): Context[StartUpData] =
    new Context(
      startUpData,
      Frame(
        dice,
        gameTime,
        inputState
      ),
      Services(
        boundaryLocator,
        scala.util.Random(),
        _captureScreen
      )
    )

  final class Frame(
      val dice: Dice,
      val time: GameTime,
      val input: InputState
  ):
    export time.running
    export time.delta
    export input.mouse
    export input.keyboard
    export input.gamepad
    export input.pointers

  trait Services:
    def bounds: Services.Bounds
    def random: Services.Random
    def screenCapture: Services.ScreenCapture

  object Services:

    def apply(
        boundaryLocator: BoundaryLocator,
        _random: scala.util.Random,
        _captureScreen: Batch[ScreenCaptureConfig] => Batch[Either[String, AssetType.Image]]
    ): Services =
      new Services:
        def bounds: Services.Bounds =
          new Bounds:
            def measureText(textBox: TextBox): Rectangle      = boundaryLocator.measureText(textBox)
            def find(sceneNode: SceneNode): Option[Rectangle] = boundaryLocator.findBounds(sceneNode)
            def get(sceneNode: SceneNode): Rectangle          = boundaryLocator.bounds(sceneNode)
            def textAsLinesWithBounds(
                text: String,
                fontKey: FontKey,
                letterSpacing: Int,
                lineHeight: Int
            ): Batch[TextLine] =
              boundaryLocator.textAsLinesWithBounds(text, fontKey, letterSpacing, lineHeight)

        def random: Services.Random =
          new Random:
            def nextBoolean: Boolean = _random.nextBoolean()
            def nextDouble: Double   = _random.nextDouble()
            def between(minInclusive: Double, maxExclusive: Double): Double =
              _random.between(minInclusive, maxExclusive)
            def nextFloat: Float                                         = _random.nextFloat()
            def between(minInclusive: Float, maxExclusive: Float): Float = _random.between(minInclusive, maxExclusive)
            def nextInt: Int                                             = _random.nextInt()
            def nextInt(n: Int): Int                                     = _random.nextInt(n)
            def between(minInclusive: Int, maxExclusive: Int): Int       = _random.between(minInclusive, maxExclusive)
            def nextLong: Long                                           = _random.nextLong()
            def nextLong(n: Long): Long                                  = _random.nextLong(n)
            def between(minInclusive: Long, maxExclusive: Long): Long    = _random.between(minInclusive, maxExclusive)
            def nextString(length: Int): String                          = _random.nextString(length)
            def nextPrintableChar(): Char                                = _random.nextPrintableChar()
            def setSeed(seed: Long): Unit                                = _random.setSeed(seed)
            def shuffle[T](xs: List[T]): List[T]                         = _random.shuffle(xs)
            def alphanumeric(take: Int): List[Char]                      = _random.alphanumeric.take(take).toList

        def screenCapture: Services.ScreenCapture =
          new ScreenCapture:
            def captureScreen(captureConfig: Batch[ScreenCaptureConfig]): Batch[Either[String, AssetType.Image]] =
              _captureScreen(captureConfig)

            def captureScreen(captureConfig: ScreenCaptureConfig): Either[String, AssetType.Image] =
              captureScreen(Batch(captureConfig)).headOption match {
                case Some(v) => v
                case None    => Left("Could not capture image")
              }

    def noop: Services =
      new Services:
        def bounds: Bounds =
          new Bounds:
            def measureText(textBox: TextBox): Rectangle      = Rectangle.zero
            def find(sceneNode: SceneNode): Option[Rectangle] = None
            def get(sceneNode: SceneNode): Rectangle          = Rectangle.zero
            def textAsLinesWithBounds(
                text: String,
                fontKey: FontKey,
                letterSpacing: Int,
                lineHeight: Int
            ): Batch[TextLine] =
              Batch.empty

        def random: Random =
          new Random:
            def nextBoolean: Boolean                                        = false
            def nextDouble: Double                                          = 0.0
            def between(minInclusive: Double, maxExclusive: Double): Double = 0.0
            def nextFloat: Float                                            = 0.0f
            def between(minInclusive: Float, maxExclusive: Float): Float    = 0.0f
            def nextInt: Int                                                = 0
            def nextInt(n: Int): Int                                        = 0
            def between(minInclusive: Int, maxExclusive: Int): Int          = 0
            def nextLong: Long                                              = 0L
            def nextLong(n: Long): Long                                     = 0L
            def between(minInclusive: Long, maxExclusive: Long): Long       = 0L
            def nextString(length: Int): String                             = ""
            def nextPrintableChar(): Char                                   = ' '
            def setSeed(seed: Long): Unit                                   = ()
            def shuffle[T](xs: List[T]): List[T]                            = xs
            def alphanumeric(take: Int): List[Char]                         = List.fill(take)(' ')

        def screenCapture: ScreenCapture =
          new ScreenCapture:
            def captureScreen(captureConfig: Batch[ScreenCaptureConfig]): Batch[Either[String, AssetType.Image]] =
              Batch.empty
            def captureScreen(captureConfig: ScreenCaptureConfig): Either[String, AssetType.Image] = Left(
              "Screen capture not supported in noop implementation"
            )

    trait Bounds {
      def measureText(textBox: TextBox): Rectangle

      /** Safely finds the bounds of any given scene node, if the node has bounds. It is not possible to sensibly
        * measure the bounds of some node types, such as clones, and some nodes are dependant on external data that may
        * be missing.
        */
      def find(sceneNode: SceneNode): Option[Rectangle]

      /** Finds the bounds or returns a `Rectangle` of size zero for convenience.
        */
      def get(sceneNode: SceneNode): Rectangle

      def textAsLinesWithBounds(text: String, fontKey: FontKey, letterSpacing: Int, lineHeight: Int): Batch[TextLine]
    }
    trait Random {
      def nextBoolean: Boolean
      def nextDouble: Double
      def between(minInclusive: Double, maxExclusive: Double): Double
      def nextFloat: Float
      def between(minInclusive: Float, maxExclusive: Float): Float
      def nextInt: Int
      def nextInt(n: Int): Int
      def between(minInclusive: Int, maxExclusive: Int): Int
      def nextLong: Long
      def nextLong(n: Long): Long
      def between(minInclusive: Long, maxExclusive: Long): Long
      def nextString(length: Int): String
      def nextPrintableChar(): Char
      def setSeed(seed: Long): Unit
      def shuffle[T](xs: List[T]): List[T]
      def alphanumeric(take: Int): List[Char]
    }
    trait ScreenCapture {

      /** Capture the screen as a number of images, each with the specified configuration
        *
        * @param captureConfig
        *   The configurations to use when capturing the screen
        * @return
        *   A batch containing either the captured images, or error messages
        */
      def captureScreen(captureConfig: Batch[ScreenCaptureConfig]): Batch[Either[String, AssetType.Image]]

      /** Capture the screen as an image, with the specified configuration
        *
        * @param captureConfig
        *   The configuration to use when capturing the screen
        * @return
        *   The captured image, or an error message
        */
      def captureScreen(captureConfig: ScreenCaptureConfig): Either[String, AssetType.Image]
    }
