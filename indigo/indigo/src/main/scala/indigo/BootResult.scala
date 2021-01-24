package indigo

import indigo.shared.subsystems.SubSystem
import indigo.shared.display.Shader

final case class BootResult[A](
    gameConfig: GameConfig,
    bootData: A,
    animations: Set[Animation],
    assets: Set[AssetType],
    fonts: Set[FontInfo],
    subSystems: Set[SubSystem],
    shaders: Set[Shader]
) {

  def addAnimations(newAnimations: Set[Animation]): BootResult[A] =
    this.copy(animations = animations ++ newAnimations)
  def addAnimations(newAnimations: Animation*): BootResult[A] =
    addAnimations(newAnimations.toSet)
  def withAnimations(newAnimations: Set[Animation]): BootResult[A] =
    this.copy(animations = newAnimations)
  def withAnimations(newAnimations: Animation*): BootResult[A] =
    withAnimations(newAnimations.toSet)

  def addAssets(newAssets: Set[AssetType]): BootResult[A] =
    this.copy(assets = assets ++ newAssets)
  def addAssets(newAssets: AssetType*): BootResult[A] =
    addAssets(newAssets.toSet)
  def withAssets(newAssets: Set[AssetType]): BootResult[A] =
    this.copy(assets = newAssets)
  def withAssets(newAssets: AssetType*): BootResult[A] =
    withAssets(newAssets.toSet)

  def addFonts(newFonts: Set[FontInfo]): BootResult[A] =
    this.copy(fonts = fonts ++ newFonts)
  def addFonts(newFonts: FontInfo*): BootResult[A] =
    addFonts(newFonts.toSet)
  def withFonts(newFonts: Set[FontInfo]): BootResult[A] =
    this.copy(fonts = newFonts)
  def withFonts(newFonts: FontInfo*): BootResult[A] =
    withFonts(newFonts.toSet)

  def addSubSystems(newSubSystems: Set[SubSystem]): BootResult[A] =
    this.copy(subSystems = subSystems ++ newSubSystems)
  def addSubSystems(newSubSystems: SubSystem*): BootResult[A] =
    addSubSystems(newSubSystems.toSet)
  def withSubSystems(newSubSystems: Set[SubSystem]): BootResult[A] =
    this.copy(subSystems = newSubSystems)
  def withSubSystems(newSubSystems: SubSystem*): BootResult[A] =
    withSubSystems(newSubSystems.toSet)

  def addShaders(newShaders: Set[Shader]): BootResult[A] =
    this.copy(shaders = shaders ++ newShaders)
  def addShaders(newShaders: Shader*): BootResult[A] =
    addShaders(newShaders.toSet)
  def withShaders(newShaders: Set[Shader]): BootResult[A] =
    this.copy(shaders = newShaders)
  def withShaders(newShaders: Shader*): BootResult[A] =
    withShaders(newShaders.toSet)

}
object BootResult {
  def apply[A](gameConfig: GameConfig, bootData: A): BootResult[A] =
    new BootResult[A](gameConfig, bootData, Set(), Set(), Set(), Set(), Set())

  def noData(gameConfig: GameConfig): BootResult[Unit] =
    apply(gameConfig, ())
  def configOnly(gameConfig: GameConfig): BootResult[Unit] =
    noData(gameConfig)

  def default: BootResult[Unit] =
    noData(GameConfig.default)
}
