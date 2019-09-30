port module Main exposing (..)

import Browser
import Debug exposing (log)
import Html exposing (Html, button, div, text)
import Html.Events exposing (onClick)
import Json.Decode as Decode exposing (..)
import Json.Encode as Encode


main =
    Browser.element
        { init = init
        , update = update
        , view = view
        , subscriptions = subscriptions
        }


type Msg
    = Increment
    | Decrement
    | Doubled Int
    | Noop


type alias Model =
    { amount : Int
    , doubled : Int
    }


type alias ScalaMsg =
    Encode.Value


port toScala : ScalaMsg -> Cmd msg


port fromScala : (String -> msg) -> Sub msg


init : String -> ( Model, Cmd Msg )
init =
    \_ -> ( Model 0 0, Cmd.none )


subscriptions : Model -> Sub Msg
subscriptions _ =
    fromScala decoderScalaMsg


decoderScalaMsg : String -> Msg
decoderScalaMsg msg =
    case Decode.decodeString (field "amount" int) msg of
        Err e ->
            (log <| Debug.toString e)
                Noop

        Ok i ->
            Doubled i


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        Increment ->
            ( { model | amount = model.amount + 1 }, toScala <| encode <| model.amount + 1 )

        Decrement ->
            ( { model | amount = model.amount - 1 }, toScala <| encode <| model.amount - 1 )

        Doubled d ->
            ( { model | doubled = d }, Cmd.none )

        Noop ->
            ( model, Cmd.none )


encode : Int -> ScalaMsg
encode i =
    Encode.object
        [ ( "amount", Encode.int i ) ]


view : Model -> Html Msg
view model =
    div []
        [ button [ onClick Decrement ] [ text "-" ]
        , div [] [ text (String.fromInt model.amount) ]
        , button [ onClick Increment ] [ text "+" ]
        , div [] [ text <| "Doubled: " ++ String.fromInt model.doubled ]
        ]
