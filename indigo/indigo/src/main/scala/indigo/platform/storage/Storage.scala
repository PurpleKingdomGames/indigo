package indigo.platform.storage

import indigo.shared.events.StorageActionType
import indigo.shared.events.StorageEventError
import indigo.shared.events.StorageEventError.FeatureNotAvailable
import indigo.shared.events.StorageEventError.InvalidPermissions
import indigo.shared.events.StorageEventError.QuotaExceeded
import indigo.shared.events.StorageEventError.Unspecified
import org.scalajs.dom

import scala.scalajs.js

final class Storage {
  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def key(index: Int): Either[Option[String], StorageEventError] =
    try
      if dom.window.localStorage == null then Right(FeatureNotAvailable(index, StorageActionType.Find))
      else Left(Option(dom.window.localStorage.key(index)))
    catch {
      case e: js.JavaScriptException =>
        Right(errToEvent(e, index, StorageActionType.Find))
    }

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def save(key: String, data: String): Either[Unit, StorageEventError] =
    try
      if dom.window.localStorage == null then Right(FeatureNotAvailable(key, StorageActionType.Save))
      else Left(dom.window.localStorage.setItem(key, data))
    catch {
      case e: js.JavaScriptException =>
        Right(errToEvent(e, key, StorageActionType.Save))
    }

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def load(key: String): Either[Option[String], StorageEventError] =
    try
      if dom.window.localStorage == null then Right(FeatureNotAvailable(key, StorageActionType.Load))
      else Left(Option(dom.window.localStorage.getItem(key)))
    catch {
      case e: js.JavaScriptException =>
        Right(errToEvent(e, key, StorageActionType.Load))
    }

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def delete(key: String): Either[Unit, StorageEventError] =
    try
      if dom.window.localStorage == null then Right(FeatureNotAvailable(key, StorageActionType.Delete))
      else Left(dom.window.localStorage.removeItem(key))
    catch {
      case e: js.JavaScriptException =>
        Right(errToEvent(e, key, StorageActionType.Delete))
    }

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def deleteAll(): Either[Unit, StorageEventError] =
    try
      if dom.window.localStorage == null then Right(FeatureNotAvailable("", StorageActionType.Delete))
      else Left(dom.window.localStorage.clear())
    catch {
      case e: js.JavaScriptException =>
        Right(errToEvent(e, "", StorageActionType.Delete))
    }

  private def errToEvent(
      e: js.JavaScriptException,
      id: String | Int,
      actionType: StorageActionType
  ): StorageEventError =
    val lowerMsg = e.getMessage().toLowerCase
    if lowerMsg.contains("quota") then QuotaExceeded(id, actionType)
    else if lowerMsg.contains("security") || lowerMsg.contains("permission") then InvalidPermissions(id, actionType)
    else Unspecified(id, actionType, e.getMessage())
}

object Storage {
  def default: Storage =
    new Storage
}
