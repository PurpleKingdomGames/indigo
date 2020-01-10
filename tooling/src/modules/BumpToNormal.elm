port module Modules.BumpToNormal exposing (BumpToNormal, BumpToNormalMsg, initialModel, update, view)

import App.Styles as Styles
import Browser
import Browser.Events exposing (onAnimationFrameDelta)
import Canvas
import Element exposing (..)
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


type alias BumpToNormal =
    { size : ImageSize
    , texture : Maybe Texture
    , imagePath : Maybe String
    }


type alias ImageSize =
    { width : Int
    , height : Int
    }


type BumpToNormalMsg
    = TextureLoaded (Result Error Texture)
    | Download String
    | SwapToImage ImageDetails
    | ImageUploadRequested
    | ImageUploadSelected File
    | ImageUploadLoaded String
    | PreventDefault


type alias ImageDetails =
    { width : Int
    , height : Int
    , path : String
    }


port onDownload : String -> Cmd msg


file1 : ImageDetails
file1 =
    { width = 359
    , height = 356
    , path = "/static/assets/bump-example.jpg"
    }


file2 : ImageDetails
file2 =
    { width = 300
    , height = 300
    , path = "/static/assets/shapes.png"
    }


initialModel : BumpToNormal
initialModel =
    { size = { width = 100, height = 100 }
    , texture = Nothing
    , imagePath = Nothing
    }


initialiseModelWithFile : ImageDetails -> BumpToNormal
initialiseModelWithFile details =
    { size = { width = details.width, height = details.height }
    , texture = Nothing
    , imagePath = Just details.path
    }


loadSpecificImage : ImageDetails -> Cmd BumpToNormalMsg
loadSpecificImage details =
    Task.attempt TextureLoaded
        (Texture.loadWith
            { magnify = linear
            , minify = nearest
            , horizontalWrap = clampToEdge
            , verticalWrap = clampToEdge
            , flipY = True
            }
            details.path
        )


update : BumpToNormalMsg -> BumpToNormal -> ( BumpToNormal, Cmd BumpToNormalMsg )
update msg model =
    case msg of
        TextureLoaded (Ok textureResult) ->
            let
                width =
                    case Texture.size textureResult of
                        ( w, _ ) ->
                            w

                height =
                    case Texture.size textureResult of
                        ( _, h ) ->
                            h
            in
            ( { model | texture = Just textureResult, size = { width = width, height = height } }, Cmd.none )

        TextureLoaded (Err LoadError) ->
            ( model, Cmd.none )

        TextureLoaded (Err (SizeError w h)) ->
            ( model, Cmd.none )

        Download canvasId ->
            ( model, onDownload canvasId )

        SwapToImage details ->
            ( { model
                | size = { width = details.width, height = details.height }
                , imagePath = Just details.path
                , texture = Nothing
              }
            , loadSpecificImage details
            )

        ImageUploadRequested ->
            ( model
            , Select.file [ "image/png", "image/jpeg" ] ImageUploadSelected
            )

        ImageUploadSelected file ->
            ( model
            , Task.perform ImageUploadLoaded (File.toUrl file)
            )

        ImageUploadLoaded content ->
            ( { model | imagePath = Just content }
            , loadSpecificImage { width = 0, height = 0, path = content }
            )

        PreventDefault ->
            ( model, Cmd.none )


view : BumpToNormal -> Element BumpToNormalMsg
view model =
    column [ padding 10, spacing 10 ]
        [ row []
            [ chooseImage ]
        , holder model
        ]


holder : BumpToNormal -> Element BumpToNormalMsg
holder model =
    row [ width fill, spacing 10 ]
        [ boxHolder uploadLink (bumpSource model)
        , text ">>"
        , boxHolder downloadLink (outputCanvas model)
        ]


boxHolder : Element BumpToNormalMsg -> Element BumpToNormalMsg -> Element BumpToNormalMsg
boxHolder link content =
    column [ spacing 10 ]
        [ Element.el
            ([ Border.solid
             , Border.width 2
             , Border.color Styles.purple
             , width (px 512)
             , height (px 512)
             ]
                ++ initDropzone
            )
            (row [ centerX, centerY ] [ content ])
        , Element.el [] link
        ]


chooseImage : Element BumpToNormalMsg
chooseImage =
    row [ spacing 20 ]
        [ text "Try a sample image?"
        , Input.button [ Font.color Styles.lightPurple ]
            { onPress = Just (SwapToImage file1)
            , label = text "Weave"
            }
        , Input.button [ Font.color Styles.lightPurple ]
            { onPress = Just (SwapToImage file2)
            , label = text "Shapes"
            }
        ]


bumpSource : BumpToNormal -> Element BumpToNormalMsg
bumpSource model =
    case model.imagePath of
        Just path ->
            Element.html <|
                H.img
                    [ HA.src path
                    , HA.style "max-width" "508px"
                    , HA.style "max-height" "508px"
                    , HA.style "object-fit" "contain"
                    ]
                    []

        Nothing ->
            text "No source image.\n\nUpload a file or drop an image here."


outputCanvas : BumpToNormal -> Element BumpToNormalMsg
outputCanvas model =
    model.texture
        |> Maybe.map
            (\tx ->
                Element.html
                    (WebGL.toHtmlWith
                        [ clearColor 0 1 0 1
                        , WebGL.alpha False
                        , preserveDrawingBuffer
                        ]
                        [ HA.width model.size.width
                        , HA.height model.size.height
                        , HA.style "display" "block"
                        , HA.id "image-output"
                        , HA.style "max-width" "508px"
                        , HA.style "max-height" "508px"
                        , HA.style "object-fit" "contain"
                        ]
                        [ WebGL.entity vertexShader fragmentShader mesh { projection = projection model.size, transform = transform model.size, texture = tx, size = imageSizeToVec2 model.size }
                        ]
                    )
            )
        |> (Maybe.withDefault <|
                text "Texture not loaded"
           )


uploadLink : Element BumpToNormalMsg
uploadLink =
    link [ Events.onClick ImageUploadRequested ]
        { url = "#"
        , label = text "upload"
        }


downloadLink : Element BumpToNormalMsg
downloadLink =
    Element.html
        (H.a [ HA.id "downloadLink", HA.href "", HA.download "normal-map.png", HA.target "_blank", HE.onClick <| Download "image-output" ] [ H.text "download me" ])


imageSizeToVec2 : ImageSize -> Vec2
imageSizeToVec2 size =
    vec2 (toFloat size.width) (toFloat size.height)


projection : ImageSize -> Mat4
projection size =
    Mat4.makeOrtho 0 (toFloat size.width) (toFloat size.height) 0 -10000 10000


transform : ImageSize -> Mat4
transform size =
    Mat4.identity
        |> Mat4.scale (vec3 (toFloat size.width) (toFloat size.height) 1)
        |> Mat4.translate (vec3 0.5 0.5 1)
        |> Mat4.scale (vec3 1 -1 1)


initDropzone : List (Attribute BumpToNormalMsg)
initDropzone =
    HEE.onDropTarget (DropTargetConfig CopyOnDrop (\_ _ -> PreventDefault) processImageDrop Nothing Nothing)
        |> List.map (\a -> htmlAttribute a)


processImageDrop : Event -> BumpToNormalMsg
processImageDrop event =
    processFileList event.dataTransfer.files


processFileList : List File -> BumpToNormalMsg
processFileList files =
    case files of
        file :: others ->
            case File.mime file of
                "image/png" ->
                    ImageUploadSelected file

                "image/jpeg" ->
                    ImageUploadSelected file

                _ ->
                    processFileList others

        _ ->
            PreventDefault


type alias Vertex =
    { position : Vec3
    , coord : Vec2
    }


mesh : Mesh Vertex
mesh =
    WebGL.triangleStrip
        [ Vertex (vec3 -0.5 -0.5 1) (vec2 0 0)
        , Vertex (vec3 -0.5 0.5 1) (vec2 0 1)
        , Vertex (vec3 0.5 -0.5 1) (vec2 1 0)
        , Vertex (vec3 0.5 0.5 1) (vec2 1 1)
        ]



-- Shaders


type alias Uniforms =
    { projection : Mat4
    , transform : Mat4
    , texture : Texture
    , size : Vec2
    }


vertexShader : Shader Vertex Uniforms { vcoord : Vec2, vsize : Vec2 }
vertexShader =
    [glsl|
        attribute vec3 position;
        attribute vec2 coord;

        uniform mat4 projection;
        uniform mat4 transform;
        uniform vec2 size;

        varying vec2 vcoord;
        varying vec2 vsize;

        void main () {
            gl_Position = projection * transform * vec4(position, 1.0);
            vcoord = coord;
            vsize = size;
        }
    |]


fragmentShader : Shader {} Uniforms { vcoord : Vec2, vsize : Vec2 }
fragmentShader =
    [glsl|
        precision mediump float;

        uniform sampler2D texture;

        varying vec2 vcoord;
        varying vec2 vsize;

        float strength = 0.5;
        float minLevel = 0.0;
        float maxLevel = 255.0;
        float gamma = 1.5;

        float grayscale(vec4 colour) {
          return (colour.r + colour.g + colour.b) / 3.0;
        }

        float makeSample(vec2 at) {
          return grayscale(texture2D(texture, at));
        }

        float rateOfChange(float sample1, float sample2, float sample3) {
          return ((sample1 - sample2) + (sample2 - sample3)) / strength;
        }

        float levelRange(float color, float minInput, float maxInput){
            return min(max(color - minInput, 0.0) / (maxInput - minInput), 1.0);
        }

        float gammaCorrect(float value, float gamma){
          return pow(value, 1.0 / gamma);
        }

        float finalLevels(float color, float minInput, float gamma, float maxInput){
            return gammaCorrect(levelRange(color, minInput, maxInput), gamma);
        }

        void main () {

          float oneWidth = 1.0 / vsize.x;
          float oneHeight = 1.0 / vsize.y;

          float r = rateOfChange(
            makeSample(vec2(vcoord.x - oneWidth, vcoord.y)),
            makeSample(vcoord),
            makeSample(vec2(vcoord.x + oneWidth, vcoord.y))
          );

          float g = rateOfChange(
            makeSample(vec2(vcoord.x, vcoord.y - oneHeight)),
            makeSample(vcoord),
            makeSample(vec2(vcoord.x, vcoord.y + oneHeight))
          );

          float b = 1.0 - ((r + g) / 2.0);

          float rGamma = finalLevels(r + 0.5, minLevel/255.0, gamma, maxLevel/255.0);
          float gGamma = finalLevels(g + 0.5, minLevel/255.0, gamma, maxLevel/255.0);

          gl_FragColor = vec4(rGamma, gGamma, b, 1.0);
        }
    |]
