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
  */
trait IndigoShader extends GameLauncher[IndigoShaderModel, IndigoShaderModel, Unit] {

  /** Your shader's default configuration settings, values like the viewport size can be overriden with flags.
    */
  val config: GameConfig

  /** A fixed set of assets that will be loaded before the game starts
    */
  val assets: Set[AssetType]
  // TODO: What if they want to load images here?!?

  /** The shader you want to render
    */
  val shader: Shader

  private def boot(flags: Map[String, String]): Outcome[BootResult[IndigoShaderModel]] =
    val width    = flags.get("width").map(_.toInt).getOrElse(config.viewport.width)
    val height   = flags.get("height").map(_.toInt).getOrElse(config.viewport.height)
    val channel0 = flags.get("channel0")
    val channel1 = flags.get("channel1")
    val channel2 = flags.get("channel2")
    val channel3 = flags.get("channel3")

    val channelAssets: Set[AssetType] =
      (channel0.toSet.map("channel0"  -> _) ++
        channel1.toSet.map("channel1" -> _) ++
        channel2.toSet.map("channel2" -> _) ++
        channel3.toSet.map("channel3" -> _)).map { case (channel, path) =>
        AssetType.Image(AssetName(channel), AssetPath(path))
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
        channel0.map(_ => AssetName("channel0")),
        channel1.map(_ => AssetName("channel1")),
        channel2.map(_ => AssetName("channel2")),
        channel3.map(_ => AssetName("channel3"))
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
