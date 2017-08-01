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
import Html.Events exposing (onInput)
import Json.Encode exposing (..)
import Monocle.Lens exposing (Lens, compose)


-- Config definitions


type alias ViewportConfig =
    { width : Int
    , height : Int
    }


viewportWidthLens : Lens ViewportConfig Int
viewportWidthLens =
    Lens (\vp -> vp.width) (\w vp -> { vp | width = w })


viewportHeightLens : Lens ViewportConfig Int
viewportHeightLens =
    Lens (\vp -> vp.height) (\h vp -> { vp | height = h })


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


configViewportLens : Lens ConfigModel ViewportConfig
configViewportLens =
    Lens (\c -> c.viewport) (\vp c -> { c | viewport = vp })


configViewportWidthLens : Lens ConfigModel Int
configViewportWidthLens =
    compose configViewportLens viewportWidthLens


configViewportHeightLens : Lens ConfigModel Int
configViewportHeightLens =
    compose configViewportLens viewportHeightLens



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
    | UpdateViewportWidth String
    | UpdateViewportHeight String


configUpdate : ConfigUpdateMsg -> ConfigModel -> ConfigModel
configUpdate msg model =
    case msg of
        UpdateMagnification msg ->
            { model | magnification = CounterComponent.update 1 10 msg model.magnification }

        UpdateFrameRate msg ->
            { model | frameRate = CounterComponent.update 1 60 msg model.frameRate }

        UpdateViewportWidth msg ->
            configViewportWidthLens.set (Result.withDefault 1 (String.toInt msg)) model

        UpdateViewportHeight msg ->
            configViewportHeightLens.set (Result.withDefault 1 (String.toInt msg)) model



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
        , div []
            [ text "Viewport width"
            , input [ value (toString (configViewportWidthLens.get model)), onInput UpdateViewportWidth ] []
            ]
        , div []
            [ text "Viewport height"
            , input [ value (toString (configViewportHeightLens.get model)), onInput UpdateViewportHeight ] []
            ]
        , textarea [ cols 50, rows 25 ] [ text (encode 2 (configJson model)) ]
        ]
