module Modules.BumpToNormal exposing (BumpToNormal, BumpToNormalMsg, initialModel, loadImage, update, view)

import Browser
import Browser.Events exposing (onAnimationFrameDelta)
import Canvas
import Debug exposing (log)
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick)
import Math.Matrix4 as Mat4 exposing (Mat4)
import Math.Vector2 as Vec2 exposing (Vec2, vec2)
import Math.Vector3 as Vec3 exposing (Vec3, vec3)
import Task
import WebGL exposing (..)
import WebGL.Texture as Texture exposing (..)


type alias BumpToNormal =
    { size : ImageSize
    , texture : Maybe Texture
    , imagePath : String
    }


type alias ImageSize =
    { width : Int
    , height : Int
    }


type BumpToNormalMsg
    = TextureLoaded (Result Error Texture)
    | Download


type alias ImageDetails =
    { width : Int
    , height : Int
    , path : String
    }


file1 : ImageDetails
file1 =
    { width = 359
    , height = 356
    , path = "/src/assets/bump-example.jpg"
    }


file2 : ImageDetails
file2 =
    { width = 300
    , height = 300
    , path = "/src/assets/untitled.png"
    }


currentFile : ImageDetails
currentFile =
    file1


initialModel : BumpToNormal
initialModel =
    initialiseModelWithFile currentFile


initialiseModelWithFile : ImageDetails -> BumpToNormal
initialiseModelWithFile details =
    { size = { width = details.width, height = details.height }
    , imagePath = details.path
    , texture = Nothing
    }


loadImage : Cmd BumpToNormalMsg
loadImage =
    Task.attempt TextureLoaded
        (Texture.loadWith
            { magnify = linear
            , minify = nearest
            , horizontalWrap = clampToEdge
            , verticalWrap = clampToEdge
            , flipY = True
            }
            currentFile.path
        )

update : BumpToNormalMsg -> BumpToNormal -> BumpToNormal
update msg model =
    case msg of
        TextureLoaded (Ok textureResult) ->
            { model | texture = Just textureResult }

        TextureLoaded (Err LoadError) ->
            log "Couldn't load the texture"
                model

        TextureLoaded (Err (SizeError w h)) ->
            log
                ("Couldn't load the texture, size error: "
                    ++ String.fromInt w
                    ++ " x "
                    ++ String.fromInt h
                )
                model

        Download ->
            model


view : BumpToNormal -> Html.Html BumpToNormalMsg
view model =
    div [ style "display" "block" ]
        [ bumpSource model
        , outputCanvas model
        ]


modeSelectView : Html.Html BumpToNormalMsg
modeSelectView =
    div [ style "display" "block" ]
        [ text "Choose a mode: "
        , br [] []
        , input [ type_ "radio", name "mode", value "bump mode" ] []
        , text "Bump mode!"
        , br [] []
        , input [ type_ "radio", name "mode", value "lighting mode" ] []
        , text "Lighting mode!"
        ]


bumpSource : BumpToNormal -> Html.Html BumpToNormalMsg
bumpSource model =
    div [ style "display" "block" ]
        [ img [ src model.imagePath, width model.size.width, height model.size.height ] []
        ]


outputCanvas : BumpToNormal -> Html.Html BumpToNormalMsg
outputCanvas model =
    model.texture
        |> Maybe.map
            (\tx ->
                div
                    [ style "display" "block"
                    ]
                    [ WebGL.toHtmlWith
                        [ clearColor 0 1 0 1
                        , alpha False
                        ]
                        [ width model.size.width
                        , height model.size.height
                        , style "display" "block"
                        , id "image-output"
                        ]
                        [ WebGL.entity vertexShader fragmentShader mesh { projection = projection model.size, transform = transform model.size, texture = tx, size = imageSizeToVec2 model.size }
                        ]
                    , input [ type_ "submit", value "Download", onClick Download ] []
                    ]
            )
        |> Maybe.withDefault (div [] [ text "Texture not loaded" ])


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
