module CheckboxComponent exposing (..)

import Html exposing (..)
import Html.Events exposing (onCheck)
import Html.Attributes exposing (..)


type alias CheckboxModel =
    Bool


initial : Bool -> CheckboxModel
initial b =
    b


type CheckboxMsg
    = Flip Bool


update : CheckboxMsg -> CheckboxModel -> CheckboxModel
update msg model =
    case msg of
        Flip v ->
            v


view : String -> CheckboxModel -> Html CheckboxMsg
view label model =
    div []
        [ text label
        , input [ type_ "checkbox", value (toString model), onCheck Flip ] []
        ]
