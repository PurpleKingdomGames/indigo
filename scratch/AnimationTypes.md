# Animation Types

# Stateless
Stateless animations are animations that are achieved without a timeline by combining the GameModel and the GameTime.
All of the details of what the animation should be doing now must be held in the model and will probably involve maths.

This is a very direct style of animation where the programmer takes full responsibility for making it work and changes
applied and reflected immediately.

# Timeline based
In timeline animation (using sprites) the programmer models the state that the sprite should be in with his domain model,
and then issues animation actions to the the sprite such as gotoAndStop(0) or play or stop or playCycle("walking").

This is indirect. The state is preserved with no programmer intervention and no direct access to timeline properties.

The process is:
 - After the view function has completed, the animations will be in a default / vanilla state.
 - If the sprites bindingKey has been seen and saved before, then the sprite's memento is applied moving all the
   animation pieces into the right state.
 - Animation commands are then applied.
 - Sprite mementos are then stored.
 - Sprites that no longer exist in the view but whose ids have mementos that are still be stored are GC'd.