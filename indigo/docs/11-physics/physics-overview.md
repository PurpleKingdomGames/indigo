# Physics Overview

Indigo includes a basic physics engine as of release `0.15.0-RC3`. The engine is what is known as a rigid body axis-aligned physics engine. This is as simple as physics gets! Axis aligned means that there is no support for rotation, but for the kinds of game Indigo is intended for, this is still good enough for a wide range of use cases.

Indigo's phyics support has not yet been battled tested/hardened or performance tuned yet. Please report issues. Still, creative and enterprising developers may be able to find a use for it.

## Live example: Pong!

A very small example of a running game using physics is our little Pong! demo (playable link on the repo):

[https://github.com/davesmith00000/pong](https://github.com/davesmith00000/pong)

In this demo the physics engine is simply used to deal with the ball colliding with the walls and paddles.

### Quick start

All of the physics primitives are available with the following import:

```scala
import indigo.physics.*
```

As always, a lot of work has gone into the API to let the types and IDE discoverable methods guide you, however, here are a few pointers to get you started.

Everything starts with a world, that you keep in your `Model`:

```scala
World.empty[String]
```

Notice the `String` type there? That is the type of your collider 'tags', and can be any type you like, an enum is a good idea. The tags are used to help you access and manipulate the scene. For example the world has methods like `findByTag`.

Initially, your world is a lot like outer space. No general forces or resistances. Let's add some gravity and wind resistance:

```scala
World.empty[String]
  .addForces(Vector2(0, 1000))
  .withResistance(Resistance(0.01))
```

What are these magic numbers? Well here you need to use some discretion. The physics engine does not understand the scale of your game - the scale can be anything! In this case we're adding a force that acts on all elements at a rate of 1000 'units' per second, meaning, at full speed an item will be moving 1000 'thingys' downwards. How do we decide what the units are/mean? Well in this case we have a screen that is 600 pixels high, so we're saying that terminal velocity (maximum speed by falling) allows you to cover just under 2 times the height of the screen in 1 second. Again, this scale is arbitrary, you will need to think about what makes sense for your game.

Now we need a bouncy ball and a platform for it to land on. Our screen is 800x600 in size, so positions are based on that:

```scala
World.empty[String]
  .addForces(Vector2(0, 1000))
  .withResistance(Resistance(0.01))
  .withColliders(
    Collider("ball", BoundingCircle(400.0, 80.0, 25.0)).withRestitution(Restitution(0.8)),
    Collider("platform", BoundingBox(10.0d, 550.0, 780.0, 20.0)).makeStatic
  )
```

Physics engines use things called 'colliders' to represent physical things in the world that can ...collide! Colliders can be `Collider.Circle` or `Collider.Box` types.

We have two colliders in our scene:

1. The first collider is our ball, it's positioned near the top of the screen, centered horizontally. The ball has been given a 'restitution' value of `0.8`. 

> Restitution is how much energy is retained when it bounces, so `0` would be no bounce at all, and `1` would be a perfect elastic bounce.

2. The second is our platform, notice that the platform has been made 'static'. This has a few effects internally, but all you need to know right now is that it won't move no matter what happens.

This simulation is entirely self contained, but you can interact with it using the various world 'find', 'modify' and 'remove' methods. You can also add transient colliders, which are colliders that don't exist in the world but are added deliberately on update / presentation for some reason. A good example of this is the paddles in the [Pong demo](https://github.com/davesmith00000/pong), which are added every frame because they are directly controlled by the player (***Tip:*** Make things like this static!). They need to be in the world for the ball to interact with, but otherwise aren't an on going part of the simulation.

Ok, so we have a simulation set up, how do we use it? 

1. First, we place it in our model during the `initialModel` call.
2. On each `FrameTick` of the `updateModel` function, we need to update it using: `world.update(context.delta)` (`delta` is the time delta, or the time that has elapsed since the last frame. All calculations are frame rate independent.)
3. We need to decide how to present our world...

Presentation comes in a few flavours, which you can mix and match to suit your needs. You can:

1. Call `present` on the world and render a `Batch[SceneNode]` based on what is in the simulation. Present can also perform a partial presentation based on the filter arguments.
2. Look up the colliders or map over them yourself and render the parts you care about. Perhaps your simulation is abstract and you just need information rather than it being a direct 1-1 collider to renderable relationship.
3. Render things in your model directly, that only affect the sim as transient colliders (like the paddles in Pong).

In our case, let's just render the world and see what's going on*:

```scala
def present(world: World[String]): Outcome[SceneUpdateFragment] =
  Outcome(
    SceneUpdateFragment(
      world.present {
        case Collider.Circle(_, bounds, _, _, _, _, _, _, _) =>
          Shape.Circle(
            bounds.position.toPoint,
            bounds.radius.toInt,
            Fill.Color(RGBA.White.withAlpha(0.2)),
            Stroke(1, RGBA.White)
          )

        case Collider.Box(_, bounds, _, _, _, _, _, _, _) =>
          Shape.Box(
            bounds.toRectangle,
            Fill.Color(RGBA.White.withAlpha(0.2)),
            Stroke(1, RGBA.White)
          )
      }
    )
  )
```

(* Tip: Rendering colliders on top of your game can also be handy for visual debugging.)

### Limitations and issues, bullets and paper walls

This is a limited Physics model, and it will have problems both in terms of physical accuracy (even within the scope of what we model) and in terms of initial feature set. You are welcome and encouraged to help by playing with it, reporting issues, and helping with code / maths / physics if you can.

One example of a known limitation of the current model, is that it suffers from what is sometimes called the 'bullet through paper' problem.

Simply put: If a projectile is moving fast enough and an object in it's flight path is small / thin enough, then on frame A the projectile's calculated position is on one side of the object it should collide with, and on the next frame B it's calculated position is on the other side of the object in it's way. The result is that the two objects never collide in the engine and carry on unobstructed, even though they clearly would collide in the real world.

You can see this problem for yourself if you go and play the [pong demo](https://davesmith00000.github.io/pong/) until the ball is going _really, really fast!_ (It doesn't take long provided you can keep up.)
