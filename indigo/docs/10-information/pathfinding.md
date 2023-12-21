# Path finding

Indigo includes a generic pathfinding algorithm (A* generic variant) as of release `0.15.3`.

### Quick start

All of the pathfinding primitives are available with the following import:

```scala
import indigoextras.pathfinding.*
```

The computation of the path can be done with the following function call:

```scala
// def findPath[T](start: T, end: T, pathBuilder: PathBuilder[T])(using CanEqual[T,T]): Option[Batch[T]]
PathFinder.findPath(start, end, pathBuilder)
```

`start` and `end` have the same type and are the start and end points of the path.
`pathBuilder` is the type allowing to customize the pathfinding algorithm (see below).

If `start` and `end` are of type `Point`:
- when a path is found, the function returns a `Some[Batch[Point]]` containing the path from `start` to `end`.
- when no path is found, the function returns `None`
- when `start` and `end` are the same point, the function returns `Some(Batch(start))` (that is also `Some(Batch(end))`).

You may also find samples in the tests `indigoextras.pathfinding.PathFinderTests` or in the sandbox `com.example.sandbox.scenes.PathFindingScene`.

### PathBuilder

The path builder is a trait that allows to customize the pathfinding algorithm.
It requires the implementations of the 3 main characteristics of the A* algorithm:
- `neighbours`: the function that returns the neighbours of a point
- `distance`: the function that returns the distance (cost) to reach a neighbour from a point
- `heuristic`: the heuristic function used to estimate the distance (cost) from a point to reach the end point

The path builder also requires a given of type `CanEqual[T,T]` to compare the points.

## Default path builders

This object contains default path builders for the most common use cases.
It also contains a few helper functions and constants to compute the neighbours and to define the allowed movements.
If you need to customize the pathfinding algorithm this file is a good starting point.

Indigo provides default path builders, for `Point`, located in `indigoextras.pathfinding.PathBuilder` companion object.

- `PathBuilder.fromAllowedPoints`
- `PathBuilder.fromImpassablePoints`
- `PathBuilder.fromWeightedGrid`
- `PathBuilder.fromWeighted2DGrid`
