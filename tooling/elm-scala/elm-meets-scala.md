# When Elm met Scala.js

I love Elm, and sometimes I hate it.

> Everything in Elm makes sense when taken holistically.

It's my little zen mantra.

When Elm throws me another curve ball and I notice that I've started shaking my head at the screen or getting annoyed because something perfectly reasonable just can't be done. I stop, recite my little mantra and think "Ok Evan, what did I miss this time?"

Even Czaplicki is Elm's benevolent dictator, and he has a plan it seems, or at least a very strong sense of direction. As a piece of holistic thinking where everything has a rhyme and reason, Elm is actually quite a fascinating thing. It's also infuriating, because unlike most languages where things are added over time, things in Elm and generally - bravely - removed or locked down in newer versions.

If you don't subscribe to the Elm way of doing things, turn back now. But if you're prepared to fully embrace, you're in for a treat. In my humble opinion there isn't a better, more satisfying or productive and reliable way to do Frontend dev today.

## The JS Masses

One of the things driving Elm's design that is critical to grasp early on is that Elm, although superficially like Haskell, isn't meant for hardened Functional Programmers, it's meant for that masses of JavaScript folks.

While the JavaScript community may or may not decide to embrace Elm, it must be said that I am not a JavaScript person. Most of the criticism that I've sympathised with over the years has been squarely from the functional programming camp. We're functional programmers write code in a functional lanaguage, we know how to do this, why aren't you letting us?

### Infix Operatiors

Let's start with an easy (and arguably petty) example. Elm no longer allows you to define inflix operators.

It's entirely normal in FP to be able to construct infix operators, for example:

`(|+|) : a -> a -> a` would be used on any two `a`'s like the `+` operator, presumably combining them in some way, e.g. `x |+| y`.

Elm used to allow this, but now does not. That function would now need to be declared as `combine: a -> a -> a` and used `combine x y`.

The idea is consistency. Adding infix operators is just another thing to learn, another thing to grok, another barrier to entry. Sure, once you're used to seeing it it's no big deal, but I do remember my early Scala days trying to work out where the funny operators had come from.

It is a real problem, and if you take a deep breath you - hard working FP'er - can probably grumble but stomach that design choice. You can be the bigger person here.

### Real problems. Probably

Then the metphorical poop really hits the equally metaphorical fan.

If you're a statically typed functional programmer, then you have expectations about what tools should be in your toolkit. There will be Functors, there will be abstractions, there will be typeclasses and ad-hoc polymorphism. You may forgive a new language for having an incomplete suite a capabilities, perhaps you'll tell yourself you'll come back when the language grows up a bit or even that you'll take pity on the poor overworked authors and write that bit for them.

Where FP'ers get stuck with Elm is that it's been around for ages and the authors actively tell you that your expected toolbox will not materialise since it's not needed. Not only is it not needed, it's actually not possible to implement, and it never will be. The reaction is typically that the language authors have got it wrong, such a shame.

I fully sympathise.

There are a bunch of issues that FP'ers like to point at with Elm - like the lack of Higher Kinded Types - but their abscence is all explainable, I won't tackle them all here but I will pick off the most contentious one: TypeClasses, or the lack thereof. I've wasted countless hours pointlessly trying to make them work - despite the fact that I'm about to explain why you don't need them.

### The TypeClass Issue

Statically Typed Functional Programming is almost entirely about programming with abstract functions that are materialised into concrete solutions as late as possible. Consider something simple:

```elm
asString : Show a -> a -> String
```

This function signature reads as follows: Given evidence (and an associated function to do the work) that some unknown type defined as `a` can be turned into a String (recall, `Show` turns things into String), presumably carry on and do that with the `a` provided in the second argument.

Certainly there are problems here, for one thing the type signature doesn't preclude noddy solutions like:

```elm
asString : Show a -> a -> String
asString _ _ =
  "Hello"
```

That said, we've done an okay job here. We've used a totally abstract type and heavily constrained it by saying "The only thing you can do here is turn it into a `String`, nothing more." The power of this is that the type signature is so tightly specified that - noddy solutions and mistakes aside - we can with experience fully guess the implementation, it *damn well ought to be*:

```elm
asString : Show a -> a -> String
asString showA value =
  showA.toString(value)
```

That's completely valid Elm code by the way. Looks a lot like a TypeClass doesn't it.

The reason it isn't a TypeClass as we expect them to behave is that the instance isn't "found" by the compiler, it must be provided concretely, and that concreteness hampers your ability to abstract deeply.

Surely that's a mistake? Doesn't that mean every single thing has to be concretely defined? Yes it does.

The justification is this: What do you need them for? Let's take a concrete example.

### Requests and Responses

The idea is fairly straightforward. Http microservices all (after a fashion) satisfy a function of signature:

```scala
Request => F[Response]
```

That is, you accept a request and produce a response. The `F` is always some sort of effectful Functor e.g. a `Future`, `Task` or `IO` - something that can handle the fact that responses are asynchronous, probably need some threadpool management, and could fail with an `Exception`.

We can probably fairly easily imagine parsing and mindlessly mapping the request into some valid input, and also converting a domain back into a response, so now we have a function to satisfy that looks like:

```scala
def doBusinessLogic(input: ValidInput): F[UsefulData] = ???
```

Hang on. How do we make one of those `F` things? We don't know what it is or what we can do with it, we certainly don't know how to make one. But we could, with a TypeClass:

```scala
def doBusinessLogic(input: ValidInput)(implicit P: Pure[F]): F[UsefulData] = {
  val output: UsefulData = ???
  P.pure(output)
}
```

There are other ways to encode this, but the code above simply says "Whatever `F` you give me, I must be able to make one". Find me an instance of `Pure` for `F` that has a function on it that allows me to make an `F` with a given underlying value.

At least two of the benefits here are:

1. Your business logic is constrained, and therefore easier to test and reason about. It will turn valid input into useful data and put it in some `F` type. It can't do anything else other than error.
2. Your business logic is not bound to any particular effect type - and by extension, any particular Http framework.

### Back to Elm

Elm, however, doesn't have effectful types in the same way.

There is no explicit concurrency - it's all taken care of for you.

You cannot throw an exception and just terminate the programme - you're doing frontend development, you can error but you have to recover, keep going and report something to your user. Bombing out isn't an option.

The use cases for TypeClasses are far fewer and less frequent, and as such, for the odd time it's handly you can get away with concrete instances explicitly passed around.

> Everything in Elm makes sense when taken holistically.

Just so we're clear: Acceptance of that reality doesn't mean I *like* programming this way, it feels counter intuitive to someone who has been sold on the idea that constrained types are empowering.

The most legitimate use case for a typeclass that I can think of in Elm, would be for a `Comparable a` instance. Elm dictionary's (because JavaScript presumably) can only accept certain "comparable" primitive types as keys, and being able to summon a `Comparable` for `a` would have been preferable. ...but if that and a couple of other like it are the only use case then the Elm authors designing for the general good and not for the exceptions is the smart move.

Take a deep breath, repeat after me:

> Everything in Elm makes sense when taken holistically.

Gah! :-)

## A really real, actual problem

Having got over the typeclass disappointment, lets look at an actual thing that definitely is a problem.

Elm works in a browser and is compiled to JavaScript. JavaScript communicates with the browser and exposes browser functionality via APIs. So far so good.

Mostly, when you need to do some interaction with the browser - such as draw to a canvas element - you'll find there is a nice safe Elm package to give you what you need. That does mean you're utterly dependant on their being a library though, and that the library exposes / wraps the specific functionality you need.

Some times that isn't the case though, and the normal solution is to use a Foreign Function Interface (FFI) that lets you call the thing you need. Elm does not have an FFI.

The whole entire point of Elm, is to be able to give guarantees. Pretty strong guarantees about correctness and errors (or the lack of them) specifically. If your code compiles in Elm, it almost certainly won't error.

However, JavaScript and a myriad JavaScript frameworks offer no such guarantees, therefore, allowing people to call JavaScript directly from Elm would obliterate the entire point of Elm's existance. So you can't do it. You cannot produce a type definition wrapping the AWS js lib, or React or anything else. You just can't.

There is no FFI to JavaScript because it would be unsound.

> Everything in Elm makes sense when taken holistically.

Deep breath: Ok, but I *need* to call JavaScript.

### Ports

Elm's solution to the problem is to use Ports.

Ports are little holes in Elm that allow messages to flow to and from JavaScript. This allows communication without breaking guarantees. The main thing to know about this process is that i

Elm ensures that if there is going to be an error, it never make it into the Elm part of the codebase, meaning that you can focus all your debugging efforts on the JS side of the fence.

So, the Elm codebase is the ordered, policed part of your codebase, and everything on the otherside is the badlands.

You could just write JavaScript at this point, but why not introduce a language that can handle the rugged outdoors a little better?

### Scala.js

There are lots of languages you could choose but I'm going with Scala.
