module Main exposing (..)

import App.Model exposing (..)
import App.Msg exposing (..)
import App.PageRouting as PageRouting
import App.ScalaJSMailbox as ScalaJSMailbox
import App.Styles as Styles
import App.SubMenu as SubMenu
import Browser exposing (..)
import Browser.Navigation as Nav
import Element exposing (..)
import Element.Background as Background
import Element.Font as Font
import Html exposing (..)
import Html.Attributes exposing (href)
import Modules.BumpToNormal as BumpToNormal
import Modules.FontSheet as FontSheet exposing (subscriptions)
import Url exposing (Url)


type alias Flags =
    {}


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
    ( Model Home navKey BumpToNormal.initialModel FontSheet.initialModel, Cmd.none )


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        UrlChange url ->
            PageRouting.urlUpdate url model

        ClickedLink (Browser.Internal url) ->
            ( model, Nav.pushUrl model.navKey <| Url.toString url )

        ClickedLink (Browser.External href) ->
            ( model, Nav.load href )

        BumpToNormalMsgWrapper b2nMsg ->
            case BumpToNormal.update b2nMsg model.bumpToNormal of
                ( m, cmd ) ->
                    ( { model | bumpToNormal = m }, Cmd.map (\e -> BumpToNormalMsgWrapper e) cmd )

        FontSheetMsgWrapper fsMsg ->
            case FontSheet.update fsMsg model.fontSheet of
                ( m, cmd ) ->
                    ( { model | fontSheet = m }, Cmd.map (\e -> FontSheetMsgWrapper e) cmd )

        LogMessage m ->
            ( model, ScalaJSMailbox.send <| LogIt m )

        ScalaCallback Ignore ->
            ( model, Cmd.none )


view : Model -> Document Msg
view model =
    { title = "Indigo Tools"
    , body = [ Element.layout [ Background.color Styles.black, Styles.pixelFont ] (basicLayout model) ]
    }


basicLayout : Model -> Element Msg
basicLayout model =
    column [ width fill, Font.color Styles.white, Font.size 12 ]
        [ SubMenu.view model.page
        , PageRouting.pageContent model
        ]


subscriptions : Model -> Sub Msg
subscriptions _ =
    Sub.batch
        [ Sub.map (\m -> ScalaCallback m) ScalaJSMailbox.receive
        , Sub.map (\m -> FontSheetMsgWrapper m) FontSheet.subscriptions
        ]
