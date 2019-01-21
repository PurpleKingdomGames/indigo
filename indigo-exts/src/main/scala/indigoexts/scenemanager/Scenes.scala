package indigoexts.scenemanager

import indigoexts.collections.NonEmptyList

sealed trait Scenes[GameModel, ViewModel, +T <: Scene[GameModel, ViewModel, _, _]] extends Product with Serializable {

  def ::[S1 <: Scene[GameModel, ViewModel, _, _]](scene: S1): ScenesList[GameModel, ViewModel, S1, T] =
    Scenes.cons(scene, this)

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  final def foldLeft[Z](acc: Z)(f: (Z, Scene[GameModel, ViewModel, _, _]) => Z): Z =
    this match {
      case ScenesNil() =>
        acc

      case ScenesList(h, t) =>
        t.foldLeft(f(acc, h))(f)
    }

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  final def findScene(name: SceneName): Option[Scene[GameModel, ViewModel, _, _]] =
    this match {
      case ScenesNil() =>
        None

      case ScenesList(h, _) if h.name === name =>
        Some(h)

      case ScenesList(_, t) =>
        t.findScene(name)
    }
}

object Scenes {
  def cons[GameModel, ViewModel, S1 <: Scene[GameModel, ViewModel, _, _], S2 <: Scene[GameModel, ViewModel, _, _]](
      scene: S1,
      scenes: Scenes[GameModel, ViewModel, S2]
  ): ScenesList[GameModel, ViewModel, S1, S2] =
    ScenesList[GameModel, ViewModel, S1, S2](scene, scenes)
}

final case class ScenesNil[GameModel, ViewModel]() extends Scenes[GameModel, ViewModel, Nothing]

final case class ScenesList[GameModel, ViewModel, S1 <: Scene[GameModel, ViewModel, _, _], +S2 <: Scene[GameModel, ViewModel, _, _]](
    current: S1,
    next: Scenes[GameModel, ViewModel, S2]
) extends Scenes[GameModel, ViewModel, S1] {
  def head: S1 = current

  def listSceneNames: NonEmptyList[SceneName] =
    this.next.foldLeft(NonEmptyList(current.name, Nil))(_ :+ _.name)

  def nextScene: ScenesList[GameModel, ViewModel, _, _] =
    next match {
      case ScenesNil() =>
        this

      case t: ScenesList[GameModel, ViewModel, _, _] =>
        t
    }
}
