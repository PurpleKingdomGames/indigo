module ConfigEditor.ConfigEditor
    exposing
        ( configUpdate
        , configView
        , ConfigUpdateMsg
        )

import Components.CounterComponent as CounterComponent
import Html exposing (..)
import Html.Attributes exposing (..)
import Json.Encode exposing (..)
import Components.TextInputComponent as TextInputComponent
import ConfigEditor.ConfigModel as ConfigModel exposing (..)


type ConfigUpdateMsg
    = UpdateMagnification CounterComponent.CounterUpdateMsg
    | UpdateFrameRate CounterComponent.CounterUpdateMsg
    | UpdateViewportWidth TextInputComponent.TextInputMsg
    | UpdateViewportHeight TextInputComponent.TextInputMsg
    | UpdateClearColorR TextInputComponent.TextInputMsg
    | UpdateClearColorG TextInputComponent.TextInputMsg
    | UpdateClearColorB TextInputComponent.TextInputMsg
    | UpdateClearColorA TextInputComponent.TextInputMsg
    | UpdateAdvancedMetricsInterval TextInputComponent.TextInputMsg


configUpdate : ConfigUpdateMsg -> ConfigModel.ConfigModel -> ConfigModel.ConfigModel
configUpdate msg model =
    case msg of
        UpdateMagnification msg ->
            { model | magnification = CounterComponent.update 1 10 msg model.magnification }

        UpdateFrameRate msg ->
            { model | frameRate = CounterComponent.update 1 60 msg model.frameRate }

        UpdateViewportWidth msg ->
            configViewportWidthLens.set (TextInputComponent.update msg model.viewport.width) model

        UpdateViewportHeight msg ->
            configViewportHeightLens.set (TextInputComponent.update msg model.viewport.height) model

        UpdateClearColorR msg ->
            configClearColorRedLens.set (TextInputComponent.update msg model.clearColor.red) model

        UpdateClearColorG msg ->
            configClearColorGreenLens.set (TextInputComponent.update msg model.clearColor.green) model

        UpdateClearColorB msg ->
            configClearColorBlueLens.set (TextInputComponent.update msg model.clearColor.blue) model

        UpdateClearColorA msg ->
            configClearColorAlphaLens.set (TextInputComponent.update msg model.clearColor.alpha) model

        UpdateAdvancedMetricsInterval msg ->
            configAdvancedMetricIntervalLens.set (TextInputComponent.update msg model.advanced.logMetricsReportIntervalMs) model


configView : ConfigModel -> Html ConfigUpdateMsg
configView model =
    div []
        [ div [] [ text "Config" ]
        , Html.map UpdateMagnification (CounterComponent.view "Magnification" model.magnification)
        , Html.map UpdateFrameRate (CounterComponent.view "Frame rate" model.frameRate)
        , Html.map UpdateViewportWidth (TextInputComponent.view "Viewport width" model.viewport.width)
        , Html.map UpdateViewportHeight (TextInputComponent.view "Viewport height" model.viewport.height)
        , Html.map UpdateClearColorR (TextInputComponent.view "Clear colour red" model.clearColor.red)
        , Html.map UpdateClearColorG (TextInputComponent.view "Clear colour green" model.clearColor.green)
        , Html.map UpdateClearColorB (TextInputComponent.view "Clear colour blue" model.clearColor.blue)
        , Html.map UpdateClearColorA (TextInputComponent.view "Clear colour alpha" model.clearColor.alpha)
        , text "Advanced"
        , Html.map UpdateAdvancedMetricsInterval (TextInputComponent.view "Metric reporting interval" model.advanced.logMetricsReportIntervalMs)
        , textarea [ cols 50, rows 25 ] [ text (encode 2 (configJson model)) ]
        ]
