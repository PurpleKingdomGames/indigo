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


clearColorRedLens : Lens ClearColorConfig Float
clearColorRedLens =
    Lens (\cc -> cc.red) (\v cc -> { cc | red = v })


clearColorGreenLens : Lens ClearColorConfig Float
clearColorGreenLens =
    Lens (\cc -> cc.green) (\v cc -> { cc | green = v })


clearColorBlueLens : Lens ClearColorConfig Float
clearColorBlueLens =
    Lens (\cc -> cc.blue) (\v cc -> { cc | blue = v })


clearColorAlphaLens : Lens ClearColorConfig Float
clearColorAlphaLens =
    Lens (\cc -> cc.alpha) (\v cc -> { cc | alpha = v })


type alias AdvancedConfig =
    { recordMetrics : Bool
    , logMetricsReportIntervalMs : Int
    , disableSkipModelUpdates : Bool
    , disableSkipViewUpdates : Bool
    }


advancedRecordMetricsLens : Lens AdvancedConfig Bool
advancedRecordMetricsLens =
    Lens (\a -> a.recordMetrics) (\v a -> { a | recordMetrics = v })


advancedMetricIntervalLens : Lens AdvancedConfig Int
advancedMetricIntervalLens =
    Lens (\a -> a.logMetricsReportIntervalMs) (\v a -> { a | logMetricsReportIntervalMs = v })


advancedDisableSkipModelLens : Lens AdvancedConfig Bool
advancedDisableSkipModelLens =
    Lens (\a -> a.disableSkipModelUpdates) (\v a -> { a | disableSkipModelUpdates = v })


advancedDisableSkipViewLens : Lens AdvancedConfig Bool
advancedDisableSkipViewLens =
    Lens (\a -> a.disableSkipViewUpdates) (\v a -> { a | disableSkipViewUpdates = v })


type alias ConfigModel =
    { magnification : CounterComponent.CounterModel
    , frameRate : CounterComponent.CounterModel
    , viewport : ViewportConfig
    , clearColor : ClearColorConfig
    , advanced : AdvancedConfig
    }


configViewportLens : Lens ConfigModel ViewportConfig
configViewportLens =
    Lens (\c -> c.viewport) (\vp c -> { c | viewport = vp })


configClearColourLens : Lens ConfigModel ClearColorConfig
configClearColourLens =
    Lens (\c -> c.clearColor) (\cc c -> { c | clearColor = cc })


configAdvancedLens : Lens ConfigModel AdvancedConfig
configAdvancedLens =
    Lens (\c -> c.advanced) (\a c -> { c | advanced = a })



-- composed lenses


configViewportWidthLens : Lens ConfigModel Int
configViewportWidthLens =
    compose configViewportLens viewportWidthLens


configViewportHeightLens : Lens ConfigModel Int
configViewportHeightLens =
    compose configViewportLens viewportHeightLens


configClearColorRedLens : Lens ConfigModel Float
configClearColorRedLens =
    compose configClearColourLens clearColorRedLens


configClearColorGreenLens : Lens ConfigModel Float
configClearColorGreenLens =
    compose configClearColourLens clearColorGreenLens


configClearColorBlueLens : Lens ConfigModel Float
configClearColorBlueLens =
    compose configClearColourLens clearColorBlueLens


configClearColorAlphaLens : Lens ConfigModel Float
configClearColorAlphaLens =
    compose configClearColourLens clearColorAlphaLens


configAdvancedRecordMetricsLens : Lens ConfigModel Bool
configAdvancedRecordMetricsLens =
    compose configAdvancedLens advancedRecordMetricsLens


configAdvancedMetricIntervalLens : Lens ConfigModel Int
configAdvancedMetricIntervalLens =
    compose configAdvancedLens advancedMetricIntervalLens


configAdvancedDisableSkipModelLens : Lens ConfigModel Bool
configAdvancedDisableSkipModelLens =
    compose configAdvancedLens advancedDisableSkipModelLens


configAdvancedDisableSkipViewLens : Lens ConfigModel Bool
configAdvancedDisableSkipViewLens =
    compose configAdvancedLens advancedDisableSkipViewLens



-- Model


configModel : ConfigModel
configModel =
    { magnification = CounterComponent.initialModel 1
    , frameRate = CounterComponent.initialModel 30
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


advancedJson : AdvancedConfig -> Value
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
