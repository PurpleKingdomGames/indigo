sealed trait PlatformSuffix {
  val suffix: String =
    this match {
      case PlatformSuffix.JS  => "JS"
      case PlatformSuffix.JVM => "JVM"
      case PlatformSuffix.Ignore => ""
    }
}
object PlatformSuffix {
  case object JS  extends PlatformSuffix
  case object JVM extends PlatformSuffix
  case object Ignore extends PlatformSuffix

  val All: List[PlatformSuffix]     = List(JS, JVM)
  val JSOnly: List[PlatformSuffix]  = List(JS)
  val JVMOnly: List[PlatformSuffix] = List(JVM)
  val Omit: List[PlatformSuffix]    = Nil
}