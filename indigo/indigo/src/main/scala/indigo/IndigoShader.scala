package indigo

import indigo.*
import indigo.entry.StandardFrameProcessor
import indigo.gameengine.GameEngine
import indigo.shared.shader.library
import indigo.shared.shader.library.IndigoUV.BlendFragmentEnvReference
import indigo.shared.subsystems.SubSystemsRegister
import org.scalajs.dom.Element
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._

import scala.concurrent.Future

/** A trait representing a shader that fills the available window.
  *
  * You can override a number of the details in this trait using launch flags, including:
  *
  *   - width - starting width of the shader
  *   - height - starting height of the shader
  *   - channel0 - path to an image
  *   - channel1 - path to an image
  *   - channel2 - path to an image
  *   - channel3 - path to an image
  */
trait IndigoShader extends GameLauncher[IndigoShaderModel, IndigoShaderModel, Unit] {

  private val Channel0Name: String = "channel0"
  private val Channel1Name: String = "channel1"
  private val Channel2Name: String = "channel2"
  private val Channel3Name: String = "channel3"

  /** Your shader's default configuration settings, values like the viewport size can be overriden with flags.
    */
  val config: GameConfig

  /** A fixed set of assets that will be loaded before the game starts, typically for loading an external shader file.
    */
  val assets: Set[AssetType]

  /** An optional path to an image asset you would like to be mapped to channel 0 for your shader to use.
    */
  val channel0: Option[AssetPath]

  /** An optional path to an image asset you would like to be mapped to channel 1 for your shader to use.
    */
  val channel1: Option[AssetPath]

  /** An optional path to an image asset you would like to be mapped to channel 2 for your shader to use.
    */
  val channel2: Option[AssetPath]

  /** An optional path to an image asset you would like to be mapped to channel 3 for your shader to use.
    */
  val channel3: Option[AssetPath]

  /** The shader you want to render
    */
  val shader: Shader

  private def boot(flags: Map[String, String]): Outcome[BootResult[IndigoShaderModel]] =
    val width  = flags.get("width").map(_.toInt).getOrElse(config.viewport.width)
    val height = flags.get("height").map(_.toInt).getOrElse(config.viewport.height)
    val c0     = flags.get(Channel0Name).map(p => AssetPath(p)).orElse(channel0)
    val c1     = flags.get(Channel1Name).map(p => AssetPath(p)).orElse(channel1)
    val c2     = flags.get(Channel2Name).map(p => AssetPath(p)).orElse(channel2)
    val c3     = flags.get(Channel3Name).map(p => AssetPath(p)).orElse(channel3)

    val channelAssets: Set[AssetType] =
      (c0.toSet.map(Channel0Name  -> _) ++
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
      bootData: IndigoShaderModel,
      assetCollection: AssetCollection,
      dice: Dice
  ): Outcome[Startup[IndigoShaderModel]] =
    Outcome(
      Startup.Success(
        bootData
      )
    )

  private def initialModel(startupData: IndigoShaderModel): Outcome[IndigoShaderModel] =
    Outcome(startupData)

  private def updateModel(
      context: FrameContext[IndigoShaderModel],
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
      context: FrameContext[IndigoShaderModel],
      model: IndigoShaderModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Layer(
          BlankEntity(
            model.viewport,
            ShaderData(
              shader.id,
              Batch.empty,
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
      boot: BootResult[IndigoShaderModel]
  ): GameEngine[IndigoShaderModel, IndigoShaderModel, Unit] = {

    val updateViewModel: (FrameContext[IndigoShaderModel], IndigoShaderModel, Unit) => GlobalEvent => Outcome[Unit] =
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
        (ctx, m) => (e: GlobalEvent) => updateModel(ctx, m)(e),
        updateViewModel,
        (ctx, m, _) => present(ctx, m)
      )

    new GameEngine[IndigoShaderModel, IndigoShaderModel, Unit](
      Set(),
      Set(),
      boot.shaders,
      (ac: AssetCollection) => (d: Dice) => setup(boot.bootData, ac, d),
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

  inline def fragment =
    Shader[Env] { env =>
      def fragment(color: vec4): vec4 =
        env.SRC
    }

  val material: BlendMaterial =
    new BlendMaterial:
      def toShaderData: BlendShaderData =
        BlendShaderData(shader.id)
