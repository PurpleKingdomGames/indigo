package indigoplugin

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
    openDevTools: Boolean,
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

  /** Open the developer tools when the Electron window is created. */
  def developerToolsOpenOnStartUp: IndigoElectronOptions =
    this.copy(openDevTools = true)

  /** Open the developer tools when the Electron window is created. (Alias for developerToolsOpenOnStartUp) */
  def openDeveloperTools: IndigoElectronOptions =
    developerToolsOpenOnStartUp

  /** Do not open the developer tools when the Electron window is created. */
  def developerToolsClosedOnStartUp: IndigoElectronOptions =
    this.copy(openDevTools = false)

  /** Do not open the developer tools when the Electron window is created. (Alias for developerToolsClosedOnStartUp) */
  def doNotOpenDeveloperTools: IndigoElectronOptions =
    developerToolsClosedOnStartUp

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
      openDevTools = false,
      electronInstall = indigoplugin.ElectronInstall.Latest
    )
}
