# Anti-Patterns

This page is an attempt to flag any anti-patterns, bad practices or traps that the authors have seen in the wild or have themselves fallen into! The hope is that but not repeating these mistakes your game dev'ving will be more fun and fruitful.

## General advice

### Uni-directional data flows

The main thing to remember when building in Indigo is that is follows a uni-directional data flow. That is to say that the data all goes in one direction.

## Anti-patterns and traps

### Feedback

A common thing to do, is to organise your game model into a hierarchy of classes/objects, and then have methods that delegate or invoke methods on their sub-objects who in turn need to somehow update their parent. As you can imagine, this can get very hairy very quickly.

#### Example: Health potions

In your game, all characters - player or otherwise - have an inventory:

```scala mdoc:js:shared
final case class Player(health: Int, inventory: Inventory)
```

Your player can can pick up health potions around the game and store them in their inventory, which can only hold one health potion at a time. Lets model the inventory as dumbly as possible to keep this really simple:

```scala mdoc:js:shared
case object HealthPotion
final case class Inventory(healthPotion: Option[HealthPotion.type]):

  def add(hp: HealthPotion.type): Inventory = 
    this.copy(healthPotion = Some(hp))

  def use: Inventory =
    healthPotion match
      case None => this
      case Some(hp) => this.copy(healthPotion = None)
```

We've given it nice little `add` and `use` methods too! While we're at it, the player needs updating:

```scala mdoc:js
final case class Player(health: Int, inventory: Inventory):

  def add(hp: HealthPotion.type): Player =
    this.copy(inventory = inventory.add(hp))

  def use: Player =
    this.copy(inventory = inventory.use)
```

Now you can call `add` or `use` on player and it delegates through to the inventory and everything is immutably updated. Great.

...there's some problems here though, lets look at two in particular:

  1. What if we're already carrying a health potion and we try and insert another one?

  2. The `use` method certainly removes the health potion from our possession, but how do we update the players health?

The anti-pattern is to try and feedback to the parent directly, consider these modifications to our inventory methods:

```diff
-def add(hp: HealthPotion): Inventory
+def add(hp: HealthPotion): (Inventory, Boolean)
```

Here we are adding a boolean to the return type to say "succeeded in adding the potion" (or not).

```diff
-def use: Inventory
+def use(player: Player): (Inventory, Player)
```

Here again we're going to inflate the return type so that now we can accept the player (our parent object), fiddle with it, and give it back... to itself... yuk!

#### Solution: Events

Instead of doing any weird feedback with the return types, we can just use the `Outcome` type to fire off events, consider the following alternate inventory implementation:

```scala mdoc:js:shared
import indigo.*

final case class CannotAddThis(healthPotion: HealthPotion.type) extends GlobalEvent
case object NoHealthPotionToUse extends GlobalEvent
case object IncreasePlayerHealth extends GlobalEvent

final case class AltInventory(healthPotion: Option[HealthPotion.type]):

  def add(hp: HealthPotion.type): Outcome[AltInventory] =
    healthPotion match
      case None =>
        Outcome(this.copy(healthPotion = Some(hp)))

      case Some(_) =>
        Outcome(this)
          .addGlobalEvents(CannotAddThis(hp))

  def use: Outcome[AltInventory] =
    healthPotion match
      case None =>
        Outcome(this)
          .addGlobalEvents(NoHealthPotionToUse)

      case Some(hp) =>
        Outcome(this.copy(healthPotion = None))
          .addGlobalEvents(IncreasePlayerHealth)
```

Now, when we consume a health potion we either report that there was one, or that the players health should be increased. Equally if we try to add a potion to an already full inventory, we report the problem and return the item. The events are collected on the next frame and actioned.

This will require a change to our player too to make it work:

```scala mdoc:js
final case class AltPlayer(health: Int, inventory: AltInventory):

  def add(hp: HealthPotion.type): Outcome[AltPlayer] =
    inventory.add(hp).map { inv =>
      this.copy(inventory = inv)
    }

  def use: Outcome[AltPlayer] =
    inventory.use.map { inv =>
      this.copy(inventory = inv)
    }
```

When we call `use`, it now returns an `Outcome[Inventory]` which we can map over (retaining any events created when we called `inventory.use`) and update our player.

Note that we don't try and update the player's health yet. To do that we'd add a new `increaseHealth` method to `AltPlayer` and have it invoked when the `updateModel` function receives the `IncreasePlayerHealth` event.

### Simultaneous updates

One place where uni-directional data flows begin to feel awkward and difficult, is when you need to update two things at once, where one's update depends on the other's update.

#### Example: Moving bad guys

There are two bad guys moving on a grid towards the player. There is only one empty space in front of the player, but on the next turn, both bad guys could walk into that space, _unless_ they were blocked by the other. Mapping over a list of bad guys is not sufficient because a `map` operation doesn't know how it's neighbours have been affected, and can only have the previous positions as context. In other words, if you just did `badguys.map(move)` they'd both end up in the same space.

What can you do?

#### Solution: Recursion

If the updates must happen right now on this frame, then one solution is recursion. By processing each bad guy in turn and having the context of the updated bad guys so far available, you can avoid collisions.

In pseudo-scala code:

```scala
@tailrec
def moveBadGuys(notMoved: List[BadGuy], moved: List[BadGuy]): List[BadGuy] =
  notMoved match
    case Nil => moved
    case nextBadGuy :: remainingBadGuys =>
      val nextPosition =
        // decide next position while checking for conflicts
        // within `moved`.
        ???
      moveBadGuys(remainingBadGuys, nextBadGuy.move(nextPosition) :: moved)
```
