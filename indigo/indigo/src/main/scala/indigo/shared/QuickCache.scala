package indigo.shared

import scala.annotation.nowarn

/** QuickCache is a handy way to avoid expensive re-calculation of data. It is a side-effecting arrangement that Indigo
  * uses a lot internally, that can also be used by cautious game devs. Simple example:
  *
  * ```
  * given QuickCache[MyExpensiveObject] = QuickCache.empty
  *
  * QuickCache("key")(obj)
  * ```
  */
final class QuickCache[A](private val cache: scalajs.js.Dictionary[A]):

  def fetch(key: CacheKey): Option[A] =
    cache.get(key.toString)

  def add(key: CacheKey, value: => A): A = {
    val v = value
    cache.update(key.toString, v)
    v
  }

  def fetchOrAdd(key: CacheKey, disabled: Boolean, value: => A): A =
    if (disabled) value
    else
      try cache(key.toString)
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

  @nowarn("msg=unused")
  def purge(key: CacheKey): QuickCache[A] = {
    cache.remove(key.toString)
    this
  }

  def keys: List[CacheKey] =
    cache.keys.map(CacheKey(_)).toList

  def all: List[(CacheKey, A)] =
    cache.toList.map(p => (CacheKey(p._1), p._2))

  def entryExistsFor(key: CacheKey): Boolean =
    cache.keys.exists(_ == key.toString)

  def unsafeFetch(key: CacheKey): A =
    cache(key.toString)

  def size: Int =
    cache.size

  def toMap[K](keyConvertor: CacheKey => K): Map[K, A] =
    cache.toMap.map { (pair: (String, A)) =>
      (keyConvertor(CacheKey(pair._1)), pair._2)
    }

object QuickCache:

  def apply[A](key: String)(value: => A)(implicit cache: QuickCache[A]): A =
    cache.fetchOrAdd(CacheKey(key), false, value)

  def apply[A](key: String, disabled: Boolean)(value: => A)(implicit cache: QuickCache[A]): A =
    cache.fetchOrAdd(CacheKey(key), disabled, value)

  def empty[A]: QuickCache[A] =
    new QuickCache[A](scalajs.js.Dictionary.empty[A])

opaque type CacheKey = String
object CacheKey:
  inline def apply(value: String): CacheKey = value

  extension (c: CacheKey) inline def toString: String = c

  given CanEqual[CacheKey, CacheKey] = CanEqual.derived

trait ToCacheKey[A]:
  def toKey(a: A): CacheKey

object ToCacheKey:
  def apply[A](f: A => CacheKey): ToCacheKey[A] =
    new ToCacheKey[A] {
      def toKey(a: A): CacheKey = f(a)
    }

  implicit val s: ToCacheKey[String] =
    ToCacheKey(str => CacheKey(str))

  implicit val i: ToCacheKey[Int] =
    ToCacheKey(p => CacheKey(p.toString))
