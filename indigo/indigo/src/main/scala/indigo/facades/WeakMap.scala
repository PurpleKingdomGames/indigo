package indigo.facades

import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal
class WeakMap[K, V] extends js.Object:
  def delete(key: K): Unit        = js.native
  def has(key: K): Boolean        = js.native
  def get(key: K): UndefOr[V]     = js.native
  def set(key: K, value: V): Unit = js.native
