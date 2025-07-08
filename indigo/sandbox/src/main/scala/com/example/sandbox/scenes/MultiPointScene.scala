package com.example.sandbox.scenes

import com.example.sandbox.Fonts
import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*
import org.scalajs.dom

import scala.annotation.nowarn
import scala.math.BigDecimal.RoundingMode

// 4 boxes - Pen, Touch, Mouse, Pointer
// Each box contains position, button down, state (down/up), and pressure (if applicable)

@nowarn("msg=unused")
object MultiPointScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:
  // disabling default browser touch actions
  val style = dom.document.createElement("style")
  style.innerHTML = "canvas { touch-action: none }"
  dom.document.head.appendChild(style)

  type SceneModel     = InputStateModel
  type SceneViewModel = Unit

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: Lens[SandboxGameModel, InputStateModel] =
    Lens(_.inputStates, (m, p) => m.copy(inputStates = p))

  def viewModelLens: Lens[SandboxViewModel, Unit] =
    Lens.unit

  def name: SceneName =
    SceneName("multi-input")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set()

  def updateModel(
      context: SceneContext[SandboxStartupData],
      model: InputStateModel
  ): GlobalEvent => Outcome[InputStateModel] =
    case e: PenEvent.Move => Outcome(model.copy(pen = model.pen.copy(pos = e.position, pressure = e.pressure)))
    case e: PenEvent.Down =>
      Outcome(
        model.copy(pen =
          model.pen.copy(pos = e.position, button = e.button, isDown = e.button.isEmpty, pressure = e.pressure)
        )
      )
    case e: PenEvent.Up =>
      Outcome(model.copy(pen = model.pen.copy(pos = e.position, button = None, isDown = false, pressure = e.pressure)))
    case e: MouseEvent.Move => Outcome(model.copy(mouse = model.mouse.copy(pos = e.position)))
    case e: MouseEvent.Down =>
      Outcome(model.copy(mouse = model.mouse.copy(pos = e.position, button = Some(e.button))))
    case e: MouseEvent.Up =>
      Outcome(model.copy(mouse = model.mouse.copy(pos = e.position, button = None)))
    case e: TouchEvent.Move =>
      Outcome(model.copy(touch = model.touch.copy(pos = e.position, pressure = e.pressure)))
    case e: TouchEvent.Down =>
      Outcome(
        model.copy(
          touch =
            model.touch.copy(pos = e.position, fingers = model.touch.fingers + 1, isDown = true, pressure = e.pressure)
        )
      )
    case e: TouchEvent.Up =>
      Outcome(
        model.copy(
          touch = model.touch.copy(
            pos = e.position,
            fingers = model.touch.fingers - 1,
            isDown = model.touch.fingers > 1,
            pressure = e.pressure
          )
        )
      )

    case e: PointerEvent.Move => Outcome(model.copy(pointer = model.pointer.copy(pos = e.position)))
    case e: PointerEvent.Down =>
      Outcome(model.copy(pointer = model.pointer.copy(pos = e.position, button = e.button, isDown = true)))
    case e: PointerEvent.Up =>
      Outcome(model.copy(pointer = model.pointer.copy(pos = e.position, button = None, isDown = false)))
    case _ => Outcome(model)

  def updateViewModel(
      context: SceneContext[SandboxStartupData],
      model: InputStateModel,
      viewModel: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  val textMaterial = SandboxAssets.fontMaterial.toBitmap

  val globalMagnificationLevel = 2

  def present(
      context: SceneContext[SandboxStartupData],
      model: InputStateModel,
      viewModel: Unit
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Layer(
          // Pen data
          Text("Pen", Fonts.fontKey, textMaterial).moveTo(10, 10),
          Text(s"${model.pen.pos.x},${model.pen.pos.y}", Fonts.fontKey, textMaterial).moveTo(10, 30),
          Text(
            s"${model.pen.button match {
                case Some(value) => value.toString
                case None        => "No button down"
              }}",
            Fonts.fontKey,
            textMaterial
          ).moveTo(10, 50),
          Text(s"Is ${if model.pen.isDown then "Down" else "Up"}", Fonts.fontKey, textMaterial).moveTo(10, 70),
          Text(
            s"Pressure ${BigDecimal(model.pen.pressure).setScale(4, RoundingMode.DOWN)}",
            Fonts.fontKey,
            textMaterial
          ).moveTo(10, 90),
          // Mouse data
          Text("Mouse", Fonts.fontKey, textMaterial).moveTo(10, 130),
          Text(s"${model.mouse.pos.x},${model.mouse.pos.y}", Fonts.fontKey, textMaterial).moveTo(10, 150),
          Text(
            s"${model.mouse.button match {
                case Some(value) => value.toString
                case None        => "No button down"
              }}",
            Fonts.fontKey,
            textMaterial
          ).moveTo(10, 170),
          // Touch data
          Text("Touch", Fonts.fontKey, textMaterial).moveTo(10, 210),
          Text(s"${model.touch.pos.x},${model.touch.pos.y}", Fonts.fontKey, textMaterial).moveTo(10, 230),
          Text(s"Fingers ${model.touch.fingers}", Fonts.fontKey, textMaterial).moveTo(10, 250),
          Text(s"Is ${if model.touch.isDown then "Down" else "Up"}", Fonts.fontKey, textMaterial).moveTo(10, 270),
          Text(
            s"Pressure ${BigDecimal(model.touch.pressure).setScale(4, RoundingMode.DOWN)}",
            Fonts.fontKey,
            textMaterial
          ).moveTo(10, 290),
          // Pointer data
          Text("Generic Pointer", Fonts.fontKey, textMaterial).moveTo(10, 340),
          Text(s"${model.pointer.pos.x},${model.pointer.pos.y}", Fonts.fontKey, textMaterial).moveTo(10, 360),
          Text(
            s"${model.pointer.button match {
                case Some(value) => value.toString
                case None        => "No button down"
              }}",
            Fonts.fontKey,
            textMaterial
          ).moveTo(10, 380),
          Text(s"Is ${if model.pointer.isDown then "Down" else "Up"}", Fonts.fontKey, textMaterial).moveTo(10, 400)
        ).withMagnification(1)
      )
    )

final case class InputStateModel(
    pen: Pen,
    mouse: Mouse,
    touch: Touch,
    pointer: Pointer
)

object InputStateModel:
  val empty: InputStateModel = InputStateModel(
    pen = Pen(Point.zero, None, isDown = false, pressure = 0.0),
    mouse = Mouse(Point.zero, None),
    touch = Touch(Point.zero, fingers = 0, isDown = false, pressure = 0.0),
    pointer = Pointer(Point.zero, None, isDown = false)
  )

final case class Pen(pos: Point, button: Option[MouseButton], isDown: Boolean, pressure: Double)
final case class Mouse(pos: Point, button: Option[MouseButton])
final case class Touch(pos: Point, fingers: Int, isDown: Boolean, pressure: Double)
final case class Pointer(pos: Point, button: Option[MouseButton], isDown: Boolean)
