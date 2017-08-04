module ConfigEditor.ConfigModel
    exposing
        ( configJson
        , configModel
        , ConfigModel
        , configViewportWidthLens
        , configViewportHeightLens
        )

import Components.CounterComponent as CounterComponent
import Json.Encode exposing (..)
import Monocle.Lens exposing (Lens, compose)
import Components.TextInputComponent as TextInputComponent


type alias ViewportConfig =
    { width : TextInputComponent.TextInputModel
    , height : TextInputComponent.TextInputModel
    }


type alias ClearColorConfig =
    { red : Float
    , green : Float
    , blue : Float
    , alpha : Float
    }


type alias AdvancedConfig =
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
    , advanced : AdvancedConfig
    }


configModel : ConfigModel
configModel =
    { magnification = CounterComponent.initialModel 1
    , frameRate = CounterComponent.initialModel 30
    , viewport =
        { width = TextInputComponent.initial (TextInputComponent.AsInt 550)
        , height = TextInputComponent.initial (TextInputComponent.AsInt 400)
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



-- lenses


viewportWidthLens : Lens ViewportConfig TextInputComponent.TextInputModel
viewportWidthLens =
    Lens (\vp -> vp.width) (\w vp -> { vp | width = w })


viewportHeightLens : Lens ViewportConfig TextInputComponent.TextInputModel
viewportHeightLens =
    Lens (\vp -> vp.height) (\h vp -> { vp | height = h })


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


configViewportLens : Lens ConfigModel ViewportConfig
configViewportLens =
    Lens (\c -> c.viewport) (\vp c -> { c | viewport = vp })


configClearColourLens : Lens ConfigModel ClearColorConfig
configClearColourLens =
    Lens (\c -> c.clearColor) (\cc c -> { c | clearColor = cc })


configAdvancedLens : Lens ConfigModel AdvancedConfig
configAdvancedLens =
    Lens (\c -> c.advanced) (\a c -> { c | advanced = a })


configViewportWidthLens : Lens ConfigModel TextInputComponent.TextInputModel
configViewportWidthLens =
    compose configViewportLens viewportWidthLens


configViewportHeightLens : Lens ConfigModel TextInputComponent.TextInputModel
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



-- Json encoders


viewportJson : ViewportConfig -> Value
viewportJson viewport =
    Json.Encode.object
        [ ( "width", int (TextInputComponent.toInt viewport.width) )
        , ( "height", int (TextInputComponent.toInt viewport.height) )
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
