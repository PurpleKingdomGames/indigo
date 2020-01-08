package indigo.shared.platform

trait Storage {

  def save(key: String, data: String): Unit

  def load(key: String): Option[String]

  def delete(key: String): Unit

  def deleteAll(): Unit

}
