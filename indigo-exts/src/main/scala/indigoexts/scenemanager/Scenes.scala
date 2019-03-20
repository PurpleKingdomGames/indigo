package indigoexts.scenemanager

import indigoexts.collections.NonEmptyList

sealed trait Scenes[GameModel, ViewModel] extends Product with Serializable {

  def ::(scene: Scene[GameModel, ViewModel]): ScenesList[GameModel, ViewModel] =
    Scenes.cons(scene, this)

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  final def foldLeft[Z](acc: Z)(f: (Z, Scene[GameModel, ViewModel]) => Z): Z =
    this match {
      case ScenesNil() =>
        acc

      case ScenesList(h, t) =>
        t.foldLeft(f(acc, h))(f)
    }

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  final def findScene(name: SceneName): Option[Scene[GameModel, ViewModel]] =
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
  def cons[GameModel, ViewModel](
      scene: Scene[GameModel, ViewModel],
      scenes: Scenes[GameModel, ViewModel]
  ): ScenesList[GameModel, ViewModel] =
    ScenesList[GameModel, ViewModel](scene, scenes)
}

// TODO: Get rid of the ()
final case class ScenesNil[GameModel, ViewModel]() extends Scenes[GameModel, ViewModel]

final case class ScenesList[GameModel, ViewModel](
    current: Scene[GameModel, ViewModel],
    next: Scenes[GameModel, ViewModel]
) extends Scenes[GameModel, ViewModel] {
  // TODO: current is head
  def head: Scene[GameModel, ViewModel] = current

  def listSceneNames: NonEmptyList[SceneName] =
    this.next.foldLeft(NonEmptyList(current.name, Nil))(_ :+ _.name)

  def nextScene: ScenesList[GameModel, ViewModel] =
    next match {
      case ScenesNil() =>
        this

      case t: ScenesList[GameModel, ViewModel] =>
        t
    }
}
