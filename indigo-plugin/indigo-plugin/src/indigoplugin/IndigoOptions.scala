package indigoplugin

/** Represents the various options that go into an Indigo game build.
  *
  * @param metadata
  *   Metadata about your game, such as the title.
  * @param assets
  *   Represents you game's assets processing.
  * @param electron
  *   Represents options specific to Electron builds of your game.
  */
final case class IndigoOptions(
    metadata: IndigoGameMetadata,
    assets: IndigoAssets,
    electron: IndigoElectronOptions,
    template: IndigoTemplate
) {

  /** Provide a replacement IndigoGameMetadata instance */
  def withGameMetadata(newMetadata: IndigoGameMetadata): IndigoOptions =
    this.copy(metadata = newMetadata)

  /** Modify the existing IndigoGameMetadata */
  def modifyGameMetadata(modify: IndigoGameMetadata => IndigoGameMetadata): IndigoOptions =
    this.copy(metadata = modify(metadata))

  /** Sets a new title for your game's window / title bar / tab */
  def withTitle(newTitle: String): IndigoOptions =
    this.copy(metadata = metadata.withTitle(newTitle))

  /** Make the cursor visible */
  def cursorVisible: IndigoOptions =
    this.copy(metadata = metadata.cursorVisible)

  /** Hide the cursor */
  def cursorHidden: IndigoOptions =
    this.copy(metadata = metadata.cursorHidden)

  /** Sets the background color, any valid CSS color representation acceptable, e.g. 'black' or '#000000' */
  def withBackgroundColor(cssColorValue: String): IndigoOptions =
    this.copy(metadata = metadata.withBackgroundColor(cssColorValue))

  /** Set the background color from RGBA values */
  def withBackgroundColor(r: Double, g: Double, b: Double, a: Double): IndigoOptions =
    this.copy(metadata = metadata.withBackgroundColor(r, g, b, a))

  /** Set the background color from RGB values */
  def withBackgroundColor(r: Double, g: Double, b: Double): IndigoOptions =
    this.copy(metadata = metadata.withBackgroundColor(r, g, b))

  /** Provide a replacement IndigoAssets instance */
  def withAssets(newAssets: IndigoAssets): IndigoOptions =
    this.copy(assets = newAssets)

  /** Sets the asset directory path */
  def withAssetDirectory(path: String): IndigoOptions =
    this.copy(assets = assets.withAssetDirectory(path))
  def withAssetDirectory(path: os.RelPath): IndigoOptions =
    this.copy(assets = assets.withAssetDirectory(path))

  // This is the sbt version, it's encoded differently because otherwise
  // Scala 2.12 sees these as a double definition.
  /** Filter to explicitly include matching assets.
    *
    * Decision order is to include is there is a rule, then exclude if there is a rule, and otherwise include by
    * default.
    */
  def includeAssetPaths(rules: PartialFunction[String, Boolean]): IndigoOptions = {
    val default: PartialFunction[String, Boolean] = { case _ => false }
    val pf: os.RelPath => Boolean                 = (r: os.RelPath) => rules.orElse(default)(r.toString())
    this.copy(assets = assets.withInclude(pf))
  }

  /** Filter to explicitly include matching assets.
    *
    * Decision order is to include is there is a rule, then exclude if there is a rule, and otherwise include by
    * default.
    */
  def includeAssets(rules: os.RelPath => Boolean): IndigoOptions =
    this.copy(assets = assets.withInclude(rules))

  // This is the sbt version, it's encoded differently because otherwise
  // Scala 2.12 sees these as a double definition.
  /** Filter to explicitly exclude matching assets.
    *
    * Decision order is to include is there is a rule, then exclude if there is a rule, and otherwise include by
    * default.
    */
  def excludeAssetPaths(rules: PartialFunction[String, Boolean]): IndigoOptions = {
    val default: PartialFunction[String, Boolean] = { case _ => false }
    val pf: os.RelPath => Boolean                 = (r: os.RelPath) => rules.orElse(default)(r.toString())
    this.copy(assets = assets.withExclude(pf))
  }

  /** Filter to explicitly exclude matching assets.
    *
    * Decision order is to include is there is a rule, then exclude if there is a rule, and otherwise include by
    * default.
    */
  def excludeAssets(rules: os.RelPath => Boolean): IndigoOptions =
    this.copy(assets = assets.withExclude(rules))

  /** Provide a custom asset renaming function (arguments are (name, ext) => new name) used during asset listing
    * generation to rename assets. The purpose is to avoid name clashes in the generated code where you have two
    * similarly named items that would normally result in identical names, e.g. character.png and character.json.
    * Original file names will not be affected.
    *
    * Assets names are processed by this function before being 'made safe' by the usual process. So be aware that you
    * may still not get the exact name you mapped to.
    */
  def renameAssets(f: PartialFunction[(String, String), String]): IndigoOptions =
    this.copy(assets = assets.withRenameFunction(f))

  /** Set the window start width */
  def withWindowWidth(value: Int): IndigoOptions =
    this.copy(metadata = metadata.withWindowWidth(value))

  /** Set the window start height */
  def withWindowHeight(value: Int): IndigoOptions =
    this.copy(metadata = metadata.withWindowHeight(value))

  /** Set the window start width */
  def withWindowSize(w: Int, h: Int): IndigoOptions =
    this.copy(metadata = metadata.withWindowSize(w, h))

  /** Provide a replacement IndigoElectronOptions instance */
  def withElectronOptions(newElectronOptions: IndigoElectronOptions): IndigoOptions =
    this.copy(electron = newElectronOptions)

  /** Modify the existing IndigoElectronOptions */
  def modifyElectronOptions(modify: IndigoElectronOptions => IndigoElectronOptions): IndigoOptions =
    this.copy(electron = modify(electron))

  /** Electron will limit the frame rate using the default browser refresh rate, typically it will sync with your
    * monitor's refresh rate. It is recommended that you do this, and set your indigo config to limit the framerate too.
    */
  def electronLimitsFrameRate: IndigoOptions =
    this.copy(electron = electron.electronLimitsFrameRate)

  /** Electron will not limit the frame rate. */
  def electronUnlimitedFrameRate: IndigoOptions =
    this.copy(electron = electron.electronUnlimitedFrameRate)

  /** Open the developer tools when the Electron window is created. */
  def electronDeveloperToolsOpenOnStartUp: IndigoOptions =
    this.copy(electron = electron.developerToolsOpenOnStartUp)

  /** Open the developer tools when the Electron window is created. (Alias for developerToolsOpenOnStartUp) */
  def electronOpenDeveloperTools: IndigoOptions =
    electronDeveloperToolsOpenOnStartUp

  /** Do not open the developer tools when the Electron window is created. */
  def electronDeveloperToolsClosedOnStartUp: IndigoOptions =
    this.copy(electron = electron.developerToolsClosedOnStartUp)

  /** Do not open the developer tools when the Electron window is created. (Alias for developerToolsClosedOnStartUp) */
  def electronDoNotOpenDeveloperTools: IndigoOptions =
    electronDeveloperToolsClosedOnStartUp

  /** Sets the electron installation type. It is recommended that, during development at least, you set this to
    * `ElectronInstall.Latest` to take advantage of performance improvements.
    */
  def withElectronInstallType(value: ElectronInstall): IndigoOptions =
    this.copy(electron = electron.withElectronInstallType(value))

  /** Use the latest version of Electron with the `indigoRun` command, which will be installed with NPM. */
  def useLatestElectron: IndigoOptions =
    withElectronInstallType(ElectronInstall.Latest)

  /** Use a globally installed version of Electron with the `indigoRun` command. Global installs of Electron have the
    * advantage of a slightly faster `indigoRun` startup time, however, global Electron installs can be of dubious
    * quality, and suffer from poor performance or limited features. Not recommended.
    */
  def useGlobalElectron: IndigoOptions =
    withElectronInstallType(ElectronInstall.Global)

  /** Use a specific version of Electron, follows normal NPM version formats. */
  def useElectronVersion(version: String): IndigoOptions =
    withElectronInstallType(ElectronInstall.Version(version))

  /** Use an Electron install at the specified path with the `indigoRun` command. */
  def useElectronExecutable(path: String): IndigoOptions =
    withElectronInstallType(ElectronInstall.PathToExecutable(path))

  /** Provide a replacement IndigoTemplate instance */
  def withTemplate(newTemplate: IndigoTemplate): IndigoOptions =
    this.copy(template = newTemplate)

  /** Use the static site template during the game build */
  def useDefaultTemplate: IndigoOptions =
    withTemplate(IndigoTemplate.Default)

  /** Use a custom static site template during the game build, with the given inputs and outputs specifiying important
    * locations.
    */
  def useCustomTemplate(inputs: IndigoTemplate.Inputs, outputs: IndigoTemplate.Outputs): IndigoOptions =
    withTemplate(IndigoTemplate.Custom(inputs, outputs))

}

object IndigoOptions {

  /** Default configuration for an Indigo game. */
  val defaults: IndigoOptions =
    IndigoOptions(
      IndigoGameMetadata.defaults,
      IndigoAssets.defaults,
      IndigoElectronOptions.defaults,
      IndigoTemplate.Default
    )

}
