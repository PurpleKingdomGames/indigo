# Fireworks

This is an experiment in Automata, Signals, and Property Based Testing, in that the scene is made as far as possible from only those elements. This isn't how I expect most people to use Indigo.

## An early mistake worth learning from...

Indigo works on perfect pixels and enforces this through types like `Point` which only works on `Int`. The coordinates system also lends itself to pixels starting at 0,0 in the top left and progressing downwards; completely normal for a 2D graphics engine.

As such, it is very easy to get caught up thinking about your game in _screen space_, which is the actual place that things are put on the screen, since Indigo doesn't force or encourage any other sort of coordinate space.

The problem with working in screen space for anything non-trivial is that if forces you to start considering _rendering_ problems in your _model_, and your model shouldn't care about such things. For example: I knew that I wanted the fireworks to start in the center third of the screen, so I baked that right into the code. What I should have done is give them an arbitrary starting point from, perhaps, 0 to 1, and simply moved / scaled them into the right place at the point of rendering.

3D game engines don't have this problem as they strictly separate _screen space_ from _world space_. World space has arbitrary units and proceeds in the x,y,z axis from a hypothetical 0,0,0 position. The Camera makes it real by converting world space to screen space during rendering, and there are methods for going the other way to account for things like mouse clicks (which are screen coordinates, not world coordinates).


