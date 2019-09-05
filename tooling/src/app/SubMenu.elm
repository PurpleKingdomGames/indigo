module App.SubMenu exposing (view)

import App.Msg exposing (Msg)
import App.PageRouting as PageRouting
import Html exposing (..)
import Html.Attributes exposing (href)


type alias MenuItem =
    { label : String
    , url : String
    }


view : Html Msg
view =
    navMenu
        [ MenuItem "Home" "/"
        , MenuItem "Bump To Normal" ("/" ++ PageRouting.bumpToNormalSlug)
        ]


navMenu : List MenuItem -> Html Msg
navMenu items =
    div []
        [ ul [] (List.map navItem items)
        ]


navItem : MenuItem -> Html Msg
navItem item =
    li []
        [ a [ href item.url ]
            [ text item.label ]
        ]
