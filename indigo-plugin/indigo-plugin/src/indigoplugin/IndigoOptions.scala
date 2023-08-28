package indigoplugin

/** Represents the various options that go into an Indigo game build.
  *
  * @param title
  *   Title of your game. Default 'Made with Indigo'.
  * @param showCursor
  *   Show the cursor? Default 'true'.
  * @param backgroundColor
  *   HTML page background color Default 'white'.
  * @param gameAssetsDirectory
  *   Project relative path to a directory that contains all of the assets the game needs to load. Default './assets'.
  * @param windowStartWidth
  *   Initial window width. Default '550'.
  * @param windowStartHeight
  *   Initial window height. Default '400'.
  * @param disableFrameRateLimit
  *   If possible, disables the runtime's frame rate limit, recommended to be `false`. Default 'false'.
  * @param electronInstall
  *   How should electron be run? ElectronInstall.Global | ElectronInstall.Version(version: String) |
  *   ElectronInstall.Latest | ElectronInstall.PathToExecutable(path: String). Default 'ElectronInstall.Latest'.
  */
final case class IndigoOptions(
    title: String,
    showCursor: Boolean,
    backgroundColor: String,
    gameAssetsDirectory: os.Path,
    windowStartWidth: Int,
    windowStartHeight: Int,
    disableFrameRateLimit: Boolean,
    electronInstall: ElectronInstall
) {

  /** Sets a new title for your game's window / title bar / tab */
  def withTitle(newTitle: String): IndigoOptions =
    this.copy(title = newTitle)

  /** Make the cursor visible */
  def cursorVisible: IndigoOptions =
    this.copy(showCursor = true)

  /** Hide the cursor */
  def cursorHidden: IndigoOptions =
    this.copy(showCursor = false)

  /** Sets the background color, any valid CSS color representation acceptable, e.g. 'black' or '#000000' */
  def withBackgroundColor(cssColorValue: String): IndigoOptions =
    this.copy(backgroundColor = cssColorValue)

  /** Set the background color from RGBA values */
  def withBackgroundColor(r: Double, g: Double, b: Double, a: Double): IndigoOptions = {
    val convert: Double => String = d => {
      val hex = Integer.toHexString((Math.min(1, Math.max(0, d)) * 255).toInt)
      if (hex.length == 1) "0" + hex else hex
    }
    withBackgroundColor("#" + convert(r) + convert(g) + convert(b) + convert(a))
  }

  /** Set the background color from RGB values */
  def withBackgroundColor(r: Double, g: Double, b: Double): IndigoOptions = {
    val convert: Double => String = d => {
      val hex = Integer.toHexString((Math.min(1, Math.max(0, d)) * 255).toInt)
      if (hex.length == 1) "0" + hex else hex
    }
    withBackgroundColor("#" + convert(r) + convert(g) + convert(b))
  }

  /** Sets the asset directory path */
  def withAssetDirectory(path: String): IndigoOptions =
    this.copy(
      gameAssetsDirectory =
        if (path.startsWith("/")) os.Path(path)
        else os.RelPath(path).resolveFrom(os.pwd)
    )
  def withAssetDirectory(path: os.Path): IndigoOptions =
    this.copy(gameAssetsDirectory = path)

  /** Set the window start width */
  def withWindowStartWidth(value: Int): IndigoOptions =
    this.copy(windowStartWidth = value)

  /** Set the window start height */
  def withWindowStartHeight(value: Int): IndigoOptions =
    this.copy(windowStartHeight = value)

  /** Electron will limit the frame rate using the default browser refresh rate, typically it will sync with your
    * monitor's refresh rate. It is recommended that you do this, and set your indigo config to limit the framerate too.
    */
  def electronLimitsFrameRate: IndigoOptions =
    this.copy(disableFrameRateLimit = false)

  /** Electron will not limit the frame rate. */
  def electronUnlimitedFrameRate: IndigoOptions =
    this.copy(disableFrameRateLimit = true)

  /** Sets the electron installation type. It is recommended that, during development at least, you set this to
    * `ElectronInstall.Latest` to take advantage of performance improvements.
    */
  def withElectronInstallType(value: ElectronInstall): IndigoOptions =
    this.copy(electronInstall = value)

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

  val defaults: IndigoOptions =
    IndigoOptions(
      title = "Made with Indigo",
      showCursor = true,
      backgroundColor = "white",
      gameAssetsDirectory = os.pwd / "assets",
      windowStartWidth = 550,
      windowStartHeight = 400,
      disableFrameRateLimit = false,
      electronInstall = indigoplugin.ElectronInstall.Latest
    )

}
