module SubMenu exposing (subMenu)

import Html exposing (..)
import Html.Attributes exposing (href)
import Msg exposing (Msg)


type alias MenuItem =
    { label : String
    , url : String
    }


subMenu : Html Msg
subMenu =
    navMenu
        [ MenuItem "a" "/"
        , MenuItem "b" "/page-1"
        , MenuItem "c" "/page-2"
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
