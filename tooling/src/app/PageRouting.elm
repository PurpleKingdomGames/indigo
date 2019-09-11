module App.PageRouting exposing (bumpToNormalSlug, fontSheetSlug, lightingToNormalSlug, pageContent, urlUpdate)

import App.Model exposing (..)
import App.Msg exposing (..)
import Html exposing (Html, text)
import Modules.BumpToNormal as BumpToNormal
import Url exposing (Url)
import Url.Parser as UrlParser exposing ((</>), Parser, s, top)


bumpToNormalSlug : String
bumpToNormalSlug =
    "bump-2-normal"


lightingToNormalSlug : String
lightingToNormalSlug =
    "lighting-2-normal"


fontSheetSlug : String
fontSheetSlug =
    "font-sheet"


pageContent : Model -> Html Msg
pageContent model =
    case model.page of
        Home ->
            text "Home"

        Bump2Normal ->
            Html.map (\m -> BumpToNormalMsgWrapper m) (BumpToNormal.view model.bumpToNormal)

        Lighting2Normal ->
            text "Lighting to Normal"

        FontSheet ->
            text "Font sheet generator"

        NotFound ->
            text "Not Found"


urlUpdate : Url -> Model -> ( Model, Cmd Msg )
urlUpdate url model =
    case decode url of
        Nothing ->
            ( { model | page = NotFound }, Cmd.none )

        Just Bump2Normal ->
            ( { model | page = Bump2Normal }, Cmd.none )

        Just Lighting2Normal ->
            ( { model | page = Lighting2Normal }, Cmd.none )

        Just FontSheet ->
            ( { model | page = FontSheet }, Cmd.none )

        Just route ->
            ( { model | page = route }, Cmd.none )


decode : Url -> Maybe Page
decode url =
    UrlParser.parse routeParser url


routeParser : Parser (Page -> a) a
routeParser =
    UrlParser.oneOf
        [ UrlParser.map Home top
        , UrlParser.map Bump2Normal (s bumpToNormalSlug)
        , UrlParser.map Lighting2Normal (s lightingToNormalSlug)
        , UrlParser.map FontSheet (s fontSheetSlug)
        ]
