package indigo.shaders

object ShaderDsl2 {

}

final class Channel[A, B](f: A => B)

/*

Latest thought:

You can't write complete shaders, your constrained by when you're given.

First, send time in as a Uniform to both shaders.

Vertex shaders - most of what is there moves the vertex to the right place and then spends the rest of it's time piping data to the fragment shader.
So we could just give you a Vertex => Vertex function/expression if you want to modify it.

Fragment - all the values currently being piped over are what you have to work with. (What if you want to send something custom?)
Your job is the write a function / expression that takes a pixel value and creates the output color.

Problems:
- No custom channels - could use (expensive) uniforms?
- What happens if you want to draw a shape. Can the maths eb expressive enough? (probably...)

*/


