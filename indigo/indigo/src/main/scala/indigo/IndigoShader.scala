package indigo

import indigo.entry.StandardFrameProcessor
import indigo.gameengine.GameEngine
import indigo.shared.shader.UniformBlock
import indigo.shared.shader.library
import indigo.shared.shader.library.IndigoUV.BlendFragmentEnvReference
import indigo.shared.subsystems.SubSystemsRegister
import org.scalajs.dom.Element
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.*

import scala.annotation.nowarn
import scala.concurrent.Future

/** A trait representing a shader that fills the available window.
  */
trait IndigoShader extends GameLauncher[IndigoShaderModel, IndigoShaderModel, Unit] {

  given [A](using toUBO: ToUniformBlock[A]): Conversion[A, UniformBlock] with
    def apply(value: A): UniformBlock = toUBO.toUniformBlock(value)

  private val Channel0Name: String = "channel0"
  private val Channel1Name: String = "channel1"
  private val Channel2Name: String = "channel2"
  private val Channel3Name: String = "channel3"

  /** Your shader's default configuration settings, values like the viewport size can be overriden with flags.
    */
  def config: GameConfig

  /** A fixed set of assets that will be loaded before the game starts, typically for loading an external shader file.
    */
  def assets: Set[AssetType]

  /** An optional path to an image asset you would like to be mapped to channel 0 for your shader to use.
    */
  def channel0: Option[AssetPath]

  /** An optional path to an image asset you would like to be mapped to channel 1 for your shader to use.
    */
  def channel1: Option[AssetPath]

  /** An optional path to an image asset you would like to be mapped to channel 2 for your shader to use.
    */
  def channel2: Option[AssetPath]

  /** An optional path to an image asset you would like to be mapped to channel 3 for your shader to use.
    */
  def channel3: Option[AssetPath]

  /** The uniform blocks (data) you want to pass to your shader. Example:
    *
    * ```scala
    * import indigo.*
    * import indigo.syntax.shaders.*
    * import ultraviolet.syntax.*
    *
    * final case class CustomData(color: vec4, customTime: Float) extends FragmentEnvReference derives ToUniformBlock
    * def uniformBlocks: Batch[UniformBlock] = Batch(CustomData(RGBA.Magenta.toUVVec4, 0.seconds.toFloat))
    * ```
    *
    * As long as the field types in your case class are ultraviolet types, you can pass them to your shader, see
    * Ultraviolet docs for more info.
    *
    * Many standard Indigo types are supported for the data fields, but you will need a separate case class for the
    * Shader side of the data contract definition, i.e. This is valid too:
    *
    * ```scala
    * // For use with Indigo's shader setup. Note: derives ToUniformBlock, but doesn't need to extend FragmentEnvReference
    * final case class CustomDataIndigo(color: RGBA, customTime: Seconds) derives ToUniformBlock
    *
    * // For use with Ultraviolet's UBO definitions. Note extends FragmentEnvReference, but doesn't derive ToUniformBlock
    * final case class CustomDataUV(color: vec4, customTime: Float) extends FragmentEnvReference
    * ```
    */
  def uniformBlocks: Batch[UniformBlock]

  /** The shader you want to render
    */
  def shader: ShaderProgram

  private def boot(flags: Map[String, String]): Outcome[BootResult[IndigoShaderModel, IndigoShaderModel]] =
    val width  = flags.get("width").map(_.toInt).getOrElse(config.viewport.width)
    val height = flags.get("height").map(_.toInt).getOrElse(config.viewport.height)
    val c0     = flags.get(Channel0Name).map(p => AssetPath(p)).orElse(channel0)
    val c1     = flags.get(Channel1Name).map(p => AssetPath(p)).orElse(channel1)
    val c2     = flags.get(Channel2Name).map(p => AssetPath(p)).orElse(channel2)
    val c3     = flags.get(Channel3Name).map(p => AssetPath(p)).orElse(channel3)

    val channelAssets: Set[AssetType] =
      (c0.toSet.map(Channel0Name -> _) ++
        c1.toSet.map(Channel1Name -> _) ++
        c2.toSet.map(Channel2Name -> _) ++
        c3.toSet.map(Channel3Name -> _)).map { case (channel, path) =>
        AssetType.Image(AssetName(channel), path)
      }

    val configWithOverrides =
      config
        .withViewport(width, height)
        .modifyAdvancedSettings(
          _.withAutoLoadStandardShaders(false)
        )

    val bootData =
      IndigoShaderModel(
        Size(width, height),
        c0.map(_ => AssetName(Channel0Name)),
        c1.map(_ => AssetName(Channel1Name)),
        c2.map(_ => AssetName(Channel2Name)),
        c3.map(_ => AssetName(Channel3Name))
      )

    Outcome(
      BootResult(
        configWithOverrides,
        bootData
      )
        .withShaders(
          shader,
          SceneBlendShader.shader
        )
        .withAssets(assets ++ channelAssets)
    )

  private def setup(
      bootData: IndigoShaderModel
  ): Outcome[Startup[IndigoShaderModel]] =
    Outcome(
      Startup.Success(
        bootData
      )
    )

  private def initialModel(startupData: IndigoShaderModel): Outcome[IndigoShaderModel] =
    Outcome(startupData)

  private def updateModel(
      model: IndigoShaderModel
  ): GlobalEvent => Outcome[IndigoShaderModel] = {
    case ViewportResize(vp) =>
      Outcome(model.copy(viewport = vp.size))

    case KeyboardEvent.KeyUp(Key.KEY_F) =>
      Outcome(model, Batch(ToggleFullScreen))

    case _ =>
      Outcome(model)
  }

  private def present(
      model: IndigoShaderModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Layer(
          BlankEntity(
            model.viewport,
            ShaderData(
              shader.id,
              uniformBlocks,
              model.channel0,
              model.channel1,
              model.channel2,
              model.channel3
            )
          )
        ).withBlendMaterial(SceneBlendShader.material)
      )
    )

  private def indigoGame(
      boot: BootResult[IndigoShaderModel, IndigoShaderModel]
  ): GameEngine[IndigoShaderModel, IndigoShaderModel, Unit] = {

    val updateViewModel: (Context[IndigoShaderModel], IndigoShaderModel, Unit) => GlobalEvent => Outcome[Unit] =
      (_, _, vm) => _ => Outcome(vm)

    val eventFilters: EventFilters =
      EventFilters(
        modelFilter = {
          case e: ViewportResize =>
            Some(e)

          case e @ KeyboardEvent.KeyUp(Key.KEY_F) =>
            Some(e)

          case _ =>
            None
        },
        viewModelFilter = { case _ =>
          None
        }
      )

    val frameProcessor: StandardFrameProcessor[IndigoShaderModel, IndigoShaderModel, Unit] =
      new StandardFrameProcessor(
        new SubSystemsRegister(),
        eventFilters,
        (_, m) => (e: GlobalEvent) => updateModel(m)(e),
        updateViewModel,
        (_, m, _) => present(m)
      )

    new GameEngine[IndigoShaderModel, IndigoShaderModel, Unit](
      Set(),
      Set(),
      boot.shaders,
      (_: AssetCollection) => (_: Dice) => setup(boot.bootData),
      (sd: IndigoShaderModel) => initialModel(sd),
      (_: IndigoShaderModel) => (_: IndigoShaderModel) => Outcome(()),
      frameProcessor,
      Batch.empty
    )
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  protected def ready(
      flags: Map[String, String]
  ): Element => GameEngine[IndigoShaderModel, IndigoShaderModel, Unit] =
    parentElement =>
      boot(flags) match
        case oe @ Outcome.Error(e, _) =>
          IndigoLogger.error("Error during boot - Halting")
          IndigoLogger.error(oe.reportCrash)
          throw e

        case Outcome.Result(b, evts) =>
          indigoGame(b).start(parentElement, b.gameConfig, Future(None), b.assets, Future(Set()), evts)

}

final case class IndigoShaderModel(
    viewport: Size,
    channel0: Option[AssetName],
    channel1: Option[AssetName],
    channel2: Option[AssetName],
    channel3: Option[AssetName]
)

object SceneBlendShader:

  val shader: UltravioletShader =
    UltravioletShader(
      ShaderId("[indigo_engine_shader_blend]"),
      BlendShader.vertex(library.NoOp.vertex, ()),
      BlendShader.fragment(
        fragment,
        Env.reference
      )
    )

  import ultraviolet.syntax.*

  trait Env extends BlendFragmentEnvReference
  object Env:
    val reference: Env = new Env {}

  @nowarn
  inline def fragment =
    Shader[Env] { env =>
      def fragment(color: vec4): vec4 =
        env.SRC
    }

  val material: BlendMaterial =
    new BlendMaterial:
      def toShaderData: ShaderData =
        ShaderData(shader.id)
