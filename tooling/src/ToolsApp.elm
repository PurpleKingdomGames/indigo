module Main exposing (..)

import Browser exposing (..)
import Browser.Navigation as Nav
import Html exposing (..)
import Html.Attributes exposing (href)
import Model exposing (..)
import Msg exposing (..)
import PageRouting exposing (pageContent, urlUpdate)
import SubMenu exposing (subMenu)
import Url exposing (Url)


type alias Flags =
    {}


main : Program Flags Model Msg
main =
    Browser.application
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        , onUrlRequest = ClickedLink
        , onUrlChange = UrlChange
        }


init : Flags -> Url -> Nav.Key -> ( Model, Cmd Msg )
init _ _ navKey =
    ( Model Home navKey, Cmd.none )


view : Model -> Document Msg
view model =
    { title = "Indigo Tools"
    , body =
        [ div []
            [ subMenu
            , pageContent model.page
            ]
        ]
    }


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        UrlChange url ->
            urlUpdate url model

        ClickedLink (Browser.Internal url) ->
            ( model, Nav.pushUrl model.navKey <| Url.toString url )

        ClickedLink (Browser.External href) ->
            ( model, Nav.load href )


subscriptions : Model -> Sub Msg
subscriptions _ =
    Sub.none
