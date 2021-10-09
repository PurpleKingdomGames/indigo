package indigo.shared

import scala.collection.mutable

final class QuickCache[A](private val cache: mutable.HashMap[CacheKey, A]) {

  def fetch(key: CacheKey): Option[A] =
    cache.get(key)

  def add(key: CacheKey, value: => A): A = {
    cache.update(key, value)
    value
  }

  def fetchOrAdd(key: CacheKey, disabled: Boolean, value: => A): A =
    if (disabled) value
    else
      try cache(key)
      catch {
        case _: Throwable =>
          add(key, value)
      }

  def purgeAllNow(): Unit =
    cache.clear()

  def purgeAll(): QuickCache[A] = {
    cache.clear()
    this
  }

  def purge(key: CacheKey): QuickCache[A] = {
    cache.remove(key)
    this
  }

  def keys: List[CacheKey] =
    cache.keys.toList

  def all: List[(CacheKey, A)] =
    cache.toList

  def entryExistsFor(key: CacheKey): Boolean =
    cache.keys.exists(_ == key)

  def unsafeFetch(key: CacheKey): A =
    cache(key)

  def size: Int =
    cache.size

  def toMap[K](keyConvertor: CacheKey => K): Map[K, A] =
    cache.toMap.map { (pair: (CacheKey, A)) =>
      (keyConvertor(pair._1), pair._2)
    }

}

object QuickCache {

  def apply[A](key: String)(value: => A)(implicit cache: QuickCache[A]): A =
    cache.fetchOrAdd(CacheKey(key), false, value)

  def apply[A](key: String, disabled: Boolean)(value: => A)(implicit cache: QuickCache[A]): A =
    cache.fetchOrAdd(CacheKey(key), disabled, value)

  def empty[A]: QuickCache[A] =
    new QuickCache[A](mutable.HashMap.empty[CacheKey, A])

}

opaque type CacheKey = String
object CacheKey:
  inline def apply(value: String): CacheKey = value

  given CanEqual[CacheKey, CacheKey] = CanEqual.derived

trait ToCacheKey[A] {
  def toKey(a: A): CacheKey
}
object ToCacheKey {
  def apply[A](f: A => CacheKey): ToCacheKey[A] =
    new ToCacheKey[A] {
      def toKey(a: A): CacheKey = f(a)
    }

  implicit val s: ToCacheKey[String] =
    ToCacheKey(str => CacheKey(str))

  implicit val i: ToCacheKey[Int] =
    ToCacheKey(p => CacheKey(p.toString))

}
