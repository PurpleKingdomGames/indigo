package indigoplugin

sealed trait ElectronInstall {

  def executable: String =
    this match {
      case ElectronInstall.Global                 => "electron"
      case ElectronInstall.Version(_)             => "npx --no-install electron"
      case ElectronInstall.Latest                 => "npx --no-install electron"
      case ElectronInstall.PathToExecutable(path) => path
    }

  def devDependencies: String =
    this match {
      case ElectronInstall.Global                 => ""
      case ElectronInstall.Version(version)       => s""""electron": "${version}""""
      case ElectronInstall.Latest                 => ""
      case ElectronInstall.PathToExecutable(path) => path
    }

}
object ElectronInstall {
  case object Global                              extends ElectronInstall
  final case class Version(version: String)       extends ElectronInstall
  case object Latest                              extends ElectronInstall
  final case class PathToExecutable(path: String) extends ElectronInstall
}
