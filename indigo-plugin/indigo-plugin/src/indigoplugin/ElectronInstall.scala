package indigoplugin

sealed trait ElectronInstall {
  def executable: String =
    this match {
      case ElectronInstall.Global                 => "electron"
      case ElectronInstall.Version(_)             => "npx electron"
      case ElectronInstall.Latest                 => "npx electron"
      case ElectronInstall.PathToExecutable(path) => path
    }

  def devDependencies: String =
    this match {
      case ElectronInstall.Global                 => ""
      case ElectronInstall.Version(version)       => s""""electron": "${version}""""
      case ElectronInstall.Latest                 => ""
      case ElectronInstall.PathToExecutable(path) => path
    }

  def requiresInstall: Boolean =
    this match {
      case ElectronInstall.Global              => false
      case ElectronInstall.Version(_)          => true
      case ElectronInstall.Latest              => true
      case ElectronInstall.PathToExecutable(_) => false
    }
}
object ElectronInstall {
  case object Global                              extends ElectronInstall
  final case class Version(version: String)       extends ElectronInstall
  case object Latest                              extends ElectronInstall
  final case class PathToExecutable(path: String) extends ElectronInstall
}
