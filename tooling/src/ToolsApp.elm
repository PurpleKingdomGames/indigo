module Main exposing (..)

import Browser exposing (..)
import Browser.Navigation as Nav
import Html exposing (..)
import Html.Attributes exposing (href)
import Url exposing (Url)
import Url.Parser as UrlParser exposing ((</>), Parser, s, top)


type Msg
    = UrlChange Url
    | ClickedLink UrlRequest


type Page
    = Home
    | Page1
    | Page2
    | NotFound


type alias Model =
    { page : Page
    , navKey : Nav.Key
    }


type alias Flags =
    {}


type alias MenuItem =
    { label : String
    , url : String
    }


main : Program Flags Model Msg
main =
    Browser.application
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        , onUrlRequest = ClickedLink
        , onUrlChange = UrlChange
        }


init : Flags -> Url -> Nav.Key -> ( Model, Cmd Msg )
init _ _ navKey =
    ( Model Home navKey, Cmd.none )


urlUpdate : Url -> Model -> ( Model, Cmd Msg )
urlUpdate url model =
    case decode url of
        Nothing ->
            ( { model | page = NotFound }, Cmd.none )

        Just route ->
            ( { model | page = route }, Cmd.none )


decode : Url -> Maybe Page
decode url =
    UrlParser.parse routeParser url


routeParser : Parser (Page -> a) a
routeParser =
    UrlParser.oneOf
        [ UrlParser.map Home top
        , UrlParser.map Page1 (s "page-1")
        , UrlParser.map Page2 (s "page-2")
        ]


view : Model -> Document Msg
view model =
    { title = "Dave's test app"
    , body =
        [ div []
            [ text "Dave's App"
            , navMenu
                [ MenuItem "a" "/"
                , MenuItem "b" "/page-1"
                , MenuItem "c" "/page-2"
                ]
            , pageContent model.page
            ]
        ]
    }


navMenu : List MenuItem -> Html.Html Msg
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


pageContent : Page -> Html Msg
pageContent page =
    case page of
        Home ->
            text "Home"

        Page1 ->
            text "Page 1"

        Page2 ->
            text "Page 2"

        NotFound ->
            text "Not Found"


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        UrlChange url ->
            urlUpdate url model

        ClickedLink (Browser.Internal url) ->
            ( model, Nav.pushUrl model.navKey <| Url.toString url )

        ClickedLink (Browser.External href) ->
            ( model, Nav.load href )


subscriptions : Model -> Sub Msg
subscriptions _ =
    Sub.none



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
