module ConfigEditor exposing (
  configModel
  , configUpdate
  , configView
  , ConfigModel
  , ConfigUpdateMsg
  )

import Html exposing (..)
import Html.Events exposing (onClick)
import Json.Encode exposing (..)


-- Config definitions

type alias ViewportConfig =
  { width : Int
  , height : Int
  }

type alias ClearColorConfig =
  { red: Float
  , green: Float
  , blue: Float
  , alpha: Float
  }

type alias AdvanceConfig =
  { recordMetrics: Bool
  , logMetricsReportIntervalMs: Int
  , disableSkipModelUpdates: Bool
  , disableSkipViewUpdates: Bool
  }

type alias ConfigModel =
  { magnification : Int
  , frameRate : Int
  , viewport : ViewportConfig
  , clearColor : ClearColorConfig
  , advanced : AdvanceConfig
  }


-- Model

configModel : ConfigModel
configModel =
  { magnification = 1
  , frameRate = 30
  , viewport =
    { width = 550
    , height = 400
    }
  , clearColor =
    { red = 0.0
    , green = 0.0
    , blue = 0.0
    , alpha = 1.0
    }
  , advanced =
    { recordMetrics = False
    , logMetricsReportIntervalMs = 10000
    , disableSkipModelUpdates = True
    , disableSkipViewUpdates = True
    }
  }


-- Update

type ConfigUpdateMsg =
  IncrementMagnification
  | DecrementMagnification
  | IncrementFrameRate
  | DecrementFrameRate

clampInt : Int -> Int -> Int -> Int -> Int
clampInt lowerBound upperBound current amount =
  if current + amount > lowerBound - 1 && current + amount < upperBound + 1
    then current + amount
    else current

updateMagnification : Int -> Int -> Int
updateMagnification current plus =
  clampInt 1 10 current plus

updateFrameRate : Int -> Int -> Int
updateFrameRate current plus =
  clampInt 1 60 current plus

configUpdate : ConfigUpdateMsg -> ConfigModel -> ConfigModel
configUpdate msg model =
  case msg of
    IncrementMagnification ->
      { model | magnification = updateMagnification model.magnification 1 }

    DecrementMagnification ->
      { model | magnification = updateMagnification model.magnification -1 }

    IncrementFrameRate ->
      { model | frameRate = updateFrameRate model.frameRate 1 }

    DecrementFrameRate ->
      { model | frameRate = updateFrameRate model.frameRate -1 }


-- View

viewportJson : ViewportConfig -> Value
viewportJson viewport =
  Json.Encode.object
  [ ("width", int viewport.width)
  , ("height", int viewport.height)
  ]

clearColorJson : ClearColorConfig -> Value
clearColorJson clearColor =
  Json.Encode.object
  [ ("r", float clearColor.red)
  , ("g", float clearColor.green)
  , ("b", float clearColor.blue)
  , ("a", float clearColor.alpha)
  ]

advancedJson : AdvanceConfig -> Value
advancedJson advanced =
  Json.Encode.object
  [ ("recordMetrics", bool advanced.recordMetrics)
  , ("logMetricsReportIntervalMs", int advanced.logMetricsReportIntervalMs)
  , ("disableSkipModelUpdates", bool advanced.disableSkipModelUpdates)
  , ("disableSkipViewUpdates", bool advanced.disableSkipViewUpdates)
  ]

configJson : ConfigModel -> Value
configJson model =
  Json.Encode.object
  [ ("magnification", int model.magnification)
  , ("frameRate", int model.frameRate)
  , ("viewport", viewportJson model.viewport)
  , ("clearColor", clearColorJson model.clearColor)
  , ("advanced", advancedJson model.advanced)
  ]

configView : ConfigModel -> Html ConfigUpdateMsg
configView model =
  div []
  [ div [] [ text "Config" ]
  , div []
    [ text "Magnification"
    , button [ onClick IncrementMagnification ] [ text "+" ]
    , button [ onClick DecrementMagnification ] [ text "-" ]
    ]
  , div []
    [ text "Frame rate"
    , button [ onClick IncrementFrameRate ] [ text "+" ]
    , button [ onClick DecrementFrameRate ] [ text "-" ]
    ]
  , textarea [] [ text (encode 2 (configJson model)) ]
  ]
