# Elm meets Scala.JS

An example of two-way messaging between Elm and Scala.js.

I've taken the standard "counter" elm example and extended it slightly to show how we can invoke and respond to Scala.js via messaging.

## Running the example

Assumptions:

- You have checked out the repo
- You have SBT installed
- You have Node & NPM installed
- You have ParcelJS installed
- You have Elm 0.19 installed

From your terminal, move to the `scala` folder and run:

```bash
sbt clean update compile fastOptJS
```

Go back to the root directory and run:

```bash
npm start
```

Open a browser window and navigate to `http://localhost:1234`. Open your JavaScript console and press the buttons on the webpage.

## Motivation

Elm is very [marmite](https://en.wikipedia.org/wiki/Marmite), but in my opinion Elm is the best way to do general frontend development, however, it starts to seriously break down in two key areas:

1. Complex business logic where good support for programming abstractions would simplify the programmers work. In practical terms this is actually surprisingly rare from what I've seen.
2. Anywhere that an Elm package is insufficient and you need to drop down to JavaScript.

This little proof of concept aims to answer the latter of the two in particular, but may also help with the former.

The aim was, essentially, to end up with a typechecked pattern match of a Union or ADT on both the Elm and Scala sides of the divide respectively, for strong, well typed communications.

Do all your general web dev in Elm, then drop down to Scala.js when you need more power, more flexibility ...and frankly more capacity for dirty tricks!

## Why Scala.js?

This would work equally well in any other language that can be compiled to JavaScript.

That said, it makes sense to use an impure functional language that supports a Foreign Function Interface for maximum JS compatibility, pattern matching, excellent functional abstractions, great library support, and good compiler typechecking support. Otherwise why are you even bothering? :-)

I'm a Scala developer and bias, but Scala.js fits the bill perfectly.

## Elm and JavaScript

Elm is complied to JavaScript, but does not permit you to call native JavaScript functions. This makes sense since Elm's purpose is to offer compile time correctness guarantees, and if you could just call any old JavaScript code there would be no way to enforce those guarantees.

The idea in Elm is **NOT** to create a type definition of every function in your favourite JS library and call it as normal, function by function. What you're supposed to do is called down to JavaScript via Elm's **Ports** where you're asking for a whole piece of logic to be invoked rather than an individual operation.

Elm can then enforce type correctness on messages going out or coming back in, and any errors in logic are almost certainly in the JavaScript code living "outside" the port.

## Performance considerations

Given that the solution presented is both asynchronous and relys on JSON encoding (elm), decoding (scala), encoding (scala), and finally decoding (elm), it looks slow and inefficient - and I assume that's true, though I haven't benchmarked it.

Important to note though that although it is not expected to be a performant solution, the anticipated invokation frequency is in response to *human* activity i.e. very infrequent, and therefore performance is actually not a concern at all.

Equally, doing this in a game loop would be a terrible idea!

