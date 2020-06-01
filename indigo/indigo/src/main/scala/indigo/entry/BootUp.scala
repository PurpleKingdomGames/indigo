package indigo.entry

import indigo.shared.config.GameConfig
import indigo.shared.assets.AssetType
import indigo.shared.animation.Animation
import indigo.shared.subsystems.SubSystem
import indigo.shared.datatypes.FontInfo

  final class BootUp[A](
      val gameConfig: GameConfig,
      val bootData: A,
      val animations: Set[Animation],
      val assets: Set[AssetType],
      val fonts: Set[FontInfo],
      val subSystems: Set[SubSystem]
  ) {

    def addAnimations(newAnimations: Set[Animation]): BootUp[A] =
      new BootUp(gameConfig, bootData, animations ++ newAnimations, assets, fonts, subSystems)
    def withAnimations(newAnimations: Set[Animation]): BootUp[A] =
      new BootUp(gameConfig, bootData, newAnimations, assets, fonts, subSystems)

    def addAssets(newAssets: Set[AssetType]): BootUp[A] =
      new BootUp(gameConfig, bootData, animations, assets ++ newAssets, fonts, subSystems)
    def withAssets(newAssets: Set[AssetType]): BootUp[A] =
      new BootUp(gameConfig, bootData, animations, newAssets, fonts, subSystems)

    def addFonts(newFonts: Set[FontInfo]): BootUp[A] =
      new BootUp(gameConfig, bootData, animations, assets, fonts ++ newFonts, subSystems)
    def withFonts(newFonts: Set[FontInfo]): BootUp[A] =
      new BootUp(gameConfig, bootData, animations, assets, newFonts, subSystems)

    def addSubSystems(newSubSystems: Set[SubSystem]): BootUp[A] =
      new BootUp(gameConfig, bootData, animations, assets, fonts, subSystems ++ newSubSystems)
    def withSubSystems(newSubSystems: Set[SubSystem]): BootUp[A] =
      new BootUp(gameConfig, bootData, animations, assets, fonts, newSubSystems)

  }
  object BootUp {
    def apply[A](gameConfig: GameConfig, bootData: A): BootUp[A] =
      new BootUp[A](gameConfig, bootData, Set(), Set(), Set(), Set())

    def apply[A](
        gameConfig: GameConfig,
        bootData: A,
        animations: Set[Animation],
        assets: Set[AssetType],
        fonts: Set[FontInfo],
        subSystems: Set[SubSystem]
    ): BootUp[A] =
      new BootUp[A](gameConfig, bootData, animations, assets, fonts, subSystems)
  }
