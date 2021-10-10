---
id: shader-overview
title: Shaders Overview
---

## What is a shader?

A shader program is a compiled computer program executed on your graphics card that instructs the GPU "where" on the screen to draw to, and "what" to draw when it gets there. In the case of Indigo which uses WebGL 2.0, a shader is made up of a pair of programs, called the vertex (where) and fragment (what) programs.

> The terms "shader", "program", and "shader program" are used somewhat inter-changeably here.

## How to get started with Shaders

There is a brief introduction to using shaders in your project as part of the ["how to create a custom entity"](guides/howto-custom-entity.md) guide that we recommend you read first, which also has an accompanying [example repo](https://github.com/PurpleKingdomGames/indigo-examples/tree/master/howto/custom-entity).

## Entity vs Blend Shaders

Indigo has two distinct flavors of shader.

1. [Entity shaders](entity-shaders.md) are used to draw individual items / entities on the screen.
2. Blend shaders are used in the [blending process](blending.md) to tell Indigo how to merge layers together.

They both work in a similar way, but there are differences.

### Functions available for override

A large part of what you're doing when writing shaders is providing implementations for shader functions that are called at different stages of the shading process. Both types of shader support a `vertex` and `fragment` stage, but entity shaders also have `prepare`, `light`, and `composite` which are used for lighting.

The default implementations are simply blank function calls, as follows:

**Entity shader functions**

```glsl
void vertex(){}
void fragment(){}
void prepare(){}
void light(){}
void composite(){}
```

**Blend shader functions**

```glsl
void vertex(){}
void fragment(){}
```

**Function uses**

Name|Sequential order|description
---|---|---
`vertex`|1|Used to modify the space on the screen the entity occupies, and to pass data to the fragment shader.
`fragment`|2|Tells Indigo what color each pixel needs to be.
`prepare`|3|Called before `light`, prepare gives you an opportunity to set up any data or functions needed for the lighting process.
`light`|4..4n|Called before `composite`, `light` is called once per light in the scene, and is used to build up per pixel lighting data separate from the colour data that results from `fragment`.
`composite`|5|Called last, `composite` is used to override how lighting information is combined with unlit pixel color data.

To override a function you simply need to declare it. In an `Source` shader type, this could be done as follows:

```scala mdoc
import indigo._

val shader: EntityShader =
  EntityShader
    .Source(ShaderId("my-colored-shader"))
    .withFragmentProgram(
      """
      |void fragment() {
      |  COLOR = vec4(0.0, 1.0, 0.0, 1.0);
      |}
      |""".stripMargin
      )
```

#### External source files

External files are more convenient for shader editing, allowing you to use tools like linters, and Indigo has a particular way of structuring them. The idea was to allow you as far as possible to utilise all existing GLSL editing tools, so the process below is designed not to interfere with them.

In an external shader file, we need to tell Indigo which parts of the file to use where, like this:

```glsl
//<indigo-fragment>
void fragment() {
  COLOR = vec4(0.0, 1.0, 0.0, 1.0);
}
//</indigo-fragment>
```

Indigo uses tags like the one above to do this, the tags are simply as follows (note that the `//` comments are significant so that GLSL tools ignore them):

```glsl
//<indigo-"function to override">
void "function to override"() {
  ..
}
//</indigo-"function to override">
```

Any code declared inside the "tags" will be inserted into the final shader, and you are not limited to declaring only the required override function, you could declare other functions and variables as needed.

Another useful trick is declaring variables outside the tags. If for example, you'd like to use Indigo's in-built `UV` variable, you can do this:

```glsl
vec2 UV;

//<indigo-fragment>
void fragment() {
  COLOR = vec4(UV, 0.0, 1.0);
}
//</indigo-fragment>
```

This allows the GLSL linting tools to pass since the variable is declared before it is used, but Indigo will ignore it and use the real one instead.

### Constants and variables

The different shader types also have some shared and some specific constants available to them, for example entity shaders read texture colours from the `CHANNEL_0`, `CHANNEL_1`, `CHANNEL_2` and `CHANNEL_3` variables, but blend shaders use `SRC` and `DST` instead.

A complete list is available on the ("Shader Constants, Variables, and Outputs")[constants.md] page.

## Loading external shader files

Getting an external shader into Indigo is no different from loading any other text asset:

```scala mdoc
def assets: Set[AssetType] =
  Set(
    AssetType.Text(AssetName("my vertex shader"), AssetPath("assets/shader.vert")),
    AssetType.Text(AssetName("my fragment shader"), AssetPath("assets/shader.frag"))
  )
```

> Note, best practice is to do what is shown in this example and have two shader files with the same name, one with a `.vert` and the other with a `.frag` extension. These are standard shader file names that your GLSL editor of choice will understand. Technically they could all be in the same file, but if you need to pass data from your vertex to your fragment shader (using varyings) then you'll run into problems with conflicting variable names, e.g. you'd have to declare `out vec4 mydata;` and `in vec4 mydata;` in the same file, which a linter won't like.

Here we're loading two shader files with potentially a complete set of function overrides, but please note that you only need to supply what you need. If you only need a fragment shader that overrides `void fragment(){}`, you can just supply that and Indigo will use defaults for the others.

Next we need to build our shader:

```scala mdoc
object CustomShader:
  val vertAsset: AssetName = AssetName("my vertex shader")
  val fragAsset: AssetName = AssetName("my fragment shader")

  val shader: EntityShader.External =
    EntityShader
      .External(ShaderId("my shader"))
      .withVertexProgram(vertAsset)
      .withFragmentProgram(fragAsset)
      .withLightProgram(fragAsset)
```

Finally we need to tell Indigo about these shaders, or we won't be able to use them:

```scala mdoc
    Outcome(
      BootResult.noData(GameConfig.default)
        .withShaders(CustomShader.shader)
    )
```

> Note that shaders must be fully declared during game boot up, you cannot add more later.

## Using shaders in your scene

Custom shaders are generally expected to be used in conjunction with custom entities, please see the [guide](guides/howto-custom-entity.md) for examples.

You can also use custom shaders to override the behavior of built-in materials. Technically this is as easy as replacing the `shaderId: Option[ShaderId]` field on the material with the id of your own shader, but you will need to look into the source code to determine data that material provides to the shader.

## Providing data to your shaders

Shaders without any data can still be useful if you have a known effect, and not sending data is less costly than sending it! However, sometimes you need to tell your shader about what you need it to do, and for that we use `UniformBlock`s (know as UBO's technically, Uniform Buffer Objects).

In the [guide](guides/howto-custom-entity.md), we create a custom entity that fills it's self with a solid color:

```scala mdoc
final case class MyColoredEntity(position: Point, depth: Depth) extends EntityNode:
  def size: Size        = Size(32, 32)
  def flip: Flip        = Flip.default
  def ref: Point        = Point.zero
  def rotation: Radians = Radians.zero
  def scale: Vector2    = Vector2.one

  def withDepth(newDepth: Depth): MyColoredEntity =
    this.copy(depth = newDepth)

  def toShaderData: ShaderData =
    ShaderData(MyColoredEntity.shader.id)

object MyColoredEntity:
  val shader: EntityShader =
    EntityShader
      .Source(ShaderId("my-colored-shader"))
      .withFragmentProgram(
        """
        |void fragment() {
        |  COLOR = vec4(0.0, 1.0, 0.0, 1.0);
        |}
        |""".stripMargin
      )
```

If we wanted to supply the colour, we need to modify our code as follows:

```scala mdoc:nest
import indigo.ShaderPrimitive._

final case class MyColoredEntity(position: Point, depth: Depth, color: RGBA) extends EntityNode:
  def size: Size        = Size(32, 32)
  def flip: Flip        = Flip.default
  def ref: Point        = Point.zero
  def rotation: Radians = Radians.zero
  def scale: Vector2    = Vector2.one

  def withDepth(newDepth: Depth): MyColoredEntity =
    this.copy(depth = newDepth)

  def toShaderData: ShaderData =
    ShaderData(
      MyColoredEntity.shader.id,
      UniformBlock(
        "MyCustomData",
        List(Uniform("MY_COLOR") -> vec4(color.r, color.g, color.b, color.a))
      )
    )

object MyColoredEntity:
  val shader: EntityShader =
    EntityShader
      .Source(ShaderId("my-colored-shader"))
      .withFragmentProgram(
        """
        |layout (std140) uniform MyCustomData {
        |  vec4 MY_COLOR;
        |};
        |
        |void fragment() {
        |  COLOR = MY_COLOR;
        |}
        |""".stripMargin
      )
```

The `toShaderData` function now includes a uniform block that declares (in this case) a single variable called "MY_COLOR" of type `vec4`.

> Aside, the variable names are not checked! It's the order that matters, but keep the names the same for your own sanity...

We then declare a uniform block (a special kind of struct) in the shader code, and then we can use the variable in your fragment function.

Please note that variables sent over as uniform blocks are subject to data packing rules! See below!

## `UniformBlock` / UBO Data Packing Rules

Take heed! These rules have saved me many times and originally came from here: [https://youtu.be/bdIZ2ZloXEA?t=113](https://youtu.be/bdIZ2ZloXEA?t=113)

```
"UBO - Uniform Buffer Object"

Uses a struct as a way to defined the data in the buffer.
Struct data based on STD140 layout requires data to exist in 16 byte chunks.

Float, Int and Bools are treated as 4 Bytes of Data.

Arrays, no matter the type, each element is 16 Bytes.

vec2, Contains 2 floats so 4*2 bytes of data (8 Bytes)
vec4, Contains 4 floats so 4*4 bytes of data (16 Bytes)
vec3, Must be treated as 16 bytes of data (i.e. a vec4), last 4 bytes are buffer space

mat3, Contains 3 sets of Vec3 BUT each vec3 is treated as vec4, 3*16 Bytes of data
mat4, Contains 4 sets of Vec4, so 4 * 16 Bytes

For EXAMPLE
Float - Float - Vec3 - Float

FF00 VVVV F000

You also can't straddle byte boundries. So if you're trying to pack this: Float-Vec2
This is valid: F0VV
But this isn't: FVV0
```
