import indigo.shared.events.EventTypeAliases
import indigo.shared.scenegraph.SceneGraphTypeAliases
import indigo.shared.datatypes.DataTypeAliases
import indigo.shared.networking.NetworkingTypeAliases
import indigo.shared.SharedTypeAliases

package object indigo extends DataTypeAliases with SceneGraphTypeAliases with NetworkingTypeAliases with SharedTypeAliases with EventTypeAliases {

  object syntax {

    // This is a copy from `indigo.shared.abstractions.syntax` - nicer way to do this?

    import indigo.shared.abstractions._

    implicit class FunctorSyntax[F[_], A](fa: F[A])(implicit ev: Functor[F]) {
      def map[B](f: A => B): F[B] =
        ev.map(fa)(f)
    }

    implicit class ApplySyntax[F[_], A](fa: F[A])(implicit ev: Apply[F]) {
      def ap[B](f: F[A => B]): F[B] =
        ev.ap(fa)(f)
    }

    implicit class ApplicativeSyntax[F[_], A, B](ft: (F[A], F[B]))(implicit ev: Applicative[F]) {
      def map2[C](f: (A, B) => C): F[C] =
        ev.apply2(ft._1, ft._2)(f)
    }

    implicit class MonadSyntax[F[_], A](fa: F[A])(implicit ev: Monad[F]) {
      def flatMap[B](f: A => F[B]): F[B] =
        ev.flatMap(fa)(f)
    }

  }

  val logger: indigo.shared.IndigoLogger.type = indigo.shared.IndigoLogger

  type Startup[ErrorType, SuccessType] = shared.Startup[ErrorType, SuccessType]
  val Startup: shared.Startup.type = shared.Startup

  type GameTime = shared.time.GameTime
  val GameTime: shared.time.GameTime.type = shared.time.GameTime

  type Millis = shared.time.Millis
  val Millis: shared.time.Millis.type = shared.time.Millis

  type Seconds = shared.time.Seconds
  val Seconds: shared.time.Seconds.type = shared.time.Seconds

  type Dice = shared.dice.Dice
  val Dice: shared.dice.Dice.type = shared.dice.Dice

  type AssetCollection = platform.assets.AssetCollection

  type AssetName = shared.assets.AssetName
  val AssetName: shared.assets.AssetName.type = shared.assets.AssetName

  type AssetPath = shared.assets.AssetPath
  val AssetPath: shared.assets.AssetPath.type = shared.assets.AssetPath

  type Material = shared.datatypes.Material
  val Material: shared.datatypes.Material.type = shared.datatypes.Material

  type ToReportable[T] = shared.ToReportable[T]
  val ToReportable: shared.ToReportable.type = shared.ToReportable

  type StartupErrors = shared.StartupErrors
  val StartupErrors: shared.StartupErrors.type = shared.StartupErrors

  type Outcome[T] = shared.Outcome[T]
  val Outcome: shared.Outcome.type = shared.Outcome

  val Keys: shared.constants.Keys.type = shared.constants.Keys

  type Key = shared.constants.Key
  val Key: shared.constants.Key.type = shared.constants.Key

  type PowerOfTwo = shared.PowerOfTwo
  val PowerOfTwo: shared.PowerOfTwo.type = shared.PowerOfTwo

  type NonEmptyList[A] = shared.collections.NonEmptyList[A]
  val NonEmptyList: shared.collections.NonEmptyList.type = shared.collections.NonEmptyList

  type Signal[A] = shared.temporal.Signal[A]
  val Signal: shared.temporal.Signal.type = shared.temporal.Signal

  type SignalFunction[A, B] = shared.temporal.SignalFunction[A, B]
  val SignalFunction: shared.temporal.SignalFunction.type = shared.temporal.SignalFunction

  type TimeVaryingValue[A] = shared.temporal.TimeVaryingValue[A]
  val TimeVaryingValue: shared.temporal.TimeVaryingValue.type = shared.temporal.TimeVaryingValue

}
