# Entry Point Types

There are currently four ways to make an indigo game.

## Game Data Types

Indigo wants to allow you to use precisely the types that best describe your game, rather than inheriting from some nonsense base type, and so requires you to declare them somewhere.

The three types we need are:

1. A type representing your games start up data.
1. A type representing the model of your game.
1. A type representing your game's view model.

## Imports

All entry point types can be used by importing:

```scala
import indigoexts.entrypoint._
```

## 1. Scene Manager Template

This is the recommended entry point type, and can be created by extending your main object with `IndigoGameWithScenes` as below:

```scala
object ScenesSetup extends IndigoGameWithScenes[MyStartUpData, MyGameModel, MyViewModel] {
  ...
}
```

Most games are divided up into different screens or scenes like a menu, the game itself, and of course the game over screen!

The entry point with the embedded scene manager makes this process much easier. You can roll your own too of course.

More more information, please see [scene management](scene-management.md) or take a look at the examples.

## 2. Basic Template

No frills game entry point, simply extend your initial object with `IndigoGameBasic` as below:

```scala
object BasicSetup extends IndigoGameBasic[MyStartUpData, MyGameModel, MyViewModel] {
  ...
}
```

Please take a look at the examples for more information.

## 3. Builder Pattern

The builder pattern is not recommended, but is technically the most powerful entry point. It looks like this by default:

```scala
def main(args: Array[String]): Unit =
  Indigo.game
    .withConfig(config)
    .withAssets(assets)
    .noFonts
    .noAnimations
    .startUpGameWith(setup)
    .usingInitialModel(initialModel)
    .updateModelUsing(updateModel)
    .initialiseViewModelUsing(initialViewModel)
    .updateViewModelUsing(updateViewModel)
    .presentUsing(renderer)
    .start()
```

...which is no different from the basic setup, but you can also do this:

```scala
def main(args: Array[String]): Unit =
  Indigo.game
    .withAsyncConfig(config) // <--
    .withAsyncAssets(assets) // <--
    .noFonts
    .noAnimations
    .startUpGameWith(setup)
    .usingInitialModel(initialModel)
    .updateModelUsing(updateModel)
    .initialiseViewModelUsing(initialViewModel)
    .updateViewModelUsing(updateViewModel)
    .presentUsing(renderer)
    .start()
```

The idea here was that by default, all assets and config are baked in and front loaded into Indigo. However, it might also be desirable to start up an Indigo instance that could be configured dynamically by loading config from a server. This line of enquiry works, but isn't in active development

Please take a look at the examples for more information.

## 4. Roll Your Own Frame Processor

All of the entry points previously described are part of the indigo extensions project, which means they are abstractions and not part of the core engine.

Since the core engine is what runs the game, it must follow that each of the above knows how to initialise the game somehow, and indeed they all do so in more or less the same way, crudely, by initialising a `new GameEngine(...)`.

Initialising a GameEngine instance isn't hard, and broadly not so different in terms of it's arguments as the builder pattern above.

The only thing to be aware of is that you'll have to construct your own `FrameProcessor` or make use of the `StandardFrameProcessor` somehow.

More information on `FrameProcessor`s can be found [here](key-concepts.md)
