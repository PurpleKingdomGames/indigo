# Constraints & Motivation

> Indigo is a "code only", pixel art game engine, initially designed for web based games, and aimed squarely at Scala functional programmers.

Indigo was designed to meet very specific needs, and those needs drove a number of hard constraints in order to make the project achievable, and keep feature creep to a minimum. In the future we may relax those constraints, but for now we're holding fast to the course.

...which is why Indigo has no rotation - who'd ever want to rotate pixel-art?

## Statically Typed Functional Programming

Building games is hard. Testing games is harder.

The reason testing games is hard is the perception that they are random, and their behaviour non-deterministic by default, but this doesn't have to be the case.

Indigo encodes the idea of a frame update into one single, pure, stateless, immutable function. The new frame is always predictably the direct outcome of the values it was supplied at the beginning of the update. Even apparently random elements are in fact pseudo-random.

On of the aims of Indigo is to make frame updates referentially transparent. Of course, this depends on the game programmer. If they put of big `Random` in the middle there isn't much we can do about it! (Use the `Dice` instead!)

To further increase reliability and code correctness, Indigo is written in Scala in order to take full advantage of it's relatively advanced type checker.

Indigo is not an FRP engine, and does not force a particular programming model on the developer. A game programmer could write "Scala-Java" or as close to pure FP code as Scala allows. To further empower the developer, the engine has very few dependencies, so mixing in a library like Cats or Scalaz should be no problem at all.

## Code Only

Indigo does not have an IDE or Editor software application. It's designed by a programmer, for other programmers.

There is no content pipeline, hot loading, visual node graph editors, material selectors - nothing but you and a code editor!

Hypothetically, if we wanted any of those things I think the idea would be to write separate tools for specific jobs.

## Pixel Art

Indigo is aimed at Pixel Art! Why Pixel Art? Well, two reasons:

1. Pixel Art is *very* cheap to produce, and done well, is still a wonderful graphical style. It's not about technical limitations, it's about being able to make fun games efficiently.
1. If you want to make a AAA title with photo-realistic graphics, there are better tools out there for the job! Pixel art requires us to build games that are good games in spite of a limited style, games that draw people in because they are engaging, not because you can see every wrinkle on a characters face. Not unlike board games.

Forcing the style to be aimed at pixel art has created a few interesting design choices:

1. As previously mentioned, you can't do arbitrary rotation, since that makes pixel art look bad. Right thing? Wrong thing? It is what it is at the moment.
1. Your game can be magnified. You design and code it to work on a 1 to 1 pixel scale, increase the magnification and everything goes with it. For instance, mouse positions and clicks are rescaled to remain accurate to your graphics.
1. Perfect pixel rendering. The whole engine works on whole pixels, and the shaders are written to render beautifully crisp, whole pixels only.

## Web Games ...and more!

Initially Indigo was designed only for making web games, however it is being ported to other platforms. Initially this will be the JVM.
