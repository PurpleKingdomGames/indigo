port module Modules.FontSheet exposing (FontSheet, FontSheetMsg, initialModel, update, view)

import App.Styles as Styles
import Browser
import Browser.Events exposing (onAnimationFrameDelta)
import Canvas
import Element exposing (..)
import Element.Background as Background
import Element.Border as Border
import Element.Events as Events
import Element.Font as Font
import Element.Input as Input
import File exposing (File)
import File.Select as Select
import Html as H exposing (Html)
import Html.Attributes as HA
import Html.Events as HE exposing (onClick)
import Html.Events.Extra.Drag as HEE exposing (DropEffect(..), DropTargetConfig, Event, onDropTarget)
import Math.Matrix4 as Mat4 exposing (Mat4)
import Math.Vector2 as Vec2 exposing (Vec2, vec2)
import Math.Vector3 as Vec3 exposing (Vec3, vec3)
import Task
import WebGL exposing (..)
import WebGL.Texture as Texture exposing (..)


type alias FontSheet =
    { size : Int
    , fontPath : Maybe String
    , fontName : Maybe String
    , asciiOnly : Bool
    }


type FontSheetMsg
    = FontSizeUpdated Float
    | FontUploadRequested
    | FontUploadSelected File
    | FontUploadLoaded String
    | PreventDefault


port onDownload : String -> Cmd msg


initialModel : FontSheet
initialModel =
    { size = 16
    , fontPath = Nothing
    , fontName = Nothing
    , asciiOnly = True
    }


update : FontSheetMsg -> FontSheet -> ( FontSheet, Cmd FontSheetMsg )
update msg model =
    case msg of
        FontSizeUpdated value ->
            ( { model | size = round value }, Cmd.none )

        FontUploadRequested ->
            ( model
            , Select.file [ "font/opentype", "font/woff", "font/ttf", "font/otf" ] FontUploadSelected
            )

        FontUploadSelected file ->
            ( { model | fontName = Just (File.name file) }
            , Task.perform FontUploadLoaded (File.toUrl file)
            )

        FontUploadLoaded content ->
            ( { model | fontPath = Just content }, Cmd.none )

        PreventDefault ->
            ( model, Cmd.none )


view : FontSheet -> Element FontSheetMsg
view model =
    column
        [ padding 10, spacing 10 ]
        [ row []
            [ chooseOptions model ]
        ]


chooseOptions : FontSheet -> Element FontSheetMsg
chooseOptions model =
    column
        []
        [ row [ spacing 20, width (Element.px 300) ]
            [ text
                ("Font"
                    ++ (case model.fontName of
                            Just s ->
                                " (" ++ s ++ ")"

                            Nothing ->
                                ""
                       )
                )
            , Input.button [ Font.color Styles.lightPurple ]
                { onPress = Just FontUploadRequested
                , label = text "Upload"
                }
            ]
        , row [ spacing 20, width (Element.px 300) ]
            [ text
                ("Size ("
                    ++ (if model.size < 10 then
                            "0"

                        else
                            ""
                       )
                    ++ String.fromInt model.size
                    ++ "px)"
                )
            , Input.slider
                [ Element.height (Element.px 30)
                , Element.behindContent
                    (Element.el
                        [ Element.width Element.fill
                        , Element.height (Element.px 2)
                        , Element.centerY
                        , Background.color (Element.rgb255 238 238 238)
                        , Border.rounded 2
                        ]
                        Element.none
                    )
                ]
                { label = Input.labelLeft [] (text ""), min = 5, max = 99, step = Just 1, thumb = Input.defaultThumb, onChange = FontSizeUpdated, value = toFloat model.size }
            ]
        ]
