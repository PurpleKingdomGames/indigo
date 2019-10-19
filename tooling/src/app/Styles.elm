module App.Styles exposing (..)

import Element exposing (Color, rgb255)
import Element.Font as Font


purple : Color
purple =
    rgb255 116 32 160


darkPurple : Color
darkPurple =
    rgb255 80 1 120


lightPurple : Color
lightPurple =
    rgb255 171 121 198


black : Color
black =
    rgb255 0 0 0


white : Color
white =
    rgb255 255 255 255


pixelFont =
    Font.family
        [ Font.typeface "pixelFont"
        , Font.sansSerif
        ]
