---
id: primitives
title: Primitives & Building Blocks
---

> This page has not yet been reviewed for compatibility with version 0.9.2. Details may now be incorrect.

Indigo is made of several projects, but two of particular note are Indigo itself, and "Indigo Extras".

Although it's called the "Extras" project, and is completely optional, it nonetheless contains some useful types and constructs to help you build your games, such as Buttons and some pre-made SubSystems like an FPS counter, automata, and an asset loader.

The important thing about the extras project is that everything in it is derived from the building blocks provided by indigo itself. You could rebuild all of it yourself without having to modify the core in anyway, and indigo doesn't depend on anything in the extras project.

What are the building blocks in use?

## Scene graph nodes

When you put together a scene to be rendered, you are building a graph, known as a ["Scene Graph"](https://en.wikipedia.org/wiki/Scene_graph) - it's really just a tree of things to draw, and Indigo's notion of a scene graph is fairly simplistic and gloriously [anemic](https://en.wikipedia.org/wiki/Anemic_domain_model).

Each element of the graph is called a node, and there are different types of node.

### Non-Renderable Node Types

Non-renderable nodes are nodes that are meaningless except in the context of other nodes.

#### Group

A group is an element that is removed at render time - but it is used to help organise elements together. Putting several graph nodes into a group allows you to transform them as one unit, any elements that have their own properties set under the group, do so relative to the groups properties. You can also nest groups under groups.

#### Clones & Clone Batches

Clones only work in conjunction with a `Cloneable` primitive: Currently `Sprite`s and `Graphic`s.

The purpose to clones is to render copies of a primitive many times very, very quickly, where you would only want to change a few properties, an example might be clumps of grass scattered around a level.

The specific properties you can change are:

- Depth (see below)
- Position
- Rotation
- Scale
- Alpha
- Flip horizontal
- Flip vertical

There are two clone types: `Clone` and `CloneBatch`.

A `Clone` of a primitive works exactly like a cheap copy. The nice thing about `Clone` is that you can scatter them about in your scene graph and still pay a smaller price per render.

If you've got A LOT of something to render though, you should use a `CloneBatch`, which is even more efficient. The penalty for this efficiency is that you have to draw them all at once, and all at the same depth (otherwise following the normal depth rules).

### Renderable Node Types

All renderable nodes come with the same set of basic transforms that you see on a clone:

- Depth (see below)
- Position
- Rotation
- Scale
- Alpha
- Flip horizontal
- Flip vertical

They are then specialised to do certain jobs.

#### `Text[M <: Material]`

Provides basic support for rendering text onto the screen, including things like alignment.

Requires you to have registered a `FontInfo` instance that you can link to using a `FontKey`.

#### `Graphic[M <: Material]`

The simplest type of drawn element, simply renders a bitmap (which may be cropped) onto the screen.

#### `Sprite[M <: Material]`

Essentially an animated graphic. You need to provide an `AnimationKey` to link back to your registered `Animation` information, and then you can instruct the sprite to change animation cycles, and play or stop.

## The Event Loop

The glue that binds the whole thing together is the event system. Events allow you to communicate through time and space from one component to another. Indigo's event life cycle is very strict. Events triggered in this frame will only be apparent in the next frame, available to all, and then expire (unless propagated again).
