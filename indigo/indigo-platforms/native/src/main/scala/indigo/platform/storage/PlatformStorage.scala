package indigo.platform.storage

import indigo.shared.platform.Storage

final class PlatformStorage extends Storage {

  def save(key: String, data: String): Unit =
    ()

  def load(key: String): Option[String] =
    None

  def delete(key: String): Unit =
    ()

  def deleteAll(): Unit =
    ()
}

object PlatformStorage {
  def default: Storage =
    new PlatformStorage
}
