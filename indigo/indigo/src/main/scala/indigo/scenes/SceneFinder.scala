package indigo.scenes

import indigo.shared.IndigoLogger
import indigo.shared.collections.NonEmptyList

import scala.annotation.tailrec

final case class SceneFinder(previous: List[ScenePosition], current: ScenePosition, next: List[ScenePosition])
    derives CanEqual {

  val sceneCount: Int =
    toList.length

  def toList: List[ScenePosition] =
    previous ++ List(current) ++ next

  def toNel: NonEmptyList[ScenePosition] =
    previous match {
      case Nil =>
        NonEmptyList.pure(current, next)

      case x :: xs =>
        NonEmptyList.pure(x, (xs ++ List(current)) ++ next)
    }

  def forward: SceneFinder =
    next match {
      case Nil =>
        this

      case x :: xs =>
        SceneFinder(previous ++ List(current), x, xs)
    }

  def backward: SceneFinder =
    previous.reverse match {
      case Nil =>
        this

      case x :: xs =>
        SceneFinder(xs.reverse, x, current :: next)
    }

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
      .map(p => jumpToSceneByPosition(p.index)) match {
      case Some(sf) =>
        sf

      case None =>
        IndigoLogger.errorOnce("Failed to find scene called: " + name)
        this
    }

}

object SceneFinder {
  given CanEqual[Option[SceneFinder], Option[SceneFinder]] = CanEqual.derived

  def fromScenes[StartUpData, GameModel, ViewModel](
      scenesList: NonEmptyList[Scene[StartUpData, GameModel, ViewModel]]
  ): SceneFinder = {
    val a = scenesList.map(_.name).zipWithIndex.map(p => ScenePosition(p._2, p._1))

    SceneFinder(Nil, a.head, a.tail)
  }

}

final case class ScenePosition(index: Int, name: SceneName) derives CanEqual
