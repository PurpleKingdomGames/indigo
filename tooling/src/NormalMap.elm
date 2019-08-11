module Main exposing (..)

import Browser
import Browser.Events exposing (onAnimationFrameDelta)
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick)
import Math.Matrix4 as Mat4 exposing (Mat4)
import Math.Vector3 as Vec3 exposing (Vec3, vec3)
import WebGL exposing (..)


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
    }


type alias ImageSize =
    { width : Int
    , height : Int
    }


type Msg
    = TimeDelta Float


initialModel : Model
initialModel =
    { size = { width = 359, height = 356 }
    }


init : () -> ( Model, Cmd Msg )
init =
    \() -> ( initialModel, Cmd.none )


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.none


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        TimeDelta _ ->
            ( model
            , Cmd.none
            )


view : Model -> Html.Html Msg
view model =
    div [ style "display" "block" ]
        [ modeSelectView
        , bumpSource model.size
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
            [ WebGL.entity vertexShader fragmentShader mesh { projection = projection }
            ]
        ]


projection : Mat4
projection =
    Mat4.makeOrtho -0.5 0.5 0.5 -0.5 -10000 10000



-- Mesh


type alias Vertex =
    { position : Vec3
    , color : Vec3
    }


mesh : Mesh Vertex
mesh =
    WebGL.triangleStrip
        [ Vertex (vec3 -0.5 -0.5 1) (vec3 1 0 0)
        , Vertex (vec3 -0.5 0.5 1) (vec3 0 1 0)
        , Vertex (vec3 0.5 -0.5 1) (vec3 0 0 1)
        , Vertex (vec3 0.5 0.5 1) (vec3 1 0 1)
        ]



-- Shaders


type alias Uniforms =
    { projection : Mat4 }


vertexShader : Shader Vertex Uniforms { vcolor : Vec3 }
vertexShader =
    [glsl|
        attribute vec3 position;
        attribute vec3 color;
        
        uniform mat4 projection;

        varying vec3 vcolor;
        
        void main () {
            gl_Position = projection * vec4(position, 1.0);
            vcolor = color;
        }
    |]


fragmentShader : Shader {} Uniforms { vcolor : Vec3 }
fragmentShader =
    [glsl|
        precision mediump float;
        
        varying vec3 vcolor;

        void main () {
            gl_FragColor = vec4(vcolor, 1.0);
        }
    |]
