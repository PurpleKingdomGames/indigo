# Collections, Abstractions & TypeClasses

One decision that was made in indigo was not to include any foundational libraries like Cats or Scalaz.

The reason is very simple: We want you to be able to use those libraries to make your games, without any dependancy clashes.

The drawback of that decision is that Cats and Scalaz are full of useful goodies that we couldn't use. A few of them were too good to ignore though, and have been reimplemented in Indigo. A lot of effort has gone into not having those affect you too much, but this will no doubt to reviewed.

## Collections

## Abstractions

## TypeClasses

### AsString

`AsString` is a very simple `Show` like typeclass, and indeed it uses the term `show` for it's extension method.

Scala likes to enthusiastically toString things whenever if can't work out how to add things together, leading to bugs.

Using the `AsString` typeclass ensures that any conversion to String is intentional.

#### Examples:

Using the extension methods will always work if the type is either a primitive, or has an AsString instance in it's companion object.

```scala
import indigo.AsString._

10.show // "10"

```

Summoning an `AsString`:

```scala

def foo(i: Int)(implicit s: AsString[Int]): String =
  s.show(i)

// OR

def bar(i: Int): String =
  implicitly[AsString[Int]].show(i)

```

#### Creating an `AsString`

```

final case class Foo(id: Int)

object Foo {

  implicit val s: AsString[Foo] =
    AsString.create { foo =>
      s"Foo(id = ${implicitly[AsString[Int]].show(foo.id)})"
    }

}

```

### EqualTo
