package indigo.scenes

import indigo.shared.IndigoLogger
import indigo.shared.collections.Batch
import indigo.shared.collections.NonEmptyBatch

import scala.annotation.tailrec

final case class SceneFinder(previous: Batch[ScenePosition], current: ScenePosition, next: Batch[ScenePosition])
    derives CanEqual:

  val sceneCount: Int =
    toList.length

  def toBatch: Batch[ScenePosition] =
    previous ++ Batch(current) ++ next

  def toList: List[ScenePosition] =
    toBatch.toList

  def toNel: NonEmptyBatch[ScenePosition] =
    if previous.isEmpty then NonEmptyBatch.pure(current, next)
    else
      val h = previous.head
      val t = previous.tail

      NonEmptyBatch.pure(h, t ++ Batch(current) ++ next)

  def forward: SceneFinder =
    if next.isEmpty then this
    else
      val h = next.head
      val t = next.tail

      SceneFinder(previous ++ Batch(current), h, t)

  def forwardLoop: SceneFinder =
    if next.isEmpty then first
    else
      val h = next.head
      val t = next.tail

      SceneFinder(previous ++ Batch(current), h, t)

  def backward: SceneFinder =
    val p = previous.reverse
    if p.isEmpty then this
    else
      val h = p.head
      val t = p.tail

      SceneFinder(t.reverse, h, current :: next)

  def backwardLoop: SceneFinder =
    val p = previous.reverse
    if p.isEmpty then last
    else
      val h = p.head
      val t = p.tail

      SceneFinder(t.reverse, h, current :: next)

  @tailrec
  def jumpToSceneByPosition(index: Int): SceneFinder =
    index match
      case i if i < 0 =>
        jumpToSceneByPosition(0)

      case i if i >= sceneCount =>
        jumpToSceneByPosition(sceneCount - 1)

      case i if i == current.index =>
        this

      case i if i < current.index =>
        this.backward.jumpToSceneByPosition(index)

      case i if i > current.index =>
        this.forward.jumpToSceneByPosition(index)

      case _ =>
        this

  def jumpToSceneByName(name: SceneName): SceneFinder =
    this.toList
      .find(p => p.name == name)
      .map(p => jumpToSceneByPosition(p.index)) match
      case Some(sf) =>
        sf

      case None =>
        IndigoLogger.errorOnce("Failed to find scene called: " + name)
        this

  def first: SceneFinder =
    jumpToSceneByPosition(0)

  def last: SceneFinder =
    jumpToSceneByPosition(sceneCount - 1)

object SceneFinder:
  given CanEqual[Option[SceneFinder], Option[SceneFinder]] = CanEqual.derived

  def fromScenes[StartUpData, GameModel, ViewModel](
      scenesList: NonEmptyBatch[Scene[StartUpData, GameModel, ViewModel]]
  ): SceneFinder =
    val a = scenesList.map(_.name).zipWithIndex.map(p => ScenePosition(p._2, p._1))
    SceneFinder(Batch.empty, a.head, a.tail)

final case class ScenePosition(index: Int, name: SceneName) derives CanEqual
