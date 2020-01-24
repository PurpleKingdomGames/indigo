port module Modules.FontSheet exposing (FontSheet, FontSheetMsg, initialModel, subscriptions, update, view)

import App.Styles as Styles
import Browser
import Browser.Events exposing (onAnimationFrameDelta)
import Bytes exposing (Bytes)
import Canvas
import Element exposing (..)
import Element.Background as Background
import Element.Border as Border
import Element.Events as Events
import Element.Font as Font
import Element.Input as Input
import File exposing (File)
import File.Download as Download
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
    { base : BaseFontSheet
    , fontData : Maybe FontLoadInfo
    }


type alias BaseFontSheet =
    { size : Int
    , fontPath : Maybe String
    , fontName : Maybe String
    , renderType : Int
    , padding : Int
    , specificGlyphs : Maybe String
    }


type alias FontMapData =
    { texture : String
    , mapJson : String
    , fontName : String
    }


type FontRenderType
    = AsciiOnly
    | AllGlyphs
    | SpecificGlyphs


type FontLoadInfo
    = Ok FontMapData
    | Err String


type FontSheetMsg
    = FontSizeUpdated Float
    | RenderTypeUpdated FontRenderType
    | SpecificGlyphsUpdated String
    | PaddingUpdated Float
    | FontUploadRequested
    | FontUploadSelected File
    | FontUploadLoaded String
    | FontProcessed FontMapData
    | FontProcessErr String
    | DownloadText String String
    | PreventDefault


port processFont : BaseFontSheet -> Cmd msg


port fontProcessed : (FontMapData -> msg) -> Sub msg


port fontProcessErr : (String -> msg) -> Sub msg


initialModel : FontSheet
initialModel =
    { base =
        { size = 16
        , fontPath = Nothing
        , fontName = Nothing
        , renderType = 0
        , padding = 1
        , specificGlyphs = Nothing
        }
    , fontData = Nothing
    }


update : FontSheetMsg -> FontSheet -> ( FontSheet, Cmd FontSheetMsg )
update msg model =
    let
        base =
            model.base
    in
    case msg of
        FontSizeUpdated value ->
            processNewFontBase model { base | size = round value }

        RenderTypeUpdated value ->
            processNewFontBase model { base | renderType = renderTypeToInt value }

        SpecificGlyphsUpdated value ->
            processNewFontBase model { base | specificGlyphs = Just value }

        PaddingUpdated value ->
            processNewFontBase model { base | padding = round value }

        FontUploadRequested ->
            ( model
            , Select.file [ "font/opentype", "font/woff", "font/ttf", "font/otf" ] FontUploadSelected
            )

        FontUploadSelected file ->
            let
                ( newModel, cmd ) =
                    processNewFontBase model { base | fontName = Just (File.name file) }

                extension =
                    String.toLower (String.right 4 (File.name file))
            in
            if List.any (\a -> a == extension) [ ".otf", ".ttf", "woff" ] then
                ( newModel, Cmd.batch [ cmd, Task.perform FontUploadLoaded (File.toUrl file) ] )

            else
                ( model, Cmd.none )

        FontUploadLoaded content ->
            processNewFontBase model { base | fontPath = Just content }

        FontProcessed info ->
            ( { model | fontData = Just (Ok info) }, Cmd.none )

        FontProcessErr err ->
            ( { model | fontData = Just (Err err) }, Cmd.none )

        DownloadText name str ->
            ( model, Download.string name "text/plain" str )

        PreventDefault ->
            ( model, Cmd.none )


view : FontSheet -> Element FontSheetMsg
view model =
    column
        [ padding 10, spacing 10 ]
        [ row []
            [ chooseOptions model.base
            ]
        , row []
            [ previewFont model
            ]
        ]


chooseOptions : BaseFontSheet -> Element FontSheetMsg
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
        , row [ spacing 20, width (Element.px 300) ]
            [ text
                ("Padding ("
                    ++ (if model.padding < 10 then
                            "0"

                        else
                            ""
                       )
                    ++ String.fromInt model.padding
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
                { label = Input.labelLeft [] (text ""), min = 0, max = 10, step = Just 1, thumb = Input.defaultThumb, onChange = PaddingUpdated, value = toFloat model.padding }
            ]
        , row [ spacing 20, width fill ]
            [ Input.radio
                [ padding 10
                , spacing 20
                ]
                { onChange = RenderTypeUpdated
                , selected = Just (intToRenderType model.renderType)
                , label = Input.labelAbove [] (text "")
                , options =
                    [ Input.option AsciiOnly (text "ASCII")
                    , Input.option SpecificGlyphs (text "Specified Glyphs")
                    , Input.option AllGlyphs (text "All Glyphs")
                    ]
                }
            ]
        , row [ spacing 20, width fill ]
            (case intToRenderType model.renderType of
                SpecificGlyphs ->
                    [ text "Glyphs"
                    , Input.text
                        [ padding 10
                        , spacing 20
                        , Font.color (Element.rgb 0 0 0)
                        ]
                        { onChange = SpecificGlyphsUpdated
                        , text =
                            case model.specificGlyphs of
                                Just str ->
                                    str

                                Nothing ->
                                    ""
                        , placeholder = Nothing
                        , label = Input.labelLeft [] (text "")
                        }
                    ]

                _ ->
                    []
            )
        ]


previewFont : FontSheet -> Element FontSheetMsg
previewFont model =
    column [ spacing 10 ]
        [ row [ width fill, spacing 10 ]
            [ Element.el
                [ Border.solid
                , Border.width 2
                , Border.color Styles.purple
                , width (px 512)
                , height (px 512)
                ]
                (row [ centerX, centerY ]
                    (case model.fontData of
                        Just (Ok info) ->
                            [ Element.html <|
                                H.img
                                    [ HA.src info.texture
                                    , HA.style "max-width" "508px"
                                    , HA.style "max-height" "508px"
                                    , HA.style "object-fit" "contain"
                                    , HA.style "filter" "invert(100%)"
                                    ]
                                    []
                            ]

                        Just (Err err) ->
                            [ paragraph [] [ text err ] ]

                        Nothing ->
                            []
                    )
                )
            , Element.el
                [ Border.solid
                , Border.width 2
                , Border.color Styles.purple
                , width (px 512)
                , height (px 512)
                , scrollbarY
                ]
                (row [ centerX, centerY ]
                    (case model.fontData of
                        Just (Ok info) ->
                            [ text info.mapJson ]

                        Just (Err err) ->
                            []

                        Nothing ->
                            []
                    )
                )
            ]
        , row [ width fill, spacing 10 ]
            (case model.fontData of
                Just (Ok info) ->
                    [ Element.el [ width (px 512) ]
                        (Element.html
                            (H.a [ HA.href info.texture, HA.download (info.fontName ++ ".png"), HA.target "_blank" ] [ H.text "Download" ])
                        )
                    , Element.el [ width (px 512) ]
                        (Element.html
                            (H.a [ HA.href "", HE.onClick <| DownloadText (info.fontName ++ ".json") info.mapJson ] [ H.text "Download" ])
                        )
                    ]

                Just (Err err) ->
                    []

                Nothing ->
                    []
            )
        ]


renderTypeToInt : FontRenderType -> Int
renderTypeToInt renderType =
    case renderType of
        AsciiOnly ->
            0

        SpecificGlyphs ->
            1

        AllGlyphs ->
            2


intToRenderType : Int -> FontRenderType
intToRenderType i =
    case i of
        0 ->
            AsciiOnly

        1 ->
            SpecificGlyphs

        _ ->
            AllGlyphs


processNewFontBase : FontSheet -> BaseFontSheet -> ( FontSheet, Cmd FontSheetMsg )
processNewFontBase model base =
    ( { model | base = base }, processFont base )


subscriptions : Sub FontSheetMsg
subscriptions =
    Sub.batch
        [ fontProcessed FontProcessed
        , fontProcessErr FontProcessErr
        ]
