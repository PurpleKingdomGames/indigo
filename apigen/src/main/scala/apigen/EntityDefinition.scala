package apigen

sealed trait EntityDefinition {
  def toXml: String
}

final case class ClassEntity(name: String) extends EntityDefinition {
  def toXml: String =
    s"""<entity type="class" name="$name"></entity>\n"""
}

final case class StaticEntity(name: String) extends EntityDefinition {
  def toXml: String =
    s"""<entity type="static" name="$name"></entity>\n"""
}

final case class ValueEntity(name: String, returnType: String) extends EntityDefinition {
  def toXml: String =
    s"""<entity type="value" name="$name" returnType="$returnType"></entity>\n"""
}

final case class MethodEntity(name: String) extends EntityDefinition {
  def toXml: String =
    s"""<entity type="method" name="$name"></entity>\n"""
}

final case class FunctionEntity(name: String) extends EntityDefinition {
  def toXml: String =
    s"""<entity type="function" name="$name"></entity>\n"""
}
