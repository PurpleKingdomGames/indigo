package com.purplekingdomgames.server

import com.purplekingdomgames.shared._

object GameDetails {

  val definition: GameDefinition =
    GameDefinition(
      scenes = List(
        GameScene(
          id = "test",
          active = true,
          entities = List(
            "traffic light red"
          )
        )
      ),
      entities = List(
        Entity(
          id = "traffic light red",
          components = EntityComponents(
            presentation = EntityPresentation(
              graphic = Option(
                EntityGraphic(
                  assetRef = "trafficlights",
                  bounds = EntityRectangle(10, 10, 128, 128),
                  crop = EntityRectangle(0, 0, 64, 64)
                )
              )
            )
          )
        )
      )
    )

  val config: GameConfig =
    GameConfig.default

  val assets: AssetList =
    AssetList.empty
      .withImage("smallFontName", "assets/boxy_font.png")
      .withImage("light", "assets/light_texture.png")
      .withImage("base_charactor", "assets/base_charactor.png")
      .withImage("trafficlights", "assets/trafficlights.png")
      .withText("indigoJson", "assets/indigo.json")
      .withText("base_charactor-json", "assets/base_charactor.json")

}
