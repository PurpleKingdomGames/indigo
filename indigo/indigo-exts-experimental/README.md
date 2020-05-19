# Indigo Extensions

The Indigo Extensions library is a set of non-core abstractions and constructs to make game building easier.

You do not need the extensions library to use Indigo, but you're have to build everything yourself from the primitives indigo provides.

For example, Indigo does not have a scene manager because it does not care how you organise your game, but normally a scene manager would be a pretty useful thing to have. You can build it yourself of course, or you can use the one provided.

Indigo extensions also provide a number of simplified ways to initialise your game in the `entry` package.

## Packages

`import indigoexts.datatypes._`

Makes better collections available

`import indigoexts.entrypoint._`

Gives access to indigo entry points and entry point utils

`import indigoexts.automaton._`

Classes for managing automata

`import indigoexts.lens._`

Exposes basic lenses

`import indigoexts.grids._`

`import indigoexts.grids.pathfinding._`

Grid datatypes and grid search / pathfinding utils


`import indigoexts.lines._`

`LineSegment` is a line data type that takes two points and produces a line segment that can be tested for collisions with other line segments.

`import indigoexts.quadtree._`

`QuadTree[T]` type allows for the creation of square quad trees (power of two is best). Quad trees can be accessed by coordinate, and also searched by line or rectangle.

`import indigoexts.scenes._`

Scene management.

`import indigoexts.ui._`

Abstractions for buttons and inputfields


 