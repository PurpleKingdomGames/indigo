package indigoextras.ui.components.datatypes

import indigo.*
import indigoextras.ui.components.*
import indigoextras.ui.datatypes.*

class ContainerLikeFunctionsTests extends munit.FunSuite:

  import indigoextras.ui.Helper.*

  val present: (UIContext[Unit], Label[Unit]) => Outcome[Layer] =
    (c, l) => Outcome(Layer.empty)

  val ctx =
    UIContext(Context.initial, 1)

  test("calculateNextOffset labels") {

    val group: ComponentGroup[Unit] =
      ComponentGroup()
        .withLayout(
          ComponentLayout.Vertical(Padding.zero)
        )
        .add(
          Label[Unit]("label 1", (_, s) => Bounds(0, 0, s.length, 1))(present),
          Label[Unit]("label 2", (_, s) => Bounds(0, 0, s.length, 1))(present),
          Label[Unit]("label 3", (_, s) => Bounds(0, 0, s.length, 1))(present)
        )

    val updated: ComponentGroup[Unit] =
      group.refresh(ctx)

    val actual =
      ContainerLikeFunctions.calculateNextOffset[Unit](
        Dimensions(20, 20),
        updated.layout
      )(ctx, updated.components)

    val expected =
      Coords(0, 3)

    assertEquals(actual, expected)
  }

  test("calculateNextOffset group of labels") {

    val group: ComponentGroup[Unit] =
      ComponentGroup()
        .withBoundsMode(BoundsMode.fit)
        .withLayout(
          ComponentLayout.Vertical()
        )
        .add(
          ComponentGroup()
            .withBoundsMode(BoundsMode.fit)
            .withLayout(
              ComponentLayout.Vertical()
            )
            .add(
              Label[Unit]("label 1", (_, s) => Bounds(0, 0, s.length, 1))(present),
              Label[Unit]("label 2", (_, s) => Bounds(0, 0, s.length, 1))(present),
              Label[Unit]("label 3", (_, s) => Bounds(0, 0, s.length, 1))(present)
            )
        )

    val updated: ComponentGroup[Unit] =
      group.refresh(ctx)

    assertEquals(updated.contentBounds, Bounds(0, 0, 7, 3))
    assertEquals(updated.dimensions, Dimensions(7, 3))

    val actual =
      ContainerLikeFunctions.calculateNextOffset[Unit](
        Dimensions(100, 0), // The layout is dynamic and horizontal, so we'll only know the width
        updated.layout
      )(ctx, updated.components)

    val expected =
      Coords(0, 3)

    assertEquals(actual, expected)
  }
