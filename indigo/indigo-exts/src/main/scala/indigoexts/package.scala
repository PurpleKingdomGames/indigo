package object indigoexts {

  object lens {
    type Lens[A, B] = lenses.Lens[A, B]
    val Lens: lenses.Lens.type = lenses.Lens
  }

  object shapes {
    type LineSegment = geometry.LineSegment
    val LineSegment: geometry.LineSegment.type = geometry.LineSegment

    type LineProperties = geometry.LineProperties
    val LineProperties: geometry.LineProperties.type = geometry.LineProperties

    type IntersectionResult = geometry.IntersectionResult
    val IntersectionResult: geometry.IntersectionResult.type = geometry.IntersectionResult
  }

  object scenes {
    type Scene[GameModel, ViewModel] = scenemanager.Scene[GameModel, ViewModel]
    val Scene: scenemanager.Scene.type = scenemanager.Scene

    type SceneName = scenemanager.SceneName
    val SceneName: scenemanager.SceneName.type = scenemanager.SceneName

    type SceneEvent = scenemanager.SceneEvent
    val SceneEvent: scenemanager.SceneEvent.type = scenemanager.SceneEvent
  }

  object ui {
    type Button = indigoexts.uicomponents.Button
    val Button: indigoexts.uicomponents.Button.type = indigoexts.uicomponents.Button
    type ButtonState = indigoexts.uicomponents.ButtonState
    val ButtonState: indigoexts.uicomponents.ButtonState.type = indigoexts.uicomponents.ButtonState
    type ButtonAssets = indigoexts.uicomponents.ButtonAssets
    val ButtonAssets: indigoexts.uicomponents.ButtonAssets.type = indigoexts.uicomponents.ButtonAssets

    type InputField = indigoexts.uicomponents.InputField
    val InputField: indigoexts.uicomponents.InputField.type = indigoexts.uicomponents.InputField
    type InputFieldAssets = indigoexts.uicomponents.InputFieldAssets
    val InputFieldAssets: indigoexts.uicomponents.InputFieldAssets.type = indigoexts.uicomponents.InputFieldAssets
  }

}
