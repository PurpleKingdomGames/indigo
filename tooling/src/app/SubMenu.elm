module App.SubMenu exposing (view)

import App.Model exposing (..)
import App.Msg exposing (Msg)
import App.PageRouting as PageRouting
import App.Styles as Styles
import Element exposing (..)
import Element.Background as Background
import Element.Font as Font
import Html exposing (Html)


type alias MenuItem =
    { label : String
    , url : String
    }


view : Page -> Element Msg
view currentPage =
    navMenu currentPage
        [ MenuItem (pageToString Home) "/"
        , MenuItem (pageToString Bump2Normal) ("/" ++ PageRouting.bumpToNormalSlug)
        , MenuItem (pageToString Lighting2Normal) ("/" ++ PageRouting.lightingToNormalSlug)
        , MenuItem (pageToString FontSheet) ("/" ++ PageRouting.fontSheetSlug)
        ]


navMenu : Page -> List MenuItem -> Element Msg
navMenu currentPage items =
    row [ Background.color Styles.darkPurple, width fill, spacing 20 ] (List.map (navItem currentPage) items)


navItem : Page -> MenuItem -> Element Msg
navItem currentPage item =
    let
        fontColor =
            if pageToString currentPage == item.label then
                Styles.white

            else
                Styles.lightPurple
    in
    link
        [ padding 10
        , Styles.pixelFont
        , Font.size 12
        , Font.color fontColor
        , mouseOver [ Background.color Styles.purple, Font.color Styles.white ]
        ]
        { url = item.url
        , label = text item.label
        }
