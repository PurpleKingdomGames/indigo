module Main exposing (Model, Msg(..), init, main, subscriptions, update, view)

import Browser
import File exposing (File)
import File.Select as Select
import Html exposing (Html, button, p, text)
import Html.Attributes exposing (style)
import Html.Events exposing (onClick)
import Task



-- MAIN


main : Program () Model Msg
main =
    Browser.element
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        }



-- MODEL


type alias Model =
    { csv : Maybe String
    }


init : () -> ( Model, Cmd Msg )
init _ =
    ( Model Nothing, Cmd.none )



-- UPDATE


type Msg
    = CsvRequested
    | CsvSelected File
    | CsvLoaded String


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        CsvRequested ->
            ( model
            , Select.file [ "text/csv" ] CsvSelected
            )

        CsvSelected file ->
            ( model
            , Task.perform CsvLoaded (File.toString file)
            )

        CsvLoaded content ->
            ( { model | csv = Just content }
            , Cmd.none
            )



-- VIEW


view : Model -> Html Msg
view model =
    case model.csv of
        Nothing ->
            button [ onClick CsvRequested ] [ text "Load CSV" ]

        Just content ->
            p [ style "white-space" "pre" ] [ text content ]



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions _ =
    Sub.none
