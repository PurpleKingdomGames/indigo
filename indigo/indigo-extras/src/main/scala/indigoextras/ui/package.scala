package indigoextras.ui

import indigo.shared.Outcome
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.Layer

object syntax:

  extension [F[ReferenceData], ReferenceData](
      component: F[ReferenceData]
  )(using c: Component[F[ReferenceData], ReferenceData])
    def bounds(context: UIContext[ReferenceData]): Bounds =
      c.bounds(context, component)

    def update(
        context: UIContext[ReferenceData]
    ): GlobalEvent => Outcome[F[ReferenceData]] =
      c.updateModel(context, component)

    def present(
        context: UIContext[ReferenceData]
    ): Outcome[Layer] =
      c.present(context, component)

    def refresh(
        context: UIContext[ReferenceData]
    ): F[ReferenceData] =
      c.refresh(context, component)

// Component

type Component[A, ReferenceData] = indigoextras.ui.component.Component[A, ReferenceData]
val Component: indigoextras.ui.component.Component.type = indigoextras.ui.component.Component

// Components

type Button[ReferenceData] = indigoextras.ui.components.Button[ReferenceData]
val Button: indigoextras.ui.components.Button.type = indigoextras.ui.components.Button

type ComponentGroup[ReferenceData] = indigoextras.ui.components.ComponentGroup[ReferenceData]
val ComponentGroup: indigoextras.ui.components.ComponentGroup.type = indigoextras.ui.components.ComponentGroup

type ComponentList[ReferenceData] = indigoextras.ui.components.ComponentList[ReferenceData]
val ComponentList: indigoextras.ui.components.ComponentList.type = indigoextras.ui.components.ComponentList

type HitArea[ReferenceData] = indigoextras.ui.components.HitArea[ReferenceData]
val HitArea: indigoextras.ui.components.HitArea.type = indigoextras.ui.components.HitArea

type Input[ReferenceData] = indigoextras.ui.components.Input[ReferenceData]
val Input: indigoextras.ui.components.Input.type = indigoextras.ui.components.Input

type Label[ReferenceData] = indigoextras.ui.components.Label[ReferenceData]
val Label: indigoextras.ui.components.Label.type = indigoextras.ui.components.Label

type MaskedPane[A, ReferenceData] = indigoextras.ui.components.MaskedPane[A, ReferenceData]
val MaskedPane: indigoextras.ui.components.MaskedPane.type = indigoextras.ui.components.MaskedPane

type ScrollPane[A, ReferenceData] = indigoextras.ui.components.ScrollPane[A, ReferenceData]
val ScrollPane: indigoextras.ui.components.ScrollPane.type = indigoextras.ui.components.ScrollPane

type Switch[ReferenceData] = indigoextras.ui.components.Switch[ReferenceData]
val Switch: indigoextras.ui.components.Switch.type = indigoextras.ui.components.Switch

type TextArea[ReferenceData] = indigoextras.ui.components.TextArea[ReferenceData]
val TextArea: indigoextras.ui.components.TextArea.type = indigoextras.ui.components.TextArea

// Component datatypes

type Anchor = indigoextras.ui.components.datatypes.Anchor
val Anchor: indigoextras.ui.components.datatypes.Anchor.type = indigoextras.ui.components.datatypes.Anchor

type BoundsMode = indigoextras.ui.components.datatypes.BoundsMode
val BoundsMode: indigoextras.ui.components.datatypes.BoundsMode.type = indigoextras.ui.components.datatypes.BoundsMode

type BoundsType[ReferenceData, A] = indigoextras.ui.components.datatypes.BoundsType[ReferenceData, A]
val BoundsType: indigoextras.ui.components.datatypes.BoundsType.type = indigoextras.ui.components.datatypes.BoundsType

type ComponentEntry[A, ReferenceData] = indigoextras.ui.components.datatypes.ComponentEntry[A, ReferenceData]
val ComponentEntry: indigoextras.ui.components.datatypes.ComponentEntry.type =
  indigoextras.ui.components.datatypes.ComponentEntry

type ComponentId = indigoextras.ui.components.datatypes.ComponentId
val ComponentId: indigoextras.ui.components.datatypes.ComponentId.type =
  indigoextras.ui.components.datatypes.ComponentId

type ComponentLayout = indigoextras.ui.components.datatypes.ComponentLayout
val ComponentLayout: indigoextras.ui.components.datatypes.ComponentLayout.type =
  indigoextras.ui.components.datatypes.ComponentLayout

type Cursor = indigoextras.ui.components.datatypes.Cursor
val Cursor: indigoextras.ui.components.datatypes.Cursor.type = indigoextras.ui.components.datatypes.Cursor

type FitMode = indigoextras.ui.components.datatypes.FitMode
val FitMode: indigoextras.ui.components.datatypes.FitMode.type = indigoextras.ui.components.datatypes.FitMode

type Overflow = indigoextras.ui.components.datatypes.Overflow
val Overflow: indigoextras.ui.components.datatypes.Overflow.type = indigoextras.ui.components.datatypes.Overflow

type Padding = indigoextras.ui.components.datatypes.Padding
val Padding: indigoextras.ui.components.datatypes.Padding.type = indigoextras.ui.components.datatypes.Padding

type ScrollOptions = indigoextras.ui.components.datatypes.ScrollOptions
val ScrollOptions: indigoextras.ui.components.datatypes.ScrollOptions.type =
  indigoextras.ui.components.datatypes.ScrollOptions

type SwitchState = indigoextras.ui.components.datatypes.SwitchState
val SwitchState: indigoextras.ui.components.datatypes.SwitchState.type =
  indigoextras.ui.components.datatypes.SwitchState

// Datatypes

type Bounds = indigoextras.ui.datatypes.Bounds
val Bounds: indigoextras.ui.datatypes.Bounds.type = indigoextras.ui.datatypes.Bounds

type Coords = indigoextras.ui.datatypes.Coords
val Coords: indigoextras.ui.datatypes.Coords.type = indigoextras.ui.datatypes.Coords

type Dimensions = indigoextras.ui.datatypes.Dimensions
val Dimensions: indigoextras.ui.datatypes.Dimensions.type = indigoextras.ui.datatypes.Dimensions

type UIContext[ReferenceData] = indigoextras.ui.datatypes.UIContext[ReferenceData]
val UIContext: indigoextras.ui.datatypes.UIContext.type = indigoextras.ui.datatypes.UIContext

// Shaders

type LayerMask = indigoextras.ui.shaders.LayerMask
val LayerMask: indigoextras.ui.shaders.LayerMask.type = indigoextras.ui.shaders.LayerMask

// Window

type Window[A, ReferenceData] = indigoextras.ui.window.Window[A, ReferenceData]
val Window: indigoextras.ui.window.Window.type = indigoextras.ui.window.Window

type WindowContext = indigoextras.ui.window.WindowContext
val WindowContext: indigoextras.ui.window.WindowContext.type = indigoextras.ui.window.WindowContext

type WindowEvent = indigoextras.ui.window.WindowEvent
val WindowEvent: indigoextras.ui.window.WindowEvent.type = indigoextras.ui.window.WindowEvent

type WindowId = indigoextras.ui.window.WindowId
val WindowId: indigoextras.ui.window.WindowId.type = indigoextras.ui.window.WindowId

type WindowManager[StartUpData, Model, RefData] = indigoextras.ui.window.WindowManager[StartUpData, Model, RefData]
val WindowManager: indigoextras.ui.window.WindowManager.type = indigoextras.ui.window.WindowManager

type WindowMode = indigoextras.ui.window.WindowMode
val WindowMode: indigoextras.ui.window.WindowMode.type = indigoextras.ui.window.WindowMode

type WindowActive = indigoextras.ui.window.WindowActive
val WindowActive: indigoextras.ui.window.WindowActive.type = indigoextras.ui.window.WindowActive

type Space = indigoextras.ui.window.Space
val Space: indigoextras.ui.window.Space.type = indigoextras.ui.window.Space
