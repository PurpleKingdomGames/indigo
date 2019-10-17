module Main exposing (..)

import Browser
import Element exposing (..)
import Element.Background as Background
import Element.Border as Border
import Element.Font as Font
import Element.Input as Input
import Html exposing (Html)


main =
    Browser.sandbox { init = initialModel, update = update, view = view }


type Msg
    = Increment
    | Decrement


type alias Model =
    { amount : Int }


initialModel : Model
initialModel =
    Model 0


update : Msg -> Model -> Model
update msg model =
    case msg of
        Increment ->
            { model | amount = model.amount + 1 }

        Decrement ->
            { model | amount = model.amount - 1 }


view : Model -> Html.Html Msg
view model =
    Element.layout [] (counterElements model)


blue =
    Element.rgb255 238 238 238


myButton : String -> Msg -> Element Msg
myButton label msg =
    Input.button
        [ Background.color blue
        ]
        { onPress = Just msg
        , label = text label
        }


counterElements : Model -> Element Msg
counterElements model =
    row [ spacing 30, padding 10 ]
        [ myButton "Increment" Increment
        , text <| String.fromInt model.amount
        , myButton "Decrement" Decrement
        ]
