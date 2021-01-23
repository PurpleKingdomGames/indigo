package indigo.facades.worker

import indigo.shared.datatypes.FontInfo
import indigo.shared.animation.Animation
import indigo.shared.platform.SceneFrameData
import indigo.shared.platform.ProcessedSceneData

import scala.scalajs.js

object WorkerConversions {

  // FontInfo
  def readFontInfo(obj: js.Any): FontInfo =
    FontInfoConversion.fromJS(obj)

  def writeFontInfo(value: FontInfo): js.Any =
    FontInfoConversion.toJS(value)

  // Animation
  def readAnimation(obj: js.Any): Animation =
    AnimationConversion.fromJS(obj)

  def writeAnimation(value: Animation): js.Any =
    AnimationConversion.toJS(value)

  // SceneFrameData
  def readSceneFrameData(obj: js.Any): SceneFrameData =
    SceneFrameDataConversion.fromJS(obj)

  def writeSceneFrameData(value: SceneFrameData): js.Any =
    SceneFrameDataConversion.toJS(value)

  // ProcessedSceneData
  def readProcessedSceneData(obj: js.Any): ProcessedSceneData =
    ProcessedSceneDataConversion.fromJS(obj)

  def writeProcessedSceneData(value: ProcessedSceneData): js.Any =
    ProcessedSceneDataConversion.toJS(value)

}
