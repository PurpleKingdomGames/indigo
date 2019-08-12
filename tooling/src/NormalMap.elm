module Main exposing (..)

import Browser
import Browser.Events exposing (onAnimationFrameDelta)
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


main : Program () Model Msg
main =
    Browser.element
        { init = init
        , subscriptions = subscriptions
        , update = update
        , view = view
        }


type alias Model =
    { size : ImageSize
    , texture : Maybe Texture
    }


type alias ImageSize =
    { width : Int
    , height : Int
    }


type Msg
    = TextureLoaded (Result Error Texture)


initialModel : Model
initialModel =
    { size = { width = 359, height = 356 }
    , texture = Nothing
    }


init : () -> ( Model, Cmd Msg )
init =
    \() ->
        ( initialModel
        , Task.attempt TextureLoaded
            (Texture.loadWith
                { magnify = linear
                , minify = nearest
                , horizontalWrap = clampToEdge
                , verticalWrap = clampToEdge
                , flipY = True
                }
                "assets/bump-example.jpg"
            )
        )


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.none


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        TextureLoaded (Ok textureResult) ->
            ( { model | texture = Just textureResult }, Cmd.none )

        TextureLoaded (Err LoadError) ->
            log "Couldn't load the texture"
                ( model, Cmd.none )

        TextureLoaded (Err (SizeError w h)) ->
            log
                ("Couldn't load the texture, size error: "
                    ++ String.fromInt w
                    ++ " x "
                    ++ String.fromInt h
                )
                ( model, Cmd.none )


view : Model -> Html.Html Msg
view model =
    div [ style "display" "block" ]
        [ bumpSource model.size
        , outputCanvas model
        ]


modeSelectView : Html.Html Msg
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


bumpSource : ImageSize -> Html.Html Msg
bumpSource size =
    div [ style "display" "block" ]
        [ img [ src "assets/bump-example.jpg", width size.width, height size.height ] []
        ]


outputCanvas : Model -> Html.Html Msg
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
                        ]
                        [ WebGL.entity vertexShader fragmentShader mesh { projection = projection model.size, transform = transform model.size, texture = tx, size = imageSizeToVec2 model.size }
                        ]
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

        float grayscale(vec4 colour) {
          return (colour.r + colour.g + colour.b) / 3.0;
        }

        float makeSample(vec2 at) {
          return grayscale(texture2D(texture, at));
        }

        float rateOfChange(float sample1, float sample2, float sample3) {
          return (((sample2 - sample1) + (sample3 - sample2)) / 2.0) + 0.5;
        }

        void main () {

          float oneWidth = 1.0 / vsize.x;
          float oneHeight = 1.0 / vsize.y;

          float r = rateOfChange(makeSample(vec2(vcoord.x + oneWidth, vcoord.y)), makeSample(vcoord), makeSample(vec2(vcoord.x - oneWidth, vcoord.y)));
          float g = rateOfChange(makeSample(vec2(vcoord.x, vcoord.y - oneHeight)), makeSample(vcoord), makeSample(vec2(vcoord.x, vcoord.y + oneHeight)));
          float b = 1.0;

          gl_FragColor = vec4(r, g, b, 1.0);
        }
    |]
