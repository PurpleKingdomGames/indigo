package indigoexts.scenemanager

import indigo.collections.NonEmptyList
import indigo.shared.EqualTo._

sealed trait Scenes[GameModel, ViewModel] extends Product with Serializable {

  def ::(scene: Scene[GameModel, ViewModel]): ScenesList[GameModel, ViewModel] =
    Scenes.cons(scene, this)

  final def foldLeft[Z](acc: Z)(f: (Z, Scene[GameModel, ViewModel]) => Z): Z =
    Scenes.foldLeft(this)(acc)(f)

  final def findScene(name: SceneName): Option[Scene[GameModel, ViewModel]] =
    Scenes.findScene(this, name)
}

object Scenes {

  def empty[GameModel, ViewModel]: Scenes[GameModel, ViewModel] =
    ScenesNil()

  def cons[GameModel, ViewModel](
      scene: Scene[GameModel, ViewModel],
      scenes: Scenes[GameModel, ViewModel]
  ): ScenesList[GameModel, ViewModel] =
    ScenesList[GameModel, ViewModel](scene, scenes)

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  final def foldLeft[GameModel, ViewModel, Z](scenes: Scenes[GameModel, ViewModel])(acc: Z)(f: (Z, Scene[GameModel, ViewModel]) => Z): Z =
    scenes match {
      case ScenesNil() =>
        acc

      case ScenesList(h, t) =>
        t.foldLeft(f(acc, h))(f)
    }

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  final def findScene[GameModel, ViewModel](scenes: Scenes[GameModel, ViewModel], name: SceneName): Option[Scene[GameModel, ViewModel]] =
    scenes match {
      case ScenesNil() =>
        None

      case ScenesList(h, _) if h.name === name =>
        Some(h)

      case ScenesList(_, t) =>
        t.findScene(name)
    }
}

final case class ScenesNil[GameModel, ViewModel]() extends Scenes[GameModel, ViewModel]

final case class ScenesList[GameModel, ViewModel](
    current: Scene[GameModel, ViewModel],
    next: Scenes[GameModel, ViewModel]
) extends Scenes[GameModel, ViewModel] {
  
  def head: Scene[GameModel, ViewModel] = current

  def listSceneNames: NonEmptyList[SceneName] =
    this.next.foldLeft(NonEmptyList(current.name))(_ :+ _.name)

  def nextScene: ScenesList[GameModel, ViewModel] =
    next match {
      case ScenesNil() =>
        this

      case t: ScenesList[GameModel, ViewModel] =>
        t
    }
}
