package indigo.platform.storage

import indigo.shared.events.StorageActionType
import indigo.shared.events.StorageEventError
import indigo.shared.events.StorageEventError.FeatureNotAvailable
import indigo.shared.events.StorageEventError.InvalidPermissions
import indigo.shared.events.StorageEventError.QuotaExceeded
import indigo.shared.events.StorageEventError.Unspecified
import indigo.shared.events.StorageKey
import org.scalajs.dom

import scala.scalajs.js

final class Storage {
  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def key(index: Int): Either[StorageEventError, Option[String]] =
    try
      if dom.window.localStorage == null then Left(FeatureNotAvailable(index, StorageActionType.Find))
      else Right(Option(dom.window.localStorage.key(index)))
    catch {
      case e: js.JavaScriptException =>
        Left(errToEvent(e, index, StorageActionType.Find))
    }

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def save(key: String, data: String): Either[StorageEventError, Unit] =
    try
      if dom.window.localStorage == null then Left(FeatureNotAvailable(key, StorageActionType.Save))
      else Right(dom.window.localStorage.setItem(key, data))
    catch {
      case e: js.JavaScriptException =>
        Left(errToEvent(e, key, StorageActionType.Save))
    }

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def load(key: String): Either[StorageEventError, Option[String]] =
    try
      if dom.window.localStorage == null then Left(FeatureNotAvailable(key, StorageActionType.Load))
      else Right(Option(dom.window.localStorage.getItem(key)))
    catch {
      case e: js.JavaScriptException =>
        Left(errToEvent(e, key, StorageActionType.Load))
    }

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def delete(key: String): Either[StorageEventError, Unit] =
    try
      if dom.window.localStorage == null then Left(FeatureNotAvailable(key, StorageActionType.Delete))
      else Right(dom.window.localStorage.removeItem(key))
    catch {
      case e: js.JavaScriptException =>
        Left(errToEvent(e, key, StorageActionType.Delete))
    }

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def deleteAll(): Either[StorageEventError, Unit] =
    try
      if dom.window.localStorage == null then Left(FeatureNotAvailable(None, StorageActionType.Delete))
      else Right(dom.window.localStorage.clear())
    catch {
      case e: js.JavaScriptException =>
        Left(errToEvent(e, None, StorageActionType.Delete))
    }

  private def errToEvent(
      e: js.JavaScriptException,
      id: Option[StorageKey],
      actionType: StorageActionType
  ): StorageEventError =
    val lowerMsg = e.getMessage().toLowerCase
    if lowerMsg.contains("quota") then QuotaExceeded(id, actionType)
    else if lowerMsg.contains("security") || lowerMsg.contains("permission") then InvalidPermissions(id, actionType)
    else Unspecified(id, actionType, e.getMessage())

  private def errToEvent(
      e: js.JavaScriptException,
      id: Int,
      actionType: StorageActionType
  ): StorageEventError = errToEvent(e, Some(StorageKey.Index(id)), actionType)

  private def errToEvent(
      e: js.JavaScriptException,
      key: String,
      actionType: StorageActionType
  ): StorageEventError = errToEvent(e, Some(StorageKey.Key(key)), actionType)
}

object Storage {
  def default: Storage =
    new Storage
}
