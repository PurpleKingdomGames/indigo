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
import org.scalajs.dom.Screen

// TODO: Add documentation
final class Context[StartUpData](
    _startUpData: => StartUpData,
    val frame: Context.Frame,
    val services: Context.Services
):
  lazy val startUpData = _startUpData

  def withStartUpData(newStartUpData: StartUpData): Context[StartUpData] =
    new Context(newStartUpData, frame, services)
  def modifyStartUpData(modify: StartUpData => StartUpData): Context[StartUpData] =
    withStartUpData(modify(startUpData))

  def withFrame(newFrame: Context.Frame): Context[StartUpData] =
    new Context(startUpData, newFrame, services)
  def modifyFrame(modify: Context.Frame => Context.Frame): Context[StartUpData] =
    withFrame(modify(frame))

  def withServices(newServices: Context.Services): Context[StartUpData] =
    new Context(startUpData, frame, newServices)
  def modifyServices(modify: Context.Services => Context.Services): Context[StartUpData] =
    withServices(modify(services))

object Context:

  def initial: Context[Unit] =
    new Context((), Frame.initial, Services.noop)

  def apply(frame: Frame): Context[Unit] =
    new Context((), frame, Services.noop)

  def apply(services: Services): Context[Unit] =
    new Context((), Frame.initial, services)

  def apply(frame: Frame, services: Services): Context[Unit] =
    new Context((), frame, services)

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

    def withDice(newDice: Dice): Frame =
      new Frame(newDice, time, input)

    def withTime(newTime: GameTime): Frame =
      new Frame(dice, newTime, input)

    def withInput(newInput: InputState): Frame =
      new Frame(dice, time, newInput)

  object Frame:
    def apply(dice: Dice, time: GameTime, input: InputState): Frame =
      new Frame(dice, time, input)

    val initial: Frame =
      new Frame(Dice.fromSeed(0), GameTime.zero, InputState.default)

  trait Services:
    def bounds: Services.Bounds
    def random: Services.Random
    def screen: Services.Screen

  object Services:

    def apply(
        boundaryLocator: BoundaryLocator,
        _random: scala.util.Random,
        _captureScreen: Batch[ScreenCaptureConfig] => Batch[Either[String, AssetType.Image]]
    ): Services =
      new Services:
        def bounds: Services.Bounds = Bounds(boundaryLocator)
        def random: Services.Random = Random(_random)
        def screen: Services.Screen = Screen(_captureScreen)

    def noop: Services =
      new Services:
        def bounds: Bounds = Bounds.noop
        def random: Random = Random.noop
        def screen: Screen = Screen.noop

    trait Bounds:
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

    object Bounds:
      def apply(boundaryLocator: BoundaryLocator): Bounds =
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

      def noop: Bounds =
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

    trait Random:
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

    object Random:

      def apply(_random: scala.util.Random): Random =
        new Random:
          def nextBoolean: Boolean                                        = _random.nextBoolean()
          def nextDouble: Double                                          = _random.nextDouble()
          def between(minInclusive: Double, maxExclusive: Double): Double = _random.between(minInclusive, maxExclusive)
          def nextFloat: Float                                            = _random.nextFloat()
          def between(minInclusive: Float, maxExclusive: Float): Float    = _random.between(minInclusive, maxExclusive)
          def nextInt: Int                                                = _random.nextInt()
          def nextInt(n: Int): Int                                        = _random.nextInt(n)
          def between(minInclusive: Int, maxExclusive: Int): Int          = _random.between(minInclusive, maxExclusive)
          def nextLong: Long                                              = _random.nextLong()
          def nextLong(n: Long): Long                                     = _random.nextLong(n)
          def between(minInclusive: Long, maxExclusive: Long): Long       = _random.between(minInclusive, maxExclusive)
          def nextString(length: Int): String                             = _random.nextString(length)
          def nextPrintableChar(): Char                                   = _random.nextPrintableChar()
          def setSeed(seed: Long): Unit                                   = _random.setSeed(seed)
          def shuffle[T](xs: List[T]): List[T]                            = _random.shuffle(xs)
          def alphanumeric(take: Int): List[Char]                         = _random.alphanumeric.take(take).toList

      val noop: Random =
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

    trait Screen:
      /** Capture the screen as a number of images, each with the specified configuration
        *
        * @param captureConfig
        *   The configurations to use when capturing the screen
        * @return
        *   A batch containing either the captured images, or error messages
        */
      def capture(captureConfig: Batch[ScreenCaptureConfig]): Batch[Either[String, AssetType.Image]]

      /** Capture the screen as an image, with the specified configuration
        *
        * @param captureConfig
        *   The configuration to use when capturing the screen
        * @return
        *   The captured image, or an error message
        */
      def capture(captureConfig: ScreenCaptureConfig): Either[String, AssetType.Image]

    object Screen:

      def apply(
          _captureScreen: Batch[ScreenCaptureConfig] => Batch[Either[String, AssetType.Image]]
      ): Screen =
        new Screen:
          def capture(captureConfig: Batch[ScreenCaptureConfig]): Batch[Either[String, AssetType.Image]] =
            _captureScreen(captureConfig)

          def capture(captureConfig: ScreenCaptureConfig): Either[String, AssetType.Image] =
            capture(Batch(captureConfig)).headOption match {
              case Some(v) => v
              case None    => Left("Could not capture image")
            }

      val noop: Screen =
        new Screen:
          def capture(captureConfig: Batch[ScreenCaptureConfig]): Batch[Either[String, AssetType.Image]] =
            Batch.empty
          def capture(captureConfig: ScreenCaptureConfig): Either[String, AssetType.Image] =
            Left("Screen capture not supported in noop implementation")
