package indigo.scenes

import indigo.shared.IndigoLogger
import indigo.shared.collections.Batch
import indigo.shared.collections.NonEmptyBatch

import scala.annotation.tailrec

final case class SceneFinder(previous: Batch[ScenePosition], current: ScenePosition, next: Batch[ScenePosition])
    derives CanEqual:

  val sceneCount: Int =
    toBatch.size

  def toBatch: Batch[ScenePosition] =
    previous ++ Batch(current) ++ next

  def toNel: NonEmptyBatch[ScenePosition] =
    previous match {
      case Batch.Empty =>
        NonEmptyBatch.pure(current, next)

      case b =>
        NonEmptyBatch.pure(b.head, (b.tail ++ Batch(current)) ++ next)
    }

  def forward: SceneFinder =
    if next.isEmpty then this else SceneFinder(previous ++ Batch(current), next.head, next.tail)

  def backward: SceneFinder =
    val b = previous.reverse
    if b.isEmpty then this else SceneFinder(b.tail.reverse, b.head, current :: next)

  @tailrec
  def jumpToSceneByPosition(index: Int): SceneFinder =
    index match {
      case i if i < 0 =>
        this

      case i if i > sceneCount =>
        this

      case i if i == current.index =>
        this

      case i if i < current.index =>
        this.backward.jumpToSceneByPosition(index)

      case i if i > current.index =>
        this.forward.jumpToSceneByPosition(index)

      case _ =>
        this
    }

  def jumpToSceneByName(name: SceneName): SceneFinder =
    this.toBatch
      .find(p => p.name == name)
      .map(p => jumpToSceneByPosition(p.index)) match {
      case Some(sf) =>
        sf

      case None =>
        IndigoLogger.errorOnce("Failed to find scene called: " + name)
        this
    }

object SceneFinder:
  given CanEqual[Option[SceneFinder], Option[SceneFinder]] = CanEqual.derived

  def fromScenes[StartUpData, GameModel, ViewModel](
      scenesBatch: NonEmptyBatch[Scene[StartUpData, GameModel, ViewModel]]
  ): SceneFinder =
    val a = scenesBatch.map(_.name).zipWithIndex.map(p => ScenePosition(p._2, p._1))

    SceneFinder(Batch.Empty, a.head, a.tail)

final case class ScenePosition(index: Int, name: SceneName) derives CanEqual
