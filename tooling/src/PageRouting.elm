module PageRouting exposing (pageContent, urlUpdate)

import Html exposing (Html, text)
import Model exposing (..)
import Msg exposing (Msg)
import Url exposing (Url)
import Url.Parser as UrlParser exposing ((</>), Parser, s, top)


pageContent : Page -> Html Msg
pageContent page =
    case page of
        Home ->
            text "Home"

        Page1 ->
            text "Page 1"

        Page2 ->
            text "Page 2"

        NotFound ->
            text "Not Found"


urlUpdate : Url -> Model -> ( Model, Cmd Msg )
urlUpdate url model =
    case decode url of
        Nothing ->
            ( { model | page = NotFound }, Cmd.none )

        Just route ->
            ( { model | page = route }, Cmd.none )


decode : Url -> Maybe Page
decode url =
    UrlParser.parse routeParser url


routeParser : Parser (Page -> a) a
routeParser =
    UrlParser.oneOf
        [ UrlParser.map Home top
        , UrlParser.map Page1 (s "page-1")
        , UrlParser.map Page2 (s "page-2")
        ]
