module App.PageRouting exposing (bumpToNormalSlug, fontSheetSlug, lightingToNormalSlug, pageContent, urlUpdate)

import App.Model exposing (..)
import App.Msg exposing (..)
import App.Styles as Styles
import Element exposing (..)
import Element.Background as Background
import Element.Border as Border
import Element.Input as Input
import Html exposing (Html)
import Modules.BumpToNormal as BumpToNormal
import Modules.FontSheet as FontSheet
import Url exposing (Url)
import Url.Parser as UrlParser exposing (Parser, s, top)


bumpToNormalSlug : String
bumpToNormalSlug =
    "bump-2-normal"


lightingToNormalSlug : String
lightingToNormalSlug =
    "lighting-2-normal"


fontSheetSlug : String
fontSheetSlug =
    "font-sheet"


pageContent : Model -> Element Msg
pageContent model =
    case model.page of
        Home ->
            row [ width fill, centerY ]
                [ image [ centerX, width (px 512), height (px 512) ]
                    { src = "assets/pk_games_pixel.png"
                    , description = "Purple Kingdom Games"
                    }
                ]

        Bump2Normal ->
            Element.map (\m -> BumpToNormalMsgWrapper m) (BumpToNormal.view model.bumpToNormal)

        Lighting2Normal ->
            row [ padding 10 ]
                [ column [ spacing 10 ]
                    [ text "Lighting to Normal"
                    , Input.button [ Background.color Styles.darkPurple, Border.rounded 5, padding 10 ]
                        { onPress = Just (LogMessage "Log this elm!")
                        , label = text "Get Scala to Log Something!"
                        }
                    ]
                ]

        FontSheet ->
            Element.map (\m -> FontSheetMsgWrapper m) (FontSheet.view model.fontSheet)

        NotFound ->
            row [ padding 10 ] [ text "Not Found" ]


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
