import Html exposing (..)
import Html.Events exposing (onClick)
import Json.Encode exposing (..)

main : Program Never ConfigModel ConfigUpdateMsg
main =
  Html.beginnerProgram { model = initialConfigModel , view = configView , update = configUpdate }

-- Lets just think about the config first...

type alias ConfigModel =
  { magnification : Int
  , frameRate : Int
  }

initialConfigModel : ConfigModel
initialConfigModel =
  { magnification = 1
  , frameRate = 30
  }

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

configJson : ConfigModel -> Value
configJson model =
  Json.Encode.object [
    ("magnification", int model.magnification)
    , ("frameRate", int model.frameRate)
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

{-
{
  "viewport": {
    "width": 256,
    "height": 256
  },
  "frameRate": 10,
  "clearColor": {
    "r": 0.6,
    "g": 0.1,
    "b": 0,
    "a": 1.0
  },
  "magnification": 1,
  "advanced": {
    "recordMetrics": false,
    "logMetricsReportIntervalMs": 10000,
    "disableSkipModelUpdates": false,
    "disableSkipViewUpdates": false
  }
}
-}
