package indigoextras.ui.components

import indigo.*
import indigoextras.ui.component.*
import indigoextras.ui.components.datatypes.*
import indigoextras.ui.datatypes.Bounds
import indigoextras.ui.datatypes.Coords
import indigoextras.ui.datatypes.Dimensions
import indigoextras.ui.datatypes.UIContext

class ComponentGroupTests extends munit.FunSuite:

  val ctx =
    UIContext(Context.initial, 1)

  given Component[String, Unit] with
    def bounds(context: UIContext[Unit], model: String): Bounds =
      Bounds(0, 0, model.length, 1)

    def updateModel(
        context: UIContext[Unit],
        model: String
    ): GlobalEvent => Outcome[String] =
      _ => Outcome(model)

    def present(
        context: UIContext[Unit],
        model: String
    ): Outcome[Layer] =
      Outcome(Layer.empty)

    def refresh(context: UIContext[Unit], model: String): String =
      model

  test("ComponentGroup.calculateContentBounds should return the correct bounds (Vertical)") {
    val group: ComponentGroup[Unit] =
      ComponentGroup()
        .withLayout(
          ComponentLayout.Vertical(Padding.zero.withBottom(2))
        )
        .withBoundsMode(indigoextras.ui.components.datatypes.BoundsMode.fixed(100, 100))
        .add("abc", "def")

    val instance =
      summon[Component[ComponentGroup[Unit], Unit]]

    // This normally happens as part of the update process
    val processed = instance.refresh(ctx, group)

    val actual =
      processed.contentBounds

    val expected =
      Bounds(0, 0, 3, 4)

    assertEquals(actual, expected)
  }

  test("ComponentGroup.calculateContentBounds should return the correct bounds (Horizontal)") {
    val group: ComponentGroup[Unit] =
      ComponentGroup()
        .withLayout(
          ComponentLayout.Horizontal(Padding.zero.withRight(2))
        )
        .withBoundsMode(indigoextras.ui.components.datatypes.BoundsMode.fixed(100, 100))
        .add("abc", "def")

    val instance =
      summon[Component[ComponentGroup[Unit], Unit]]

    // This normally happens as part of the update process
    val processed = instance.refresh(ctx, group)

    val actual =
      processed.contentBounds

    val expected =
      Bounds(0, 0, 8, 1)

    assertEquals(actual, expected)
  }

  // Write a test for ComponentGroup.calculateCascadeBounds
  test("ComponentGroup.calculateCascadeBounds should return the correct bounds") {
    val group: ComponentGroup[Unit] =
      ComponentGroup()
        .withLayout(
          ComponentLayout.Vertical(Padding.zero.withBottom(2))
        )
        .withBoundsMode(indigoextras.ui.components.datatypes.BoundsMode.fixed(100, 100))
        .add("abc", "def")

    val instance =
      summon[Component[ComponentGroup[Unit], Unit]]

    // This normally happens as part of the update process
    val processed =
      instance.refresh(ctx, group)

    val actualFixed =
      processed.dimensions

    assertEquals(actualFixed, Dimensions(100, 100))

    val actualDefault =
      val c = summon[Component[ComponentGroup[Unit], Unit]]
      c.refresh(
        ctx,
        group.withBoundsMode(indigoextras.ui.components.datatypes.BoundsMode.default)
      ).dimensions

    assertEquals(actualDefault, Dimensions(100, 4))
  }

  test("refresh should re-apply the layout to all existing components") {
    val group: ComponentGroup[Unit] =
      ComponentGroup()
        .withLayout(
          ComponentLayout.Horizontal(Padding(5), Overflow.Wrap)
        )
        .withBoundsMode(indigoextras.ui.components.datatypes.BoundsMode.fixed(100, 100))
        .add("abc", "def")

    val actual =
      summon[Component[ComponentGroup[Unit], Unit]]
        .refresh(ctx, group)
        .components
        .toList
        .map(_.offset)

    val expected =
      List(
        Coords(5, 5),
        Coords(18, 5) // It's like this: 5 |3| 5.5 |3| 5
      )

    assertEquals(actual, expected)
  }

  test("Calculate the next offset - vertical, padding 0") {
    val group = ComponentGroup(10, 5)
      .withLayout(ComponentLayout.Vertical(Padding(0)))
      .add("abc", "def")

    val actual =
      summon[Component[ComponentGroup[Unit], Unit]]
        .refresh(ctx, group)
        .components
        .toList
        .map(_.offset)

    val expected =
      List(
        Coords(0, 0),
        Coords(0, 1)
      )

    assertEquals(actual, expected)
  }

  test("Calculate the next offset - vertical, padding 5") {
    val group = ComponentGroup(10, 5)
      .withLayout(ComponentLayout.Vertical(Padding(5)))
      .add("abc", "def")

    val actual =
      summon[Component[ComponentGroup[Unit], Unit]]
        .refresh(ctx, group)
        .components
        .toList
        .map(_.offset)

    val expected =
      List(
        Coords(5, 5),
        Coords(5, 5 + 1 + 5 + 5)
      )

    assertEquals(actual, expected)
  }

  test("Calculate the next offset - vertical, padding top=5") {
    val group = ComponentGroup(10, 5)
      .withLayout(
        ComponentLayout.Vertical(Padding(5, 0, 0, 0))
      )
      .add("abc", "def")

    val actual =
      summon[Component[ComponentGroup[Unit], Unit]]
        .refresh(ctx, group)
        .components
        .toList
        .map(_.offset)

    val expected =
      List(
        Coords(0, 5),
        Coords(0, 5 + 1 + 5)
      )

    assertEquals(actual, expected)
  }

  test("Calculate the next offset - horizontal, padding 0, hidden") {
    val group = ComponentGroup(5, 5)
      .withLayout(
        ComponentLayout.Horizontal(Padding(0), Overflow.Hidden)
      )
      .add("abc", "def")

    val actual =
      summon[Component[ComponentGroup[Unit], Unit]]
        .refresh(ctx, group)
        .components
        .toList
        .map(_.offset)

    val expected =
      List(
        Coords(0, 0),
        Coords(3, 0)
      )

    assertEquals(actual, expected)
  }

  test("Calculate the next offset - horizontal, padding 5, hidden") {
    val group = ComponentGroup(5, 5)
      .withLayout(
        ComponentLayout.Horizontal(Padding(5), Overflow.Hidden)
      )
      .add("abc", "def")

    val actual =
      summon[Component[ComponentGroup[Unit], Unit]]
        .refresh(ctx, group)
        .components
        .toList
        .map(_.offset)

    val expected =
      List(
        Coords(5, 5),
        Coords(5 + 3 + 5 + 5, 5)
      )

    assertEquals(actual, expected)
  }

  test("Calculate the next offset - horizontal, padding left=5, hidden") {
    val group = ComponentGroup(5, 5)
      .withLayout(
        ComponentLayout.Horizontal(Padding(0, 0, 0, 5), Overflow.Hidden)
      )
      .add("abc", "def")

    val actual =
      summon[Component[ComponentGroup[Unit], Unit]]
        .refresh(ctx, group)
        .components
        .toList
        .map(_.offset)

    val expected =
      List(
        Coords(5, 0),
        Coords(5 + 3 + 5, 0)
      )

    assertEquals(actual, expected)
  }

  test("Calculate the next offset - horizontal, padding 0, wrap") {
    val group = ComponentGroup(5, 5)
      .withLayout(
        ComponentLayout.Horizontal(Padding(0), Overflow.Wrap)
      )
      .add("abc", "def")

    val actual =
      summon[Component[ComponentGroup[Unit], Unit]]
        .refresh(ctx, group)
        .components
        .toList
        .map(_.offset)

    val expected =
      List(
        Coords(0, 0),
        Coords(0, 1)
      )

    assertEquals(actual, expected)
  }

  test("Calculate the next offset - horizontal, padding 5, wrap") {
    val group = ComponentGroup(5, 5)
      .withLayout(
        ComponentLayout.Horizontal(Padding(5), Overflow.Wrap)
      )
      .add("abc", "def")

    val actual =
      summon[Component[ComponentGroup[Unit], Unit]]
        .refresh(ctx, group)
        .components
        .toList
        .map(_.offset)

    val expected =
      List(
        Coords(5, 5),
        Coords(5, 5 + 1 + 5)
      )

    assertEquals(actual, expected)
  }

  test("Calculate the next offset - horizontal, padding left=5 top=2, wrap") {
    val group = ComponentGroup(3, 5)
      .withLayout(
        ComponentLayout.Horizontal(Padding(2, 0, 0, 5), Overflow.Wrap)
      )
      .add("abc", "def")

    val actual =
      summon[Component[ComponentGroup[Unit], Unit]]
        .refresh(ctx, group)
        .components
        .toList
        .map(_.offset)

    val expected =
      List(
        Coords(5, 2),
        Coords(5, 2 + 1)
      )

    assertEquals(actual, expected)
  }

  test("Refresh should snap to width of parent and height of contents by default.") {
    val c =
      summon[Component[ComponentGroup[Unit], Unit]]

    val group =
      ComponentGroup()
        .withLayout(
          ComponentLayout.Vertical(Padding.zero.withBottom(10))
        )
        .add("abc", "def")

    val updated: ComponentGroup[Unit] =
      c.refresh(ctx, group)

    assertEquals(updated.contentBounds, Bounds(0, 0, 3, 12))
    assertEquals(updated.dimensions, Dimensions(100, 12))
  }

  test("Calculate the next offset for nested components".only) {
    val c = summon[Component[ComponentGroup[Unit], Unit]]

    val group = ComponentGroup()
      .withLayout(ComponentLayout.Vertical())
      .add(
        ComponentGroup()
          .withLayout(ComponentLayout.Horizontal(Overflow.Wrap))
          .add("abc")
      )
      .add("abc")

    val actual =
      c.refresh(ctx, group)
        .components
        .toList
        .map(_.offset)

    val expected =
      List(
        Coords(0, 0),
        Coords(0, 1)
      )

    assertEquals(actual, expected)
  }
