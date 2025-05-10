package indigoextras.actors

import indigo.*

class ActorPoolTests extends munit.FunSuite {

  test("ActorPool should be created with an empty list of actors") {
    val actorPool = ActorPool.empty[Unit, String]

    assertEquals(actorPool.toBatch.length, 0)
  }

  test("spawn") {
    val actorPool = ActorPool.empty[Unit, String]
    val newActors = Batch("Actor1", "Actor2", "Actor3")

    val updatedPool = actorPool.spawn(newActors)

    assertEquals(updatedPool.toBatch, newActors)
  }

  test("find") {
    val actorPool = ActorPool.empty[Unit, String].spawn("Actor1", "Actor2", "Actor3")

    val foundActor = actorPool.find(_ == "Actor2")

    assertEquals(foundActor, Some("Actor2"))
  }
  test("filter") {
    val actorPool = ActorPool.empty[Unit, String].spawn("Actor1", "Actor2", "Actor3")

    val filteredActors = actorPool.filter(_ == "Actor2")

    assertEquals(filteredActors, Batch("Actor2"))
  }

  test("filterNot") {
    val actorPool = ActorPool.empty[Unit, String].spawn("Actor1", "Actor2", "Actor3")

    val filteredActors = actorPool.filterNot(_ == "Actor2")

    assertEquals(filteredActors, Batch("Actor1", "Actor3"))
  }

  test("kill") {
    val actorPool = ActorPool.empty[Unit, String].spawn("Actor1", "Actor2", "Actor3")

    val updatedPool = actorPool.kill(_ == "Actor2")

    assertEquals(updatedPool.toBatch, Batch("Actor1", "Actor3"))
  }

  test("update") {
    val actorPool = ActorPool.empty[Unit, String].spawn("Actor1", "Actor2", "Actor3")

    val ctx: Context[Unit] =
      Context.initial

    val updatedPool = actorPool.update(ctx, ())(FrameTick)

    assertEquals(updatedPool.unsafeGet.toBatch, Batch("Actor1!", "Actor2!", "Actor3!"))
  }

  test("present") {
    val actorPool = ActorPool.empty[Unit, String].spawn("Actor1", "Actor2", "Actor3")

    val ctx: Context[Unit] =
      Context.initial

    val presented = actorPool.present(ctx, ())

    assertEquals(presented.unsafeGet.length, 3)
  }

  given Actor[Unit, String] with
    def update(context: ActorContext[Unit, String], actor: String): GlobalEvent => Outcome[String] =
      _ => Outcome(actor + "!")

    def present(context: ActorContext[Unit, String], actor: String): Outcome[Batch[SceneNode]] =
      Outcome(Batch(Text(actor, FontKey("test"), Material.Bitmap(AssetName("test")))))
}
