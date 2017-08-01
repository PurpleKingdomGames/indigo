module ConfigEditor
    exposing
        ( configModel
        , configUpdate
        , configView
        , ConfigModel
        , ConfigUpdateMsg
        )

import CounterComponent
import Html exposing (..)
import Html.Attributes exposing (..)
import Json.Encode exposing (..)


-- Config definitions


type alias ViewportConfig =
    { width : Int
    , height : Int
    }


type alias ClearColorConfig =
    { red : Float
    , green : Float
    , blue : Float
    , alpha : Float
    }


type alias AdvanceConfig =
    { recordMetrics : Bool
    , logMetricsReportIntervalMs : Int
    , disableSkipModelUpdates : Bool
    , disableSkipViewUpdates : Bool
    }


type alias ConfigModel =
    { magnification : CounterComponent.CounterModel
    , frameRate : CounterComponent.CounterModel
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


type ConfigUpdateMsg
    = UpdateMagnification CounterComponent.CounterUpdateMsg
    | UpdateFrameRate CounterComponent.CounterUpdateMsg


configUpdate : ConfigUpdateMsg -> ConfigModel -> ConfigModel
configUpdate msg model =
    case msg of
        UpdateMagnification msg ->
            { model | magnification = CounterComponent.update 1 10 msg model.magnification }

        UpdateFrameRate msg ->
            { model | frameRate = CounterComponent.update 1 60 msg model.frameRate }



-- View


viewportJson : ViewportConfig -> Value
viewportJson viewport =
    Json.Encode.object
        [ ( "width", int viewport.width )
        , ( "height", int viewport.height )
        ]


clearColorJson : ClearColorConfig -> Value
clearColorJson clearColor =
    Json.Encode.object
        [ ( "r", float clearColor.red )
        , ( "g", float clearColor.green )
        , ( "b", float clearColor.blue )
        , ( "a", float clearColor.alpha )
        ]


advancedJson : AdvanceConfig -> Value
advancedJson advanced =
    Json.Encode.object
        [ ( "recordMetrics", bool advanced.recordMetrics )
        , ( "logMetricsReportIntervalMs", int advanced.logMetricsReportIntervalMs )
        , ( "disableSkipModelUpdates", bool advanced.disableSkipModelUpdates )
        , ( "disableSkipViewUpdates", bool advanced.disableSkipViewUpdates )
        ]


configJson : ConfigModel -> Value
configJson model =
    Json.Encode.object
        [ ( "magnification", int model.magnification )
        , ( "frameRate", int model.frameRate )
        , ( "viewport", viewportJson model.viewport )
        , ( "clearColor", clearColorJson model.clearColor )
        , ( "advanced", advancedJson model.advanced )
        ]


configView : ConfigModel -> Html ConfigUpdateMsg
configView model =
    div []
        [ div [] [ text "Config" ]
        , Html.map UpdateMagnification (CounterComponent.view "Magnification" model.magnification)
        , Html.map UpdateFrameRate (CounterComponent.view "Frame rate" model.frameRate)
        , textarea [ cols 50, rows 25 ] [ text (encode 2 (configJson model)) ]
        ]
