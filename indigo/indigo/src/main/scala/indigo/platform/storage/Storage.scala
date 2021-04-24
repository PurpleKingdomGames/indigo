package indigo.platform.storage

import org.scalajs.dom

final class Storage {

  def save(key: String, data: String): Unit =
    dom.window.localStorage.setItem(key, data)

  def load(key: String): Option[String] =
    Option(dom.window.localStorage.getItem(key))

  def delete(key: String): Unit =
    dom.window.localStorage.removeItem(key)

  def deleteAll(): Unit =
    dom.window.localStorage.clear()
}

object Storage {
  def default: Storage =
    new Storage
}
