package indigo

import indigo._
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
trait IndigoShader extends GameLauncher[IndigoShaderBootData, IndigoShaderModel, Unit] {

  /** Your shader's default configuration settings, values like the viewport size can be overriden with flags.
    */
  val config: GameConfig

  /** A fixed set of assets that will be loaded before the game starts
    */
  val assets: Set[AssetType]

  /** The shader you want to render
    */
  val shader: Shader

  // TODO: Accept asset path, shader details?
  // TODO: Optionally show FPS?
  private def boot(flags: Map[String, String]): Outcome[BootResult[IndigoShaderBootData]] =
    val width  = flags.get("width").map(_.toInt).getOrElse(config.viewport.width)
    val height = flags.get("height").map(_.toInt).getOrElse(config.viewport.height)

    val configWithOverrides =
      config
        .withViewport(width, height)
        .modifyAdvancedSettings(
          _.withAutoLoadStandardShaders(false)
        )

    val bootData =
      IndigoShaderBootData(Size(width, height))

    Outcome(
      BootResult(
        configWithOverrides,
        bootData
      )
        .withShaders(
          shader,
          SceneBlendShader.shader
        )
        .withAssets(assets)
    )

  private def setup(
      bootData: IndigoShaderBootData,
      assetCollection: AssetCollection,
      dice: Dice
  ): Outcome[Startup[IndigoShaderBootData]] =
    Outcome(
      Startup.Success(
        bootData
      )
    )

  private def initialModel(startupData: IndigoShaderBootData): Outcome[IndigoShaderModel] =
    Outcome(IndigoShaderModel(startupData.viewport))

  private def updateModel(
      context: FrameContext[IndigoShaderBootData],
      model: IndigoShaderModel
  ): GlobalEvent => Outcome[IndigoShaderModel] = {
    case ViewportResize(vp) =>
      Outcome(model.copy(viewport = vp.size))

    case KeyboardEvent.KeyUp(Key.KEY_F) =>
      Outcome(model, Batch(ToggleFullScreen))

    case _ =>
      Outcome(model)
  }

  lazy val shaderData: ShaderData = ShaderData(shader.id)

  private def present(
      context: FrameContext[IndigoShaderBootData],
      model: IndigoShaderModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Layer(
          BlankEntity(model.viewport, shaderData)
        ).withBlendMaterial(SceneBlendShader.material)
      )
    )

  private def indigoGame(
      boot: BootResult[IndigoShaderBootData]
  ): GameEngine[IndigoShaderBootData, IndigoShaderModel, Unit] = {

    val updateViewModel: (FrameContext[IndigoShaderBootData], IndigoShaderModel, Unit) => GlobalEvent => Outcome[Unit] =
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

    val frameProcessor: StandardFrameProcessor[IndigoShaderBootData, IndigoShaderModel, Unit] =
      new StandardFrameProcessor(
        new SubSystemsRegister(),
        eventFilters,
        (ctx, m) => (e: GlobalEvent) => updateModel(ctx, m)(e),
        updateViewModel,
        (ctx, m, _) => present(ctx, m)
      )

    new GameEngine[IndigoShaderBootData, IndigoShaderModel, Unit](
      Set(),
      Set(),
      boot.shaders,
      (ac: AssetCollection) => (d: Dice) => setup(boot.bootData, ac, d),
      (sd: IndigoShaderBootData) => initialModel(sd),
      (_: IndigoShaderBootData) => (_: IndigoShaderModel) => Outcome(()),
      frameProcessor,
      Batch.empty
    )
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  protected def ready(
      flags: Map[String, String]
  ): Element => GameEngine[IndigoShaderBootData, IndigoShaderModel, Unit] =
    parentElement =>
      boot(flags) match
        case oe @ Outcome.Error(e, _) =>
          IndigoLogger.error("Error during boot - Halting")
          IndigoLogger.error(oe.reportCrash)
          throw e

        case Outcome.Result(b, evts) =>
          indigoGame(b).start(parentElement, b.gameConfig, Future(None), b.assets, Future(Set()), evts)

}

final case class IndigoShaderBootData(viewport: Size)
final case class IndigoShaderModel(viewport: Size)

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
