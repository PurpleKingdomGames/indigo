module App.SubMenu exposing (view)

import App.Msg exposing (Msg)
import App.PageRouting as PageRouting
import Element exposing (..)
import Html exposing (Html)


type alias MenuItem =
    { label : String
    , url : String
    }


view : Html Msg
view =
    Element.layout [] <|
        navMenu
            [ MenuItem "Home" "/"
            , MenuItem "Bump To Normal" ("/" ++ PageRouting.bumpToNormalSlug)
            , MenuItem "Lighting To Normal" ("/" ++ PageRouting.lightingToNormalSlug)
            , MenuItem "Font Sheet" ("/" ++ PageRouting.fontSheetSlug)
            ]


navMenu : List MenuItem -> Element Msg
navMenu items =
    row [] (List.map navItem items)


navItem : MenuItem -> Element Msg
navItem item =
    link [ spacing 20, padding 10 ]
        { url = item.url
        , label = text item.label
        }
