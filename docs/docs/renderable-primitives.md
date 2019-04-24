# Renderable Primitives & Building Blocks

Indigo is made of several projects, but two of particular note are what are currently called "indigo" and "indigo extensions".

The extensions project is quickly becoming the defacto framework for building games in indigo, providing things like buttons and scene management.

However, everything that is possible in the extensions project, is only possible because of the basic building blocks provided by Indigo itself. Indeed, there is no need to use the extensions lib at all if you're feeling adventurous.

## The Scene Graph

When you put together a scene to be rendered, you are building a graph, known as a Scene Graph - it's really just a tree of things to draw, and Indigo's notions of a Scene Graph are fairly simplistic.

All the renderable elements can have basic effects like Alpha and Tint applied to them.

The main scene graph node types provided by Indigo for drawing things on the screen are as follows:

## Group

A group is not a renderable element - and is in fact removed at render time - but it is used to help organise elements together. Putting several graph nodes into a group allows you to move them around as one.

## Text

Provides basic support for rendering text onto the screen, including things like alignments and multiline text fields.

Requires you to have pre-established font information that you can link to using a key.

## Graphic

The simplest type of drawn element, simply renders a bitmap (which may be cropped) onto the screen.

## Sprite

Essentially an animated graphic. You need to provide a key back to your animation information and then you can instruct the sprite to change animation cycles and play or stop.
