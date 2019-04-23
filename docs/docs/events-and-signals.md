# Events & Signals

## Events

Indigo is powered by events, `GlobalEvent`s to be exact. Events represent discrete actions that took place in the previous frame, such as a mouse click.

The lifecycle of events in Indigo is very strict so that we can support referential transparency.

- Events are immutable.
- Events are ordered.
- The events present at the start of a frame are all of the events.
- Events generated during the frame will only become available in the next frame.
- Events only last for a single frame, whether acted on or not, before being disposed of.

The events list received by the frame is never empty, it always contains one `Frametick` event, and it is always last.

### List of Event Types

## Signals

**Not to be confused with the `Signal[A]` type!**
There is an open question about the relationship between `Signals` and `Signal[A]`.

If Events represent things like key presses, Signals represent values that are "alway on" like the mouse position.
