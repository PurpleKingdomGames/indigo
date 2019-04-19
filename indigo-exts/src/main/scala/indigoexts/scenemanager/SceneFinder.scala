package indigoexts.scenemanager

import indigo.shared.IndigoLogger
import indigo.collections.NonEmptyList
import indigo.shared.EqualTo
import indigo.shared.EqualTo._

import scala.annotation.tailrec

final class SceneFinder(val previous: List[ScenePosition], val current: ScenePosition, val next: List[ScenePosition]) {

  val sceneCount: Int =
    toList.length

  def toList: List[ScenePosition] =
    previous ++ List(current) ++ next

  def toNel: NonEmptyList[ScenePosition] =
    previous match {
      case Nil =>
        NonEmptyList.pure(current, next)

      case x :: xs =>
        NonEmptyList.pure(x, (xs :+ current) ++ next)
    }

  def forward: SceneFinder =
    next match {
      case Nil =>
        this

      case x :: xs =>
        SceneFinder(previous :+ current, x, xs)
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
    index match {
      case i if i < 0 =>
        this

      case i if i > sceneCount =>
        this

      case i if i === current.index =>
        this

      case i if i < current.index =>
        this.backward.jumpToSceneByPosition(index)

      case i if i > current.index =>
        this.forward.jumpToSceneByPosition(index)

      case _ =>
        this
    }

  def jumpToSceneByName(name: SceneName): SceneFinder =
    this.toList
      .find(p => p.name === name)
      .map(p => jumpToSceneByPosition(p.index)) match {
      case Some(sf) =>
        sf

      case None =>
        IndigoLogger.errorOnce("Failed to find scene called: " + name.name)
        this
    }

}

object SceneFinder {

  implicit def scenesFinderEqualTo(implicit eqL: EqualTo[List[ScenePosition]], eqP: EqualTo[ScenePosition]): EqualTo[SceneFinder] =
    EqualTo.create { (a, b) =>
      eqL.equal(a.previous, b.previous) &&
      eqP.equal(a.current, b.current) &&
      eqL.equal(a.next, b.next)
    }

  def apply(previous: List[ScenePosition], current: ScenePosition, next: List[ScenePosition]): SceneFinder =
    new SceneFinder(previous, current, next)

  def fromScenes[GameModel, ViewModel](scenesList: NonEmptyList[Scene[GameModel, ViewModel]]): SceneFinder = {
    val a = scenesList.map(_.name).zipWithIndex.map(p => ScenePosition(p._2, p._1))

    SceneFinder(Nil, a.head, a.tail)
  }

}

final case class ScenePosition(index: Int, name: SceneName)
object ScenePosition {

  implicit def EqScenePosition(implicit eqInt: EqualTo[Int], eqName: EqualTo[SceneName]): EqualTo[ScenePosition] =
    EqualTo.create[ScenePosition] { (a, b) =>
      eqInt.equal(a.index, b.index) && eqName.equal(a.name, b.name)
    }

}
