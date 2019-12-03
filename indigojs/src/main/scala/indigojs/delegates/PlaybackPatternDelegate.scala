package indigojs.delegates
import indigo.shared.scenegraph.PlaybackPattern

sealed trait PlaybackPatternDelegate {
  val playbackType: String

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  def toInternal: PlaybackPattern =
    this.playbackType match {
      case "silent" =>
        PlaybackPattern.Silent

      case "single" =>
        PlaybackPattern.SingleTrackLoop(this.asInstanceOf[SingleTrackLoopDelegate].track.toInternal)
    }
}
object PlaybackPatternDelegate {
  val Silent: SlientDelegate =
    new SlientDelegate
}

final class SlientDelegate extends PlaybackPatternDelegate {
  val playbackType: String = "silent"
}
final class SingleTrackLoopDelegate(val track: TrackDelegate) extends PlaybackPatternDelegate {
  val playbackType: String = "single"
}
