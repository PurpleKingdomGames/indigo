package indigo.shared

final case class GameDefinition(scenes: List[GameScene], entities: List[Entity])

final case class GameScene(id: String, active: Boolean, entities: List[String])

final case class Entity(id: String, components: EntityComponents)
final case class EntityComponents(presentation: EntityPresentation)
final case class EntityPresentation(graphic: Option[EntityGraphic])
final case class EntityGraphic(assetRef: String, bounds: EntityRectangle, crop: EntityRectangle)
final case class EntityRectangle(x: Int, y: Int, width: Int, height: Int)
