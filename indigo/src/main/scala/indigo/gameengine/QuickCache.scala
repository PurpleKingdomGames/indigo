package indigo.gameengine

import scala.collection.mutable

@SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures", "org.wartremover.warts.NonUnitStatements"))
final class QuickCache[A](private val cache: mutable.Map[CacheKey, A]) {

  def fetch(key: CacheKey): Option[A] =
    cache.get(key)

  def add(key: CacheKey, value: A): A = {
    cache.update(key, value)
    value
  }

  def fetchOrAdd(key: CacheKey, value: A): A =
    cache.getOrElse(key, value)

  def purgeAll(): QuickCache[A] = {
    cache.clear()
    this
  }

  def purge(key: CacheKey): QuickCache[A] = {
    cache.remove(key)
    this
  }

}

@SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
object QuickCache {

  def apply[A](key: CacheKey)(value: A)(implicit cache: QuickCache[A]): A =
    cache.add(key, value)

  def apply[A](value: A)(implicit cache: QuickCache[A], key: ToCacheKey[A]): A =
    cache.add(key.toKey(value), value)

  def empty[A]: QuickCache[A] =
    new QuickCache[A](mutable.Map.empty[CacheKey, A])

}

final class CacheKey(val value: String) extends AnyVal

trait ToCacheKey[A] {
  def toKey(a: A): CacheKey
}
object ToCacheKey {
  def apply[A](f: A => CacheKey): ToCacheKey[A] =
    new ToCacheKey[A] {
      def toKey(a: A): CacheKey = f(a)
    }
}
