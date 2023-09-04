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
    electron: IndigoElectronOptions
) {

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
    val pf: os.RelPath => Boolean                 = (r: os.RelPath) => (rules.orElse(default))(r.toString())
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
    val pf: os.RelPath => Boolean                 = (r: os.RelPath) => (rules.orElse(default))(r.toString())
    this.copy(assets = assets.withExclude(pf))
  }

  /** Filter to explicitly exclude matching assets.
    *
    * Decision order is to include is there is a rule, then exclude if there is a rule, and otherwise include by
    * default.
    */
  def excludeAssets(rules: os.RelPath => Boolean): IndigoOptions =
    this.copy(assets = assets.withExclude(rules))

  /** Set the window start width */
  def withWindowWidth(value: Int): IndigoOptions =
    this.copy(metadata = metadata.withWindowWidth(value))

  /** Set the window start height */
  def withWindowHeight(value: Int): IndigoOptions =
    this.copy(metadata = metadata.withWindowHeight(value))

  /** Set the window start width */
  def withWindowSize(w: Int, h: Int): IndigoOptions =
    this.copy(metadata = metadata.withWindowSize(w, h))

  /** Electron will limit the frame rate using the default browser refresh rate, typically it will sync with your
    * monitor's refresh rate. It is recommended that you do this, and set your indigo config to limit the framerate too.
    */
  def electronLimitsFrameRate: IndigoOptions =
    this.copy(electron = electron.electronLimitsFrameRate)

  /** Electron will not limit the frame rate. */
  def electronUnlimitedFrameRate: IndigoOptions =
    this.copy(electron = electron.electronUnlimitedFrameRate)

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

}

object IndigoOptions {

  /** Default configuration for an Indigo game. */
  val defaults: IndigoOptions =
    IndigoOptions(
      metadata = IndigoGameMetadata.defaults,
      assets = IndigoAssets.defaults,
      electron = IndigoElectronOptions.defaults
    )

}

/** Represents the various options that go into an Indigo game build.
  *
  * @param title
  *   Title of your game. Default 'Made with Indigo'.
  * @param showCursor
  *   Show the cursor? Default 'true'.
  * @param backgroundColor
  *   HTML page background color Default 'white'.
  * @param width
  *   Initial window width. Default '550'.
  * @param height
  *   Initial window height. Default '400'.
  */
final case class IndigoGameMetadata(
    title: String,
    showCursor: Boolean,
    backgroundColor: String,
    width: Int,
    height: Int
) {

  /** Sets a new title for your game's window / title bar / tab */
  def withTitle(newTitle: String): IndigoGameMetadata =
    this.copy(title = newTitle)

  /** Make the cursor visible */
  def cursorVisible: IndigoGameMetadata =
    this.copy(showCursor = true)

  /** Hide the cursor */
  def cursorHidden: IndigoGameMetadata =
    this.copy(showCursor = false)

  /** Sets the background color, any valid CSS color representation acceptable, e.g. 'black' or '#000000' */
  def withBackgroundColor(cssColorValue: String): IndigoGameMetadata =
    this.copy(backgroundColor = cssColorValue)

  /** Set the background color from RGBA values */
  def withBackgroundColor(r: Double, g: Double, b: Double, a: Double): IndigoGameMetadata = {
    val convert: Double => String = d => {
      val hex = Integer.toHexString((Math.min(1, Math.max(0, d)) * 255).toInt)
      if (hex.length == 1) "0" + hex else hex
    }
    withBackgroundColor("#" + convert(r) + convert(g) + convert(b) + convert(a))
  }

  /** Set the background color from RGB values */
  def withBackgroundColor(r: Double, g: Double, b: Double): IndigoGameMetadata = {
    val convert: Double => String = d => {
      val hex = Integer.toHexString((Math.min(1, Math.max(0, d)) * 255).toInt)
      if (hex.length == 1) "0" + hex else hex
    }
    withBackgroundColor("#" + convert(r) + convert(g) + convert(b))
  }

  /** Set the window start width */
  def withWindowWidth(value: Int): IndigoGameMetadata =
    this.copy(width = value)

  /** Set the window start height */
  def withWindowHeight(value: Int): IndigoGameMetadata =
    this.copy(height = value)

  /** Set the window start width */
  def withWindowSize(w: Int, h: Int): IndigoGameMetadata =
    this.copy(width = w, height = h)

}

object IndigoGameMetadata {

  /** The default metadata for an Indigo game. */
  val defaults: IndigoGameMetadata =
    IndigoGameMetadata(
      title = "Made with Indigo",
      showCursor = true,
      backgroundColor = "white",
      width = 550,
      height = 400
    )
}

/** Represents options specific to Electron builds of your game.
  *
  * @param disableFrameRateLimit
  *   If possible, disables the runtime's frame rate limit, recommended to be `false`. Default 'false'.
  * @param electronInstall
  *   How should electron be run? ElectronInstall.Global | ElectronInstall.Version(version: String) |
  *   ElectronInstall.Latest | ElectronInstall.PathToExecutable(path: String). Default 'ElectronInstall.Latest'.
  */
final case class IndigoElectronOptions(
    disableFrameRateLimit: Boolean,
    electronInstall: ElectronInstall
) {

  /** Electron will limit the frame rate using the default browser refresh rate, typically it will sync with your
    * monitor's refresh rate. It is recommended that you do this, and set your indigo config to limit the framerate too.
    */
  def electronLimitsFrameRate: IndigoElectronOptions =
    this.copy(disableFrameRateLimit = false)

  /** Electron will not limit the frame rate. */
  def electronUnlimitedFrameRate: IndigoElectronOptions =
    this.copy(disableFrameRateLimit = true)

  /** Sets the electron installation type. It is recommended that, during development at least, you set this to
    * `ElectronInstall.Latest` to take advantage of performance improvements.
    */
  def withElectronInstallType(value: ElectronInstall): IndigoElectronOptions =
    this.copy(electronInstall = value)

  /** Use the latest version of Electron with the `indigoRun` command, which will be installed with NPM. */
  def useLatestElectron: IndigoElectronOptions =
    withElectronInstallType(ElectronInstall.Latest)

  /** Use a globally installed version of Electron with the `indigoRun` command. Global installs of Electron have the
    * advantage of a slightly faster `indigoRun` startup time, however, global Electron installs can be of dubious
    * quality, and suffer from poor performance or limited features. Not recommended.
    */
  def useGlobalElectron: IndigoElectronOptions =
    withElectronInstallType(ElectronInstall.Global)

  /** Use a specific version of Electron, follows normal NPM version formats. */
  def useElectronVersion(version: String): IndigoElectronOptions =
    withElectronInstallType(ElectronInstall.Version(version))

  /** Use an Electron install at the specified path with the `indigoRun` command. */
  def useElectronExecutable(path: String): IndigoElectronOptions =
    withElectronInstallType(ElectronInstall.PathToExecutable(path))

}

object IndigoElectronOptions {

  /** Default settings for Electron */
  val defaults: IndigoElectronOptions =
    IndigoElectronOptions(
      disableFrameRateLimit = false,
      electronInstall = indigoplugin.ElectronInstall.Latest
    )
}

/** Represents you game's assets processing.
  *
  * @param gameAssetsDirectory
  *   Project relative path to a directory that contains all of the assets the game needs to load. Default './assets'.
  */
final case class IndigoAssets(
    gameAssetsDirectory: os.RelPath,
    include: os.RelPath => Boolean,
    exclude: os.RelPath => Boolean
) {

  /** Sets the asset directory path */
  def withAssetDirectory(path: String): IndigoAssets =
    this.copy(
      gameAssetsDirectory =
        if (path.startsWith("/")) os.Path(path).relativeTo(os.pwd)
        else os.RelPath(path)
    )
  def withAssetDirectory(path: os.RelPath): IndigoAssets =
    this.copy(gameAssetsDirectory = path)

  def withInclude(p: os.RelPath => Boolean): IndigoAssets =
    this.copy(include = p)

  def withExclude(p: os.RelPath => Boolean): IndigoAssets =
    this.copy(exclude = p)

}

object IndigoAssets {

  /** Default settings for an Indigo game's asset management */
  val defaults: IndigoAssets = {
    val pf: PartialFunction[os.RelPath, Boolean] = { case _ => false }

    IndigoAssets(gameAssetsDirectory = os.RelPath("assets"), pf, pf)
  }
}
