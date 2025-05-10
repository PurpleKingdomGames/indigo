package indigoextras.performers

import indigo.*

class StageManagerTests extends munit.FunSuite {

  val layerKey: LayerKey = LayerKey("testLayer")
  val ctx                = SubSystemContext.fromContext(Context.initial)
  val text               = Text("", FontKey("test"), Material.Bitmap(AssetName("test")))

  test("StageManager add and present") {
    val model        = StageManager.Model[Unit](WorldOptions.default)
    val stageManager = StageManager[Unit](SubSystemId("test"))

    val updatedModel = stageManager.update(ctx, model)(PerformerEvent.Add(layerKey, TestPerformer("a", "This is 'a'")))
    val presented    = stageManager.present(ctx, updatedModel.unsafeGet)

    val expected =
      Batch(
        LayerEntry(layerKey, Layer.Content(Batch(text.withText("This is 'a'"))))
      )

    assertEquals(presented.unsafeGet.layers, expected)
  }

  test("StageManager add and present, no duplicates with the same id") {
    val model        = StageManager.Model[Unit](WorldOptions.default)
    val stageManager = StageManager[Unit](SubSystemId("test"))

    val actual =
      for {
        a <- stageManager.update(ctx, model)(PerformerEvent.Add(layerKey, TestPerformer("a", "This is 'a'")))
        b <- stageManager.update(ctx, a)(PerformerEvent.Add(layerKey, TestPerformer("a", "This is 'a'")))
        p <- stageManager.present(ctx, b)
      } yield p

    val expected =
      Batch(
        LayerEntry(layerKey, Layer.Content(Batch(text.withText("This is 'a'"))))
      )

    assertEquals(actual.unsafeGet.layers, expected)
  }

  test("StageManager does not present empty layers") {
    val model        = StageManager.Model[Unit](WorldOptions.default)
    val stageManager = StageManager[Unit](SubSystemId("test"))

    val actual =
      for {
        a <- stageManager.update(ctx, model)(PerformerEvent.Add(layerKey, TestPerformer("a", "This is 'a'")))
        b <- stageManager.update(ctx, a)(
          PerformerEvent.Add(LayerKey("another layer"), TestPerformer("b", "This is 'b'"))
        )
        r <- stageManager.update(ctx, b)(PerformerEvent.Remove(PerformerId("a")))
        p <- stageManager.present(ctx, r)
      } yield p

    val expected =
      Batch(
        LayerEntry(LayerKey("another layer"), Layer.Content(Batch(text.withText("This is 'b'"))))
      )

    assertEquals(actual.unsafeGet.layers, expected)
  }

  test("StageManager add, remove, and present") {
    val model        = StageManager.Model[Unit](WorldOptions.default)
    val stageManager = StageManager[Unit](SubSystemId("test"))

    val updatedModelAdded =
      stageManager.update(ctx, model)(PerformerEvent.Add(layerKey, TestPerformer("a", "This is 'a'")))
    val updatedModelRemoved =
      stageManager.update(ctx, updatedModelAdded.unsafeGet)(PerformerEvent.Remove(PerformerId("a")))
    val presented = stageManager.present(ctx, updatedModelRemoved.unsafeGet)

    val expected =
      Batch.empty

    assertEquals(presented.unsafeGet.layers, expected)
  }

  test("StageManager, a support performer can remove itself") {
    val model        = StageManager.Model[Unit](WorldOptions.default)
    val stageManager = StageManager[Unit](SubSystemId("test"))

    val updatedModel: Outcome[StageManager.Model[Unit]] =
      for {
        added <- stageManager.update(ctx, model)(PerformerEvent.Add(layerKey, TestSupportPerformer("a", "This is 'a'")))
        tick  <- stageManager.update(ctx, added)(FrameTick)
      } yield tick

    val events = updatedModel.globalEventsOrNil

    assert(events.length == 1)
    assertEquals(events, Batch(PerformerEvent.Remove(PerformerId("a"))))

    // Just to show it really was there.
    val presentBeforeRemoved = stageManager.present(ctx, updatedModel.unsafeGet)

    val expectedBeforeRemoved =
      Batch(
        LayerEntry(layerKey, Layer.Content(Batch(text.withText("This is 'a'!"))))
      )

    assertEquals(presentBeforeRemoved.unsafeGet.layers, expectedBeforeRemoved)

    val updatedModelRemoved =
      updatedModel.flatMap { m =>
        stageManager.update(ctx, m)(events.head)
      }

    val presented = stageManager.present(ctx, updatedModelRemoved.unsafeGet)

    val expected =
      Batch.empty

    assertEquals(presented.unsafeGet.layers, expected)
  }

  final case class TestPerformer(_id: String, value: String) extends Performer.Extra[Unit]:
    val id: PerformerId       = PerformerId(_id)
    val depth: PerformerDepth = PerformerDepth(0)

    def update(context: PerformerContext[Unit]): Performer.Extra[Unit] =
      this.copy(value = value + "!")

    def present(context: PerformerContext[Unit]): Batch[SceneNode] =
      Batch(text.withText(value))

  final case class TestSupportPerformer(_id: String, value: String) extends Performer.Support[Unit]:
    val id: PerformerId       = PerformerId(_id)
    val depth: PerformerDepth = PerformerDepth(0)

    def update(context: PerformerContext[Unit]): GlobalEvent => Outcome[Performer.Support[Unit]] =
      case FrameTick =>
        Outcome(this.copy(value = value + "!"))
          .addGlobalEvents(
            PerformerEvent.Remove(id)
          )

      case _ =>
        Outcome(this)

    def present(context: PerformerContext[Unit]): Outcome[Batch[SceneNode]] =
      Outcome(Batch(text.withText(value)))

}
