package indigo

import indigo.shared.subsystems.SubSystem

final class BootResult[A](
    val gameConfig: GameConfig,
    val bootData: A,
    val animations: Set[Animation],
    val assets: Set[AssetType],
    val fonts: Set[FontInfo],
    val subSystems: Set[SubSystem]
) {

  def addAnimations(newAnimations: Set[Animation]): BootResult[A] =
    new BootResult(gameConfig, bootData, animations ++ newAnimations, assets, fonts, subSystems)
  def addAnimations(newAnimations: Animation*): BootResult[A] =
    addAnimations(newAnimations.toSet)
  def withAnimations(newAnimations: Set[Animation]): BootResult[A] =
    new BootResult(gameConfig, bootData, newAnimations, assets, fonts, subSystems)
  def withAnimations(newAnimations: Animation*): BootResult[A] =
    withAnimations(newAnimations.toSet)

  def addAssets(newAssets: Set[AssetType]): BootResult[A] =
    new BootResult(gameConfig, bootData, animations, assets ++ newAssets, fonts, subSystems)
  def addAssets(newAssets: AssetType*): BootResult[A] =
    addAssets(newAssets.toSet)
  def withAssets(newAssets: Set[AssetType]): BootResult[A] =
    new BootResult(gameConfig, bootData, animations, newAssets, fonts, subSystems)
  def withAssets(newAssets: AssetType*): BootResult[A] =
    withAssets(newAssets.toSet)

  def addFonts(newFonts: Set[FontInfo]): BootResult[A] =
    new BootResult(gameConfig, bootData, animations, assets, fonts ++ newFonts, subSystems)
  def addFonts(newFonts: FontInfo*): BootResult[A] =
    addFonts(newFonts.toSet)
  def withFonts(newFonts: Set[FontInfo]): BootResult[A] =
    new BootResult(gameConfig, bootData, animations, assets, newFonts, subSystems)
  def withFonts(newFonts: FontInfo*): BootResult[A] =
    withFonts(newFonts.toSet)

  def addSubSystems(newSubSystems: Set[SubSystem]): BootResult[A] =
    new BootResult(gameConfig, bootData, animations, assets, fonts, subSystems ++ newSubSystems)
  def addSubSystems(newSubSystems: SubSystem*): BootResult[A] =
    addSubSystems(newSubSystems.toSet)
  def withSubSystems(newSubSystems: Set[SubSystem]): BootResult[A] =
    new BootResult(gameConfig, bootData, animations, assets, fonts, newSubSystems)
  def withSubSystems(newSubSystems: SubSystem*): BootResult[A] =
    withSubSystems(newSubSystems.toSet)

}
object BootResult {
  def apply[A](gameConfig: GameConfig, bootData: A): BootResult[A] =
    new BootResult[A](gameConfig, bootData, Set(), Set(), Set(), Set())

  def apply[A](
      gameConfig: GameConfig,
      bootData: A,
      animations: Set[Animation],
      assets: Set[AssetType],
      fonts: Set[FontInfo],
      subSystems: Set[SubSystem]
  ): BootResult[A] =
    new BootResult[A](gameConfig, bootData, animations, assets, fonts, subSystems)

  def noData(gameConfig: GameConfig): BootResult[Unit] =
    apply(gameConfig, ())
}
