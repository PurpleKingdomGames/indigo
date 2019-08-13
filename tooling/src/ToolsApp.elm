module Main exposing (..)

import Browser exposing (..)
import Browser.Navigation as Nav
import Html exposing (..)
import Url exposing (Url)


type alias Msg =
    Int


type alias Model =
    String


type alias Flags =
    ()


main : Program Flags Model Msg
main =
    Browser.application
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        , onUrlRequest = onUrlRequest
        , onUrlChange = onUrlChange
        }


init : Flags -> Url -> Nav.Key -> ( Model, Cmd Msg )
init _ _ _ =
    ( "erm...", Cmd.none )


view : Model -> Document Msg
view _ =
    { title = "Dave's test app"
    , body = [ div [] [ text "fish" ] ]
    }


update : Msg -> Model -> ( Model, Cmd Msg )
update _ model =
    ( model, Cmd.none )


subscriptions : Model -> Sub Msg
subscriptions _ =
    Sub.none


onUrlRequest : UrlRequest -> Msg
onUrlRequest _ =
    0


onUrlChange : Url -> Msg
onUrlChange _ =
    1



{-
   application :
     { init : flags -> Url -> Key -> ( model, Cmd msg )
     , view : model -> Document msg
     , update : msg -> model -> ( model, Cmd msg )
     , subscriptions : model -> Sub msg
     , onUrlRequest : UrlRequest -> msg
     , onUrlChange : Url -> msg
     }
     -> Program flags model msg
-}
