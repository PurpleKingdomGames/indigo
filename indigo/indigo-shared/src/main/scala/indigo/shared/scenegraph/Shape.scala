package indigo.shared.scenegraph

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Depth
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Flip
import indigo.shared.datatypes.Rectangle
import indigo.shared.materials.ShaderData
import indigo.shared.shader.Uniform
import indigo.shared.shader.ShaderPrimitive._
import indigo.shared.datatypes.RGBA
import indigo.shared.shader.StandardShaders

sealed trait Shape extends EntityNode with Cloneable with SpatialModifiers[Shape] {
  def moveTo(pt: Point): Shape
  def moveTo(x: Int, y: Int): Shape
  def withPosition(newPosition: Point): Shape

  def moveBy(pt: Point): Shape
  def moveBy(x: Int, y: Int): Shape

  def rotateTo(angle: Radians): Shape
  def rotateBy(angle: Radians): Shape
  def withRotation(newRotation: Radians): Shape

  def scaleBy(amount: Vector2): Shape
  def scaleBy(x: Double, y: Double): Shape
  def withScale(newScale: Vector2): Shape

  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Shape
  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Shape

  def withDepth(newDepth: Depth): Shape

  def flipHorizontal(isFlipped: Boolean): Shape
  def flipVertical(isFlipped: Boolean): Shape
  def withFlip(newFlip: Flip): Shape
}

object Shape {

  final case class Box(
      dimensions: Rectangle,
      fill: RGBA,
      strokeColor: RGBA,
      strokeWidth: Int,
      rotation: Radians,
      scale: Vector2,
      depth: Depth,
      ref: Point,
      flip: Flip
  ) extends Shape {

    private lazy val square: Int =
      Math.max(dimensions.size.x, dimensions.size.y)

    lazy val position: Point =
      dimensions.position - (Point(square) / 2) - (strokeWidth / 2)

    lazy val bounds: Rectangle =
      Rectangle(
        position,
        Point(square) + strokeWidth
      )

    def withDimensions(newDimensions: Rectangle): Box =
      this.copy(dimensions = newDimensions)

    def resize(size: Point): Box =
      this.copy(dimensions = dimensions.resize(size))

    def withFillColor(newFill: RGBA): Box =
      this.copy(fill = newFill)

    def withStrokeColor(newStrokeColor: RGBA): Box =
      this.copy(strokeColor = newStrokeColor)

    def withStrokeWidth(newWidth: Int): Box =
      this.copy(strokeWidth = newWidth)

    private lazy val aspect: Vector2 =
      if (bounds.size.x > bounds.size.y)
        Vector2(1.0, bounds.size.y.toDouble / bounds.size.x.toDouble)
      else
        Vector2(bounds.size.x.toDouble / bounds.size.y.toDouble, 1.0)

    def moveTo(pt: Point): Box =
      this.copy(dimensions = dimensions.moveTo(pt))
    def moveTo(x: Int, y: Int): Box =
      moveTo(Point(x, y))
    def withPosition(newPosition: Point): Box =
      moveTo(newPosition)

    def moveBy(pt: Point): Box =
      this.copy(dimensions = dimensions.moveBy(pt))
    def moveBy(x: Int, y: Int): Box =
      moveBy(Point(x, y))

    def rotateTo(angle: Radians): Box =
      this.copy(rotation = angle)
    def rotateBy(angle: Radians): Box =
      rotateTo(rotation + angle)
    def withRotation(newRotation: Radians): Box =
      rotateTo(newRotation)

    def scaleBy(amount: Vector2): Box =
      this.copy(scale = scale * amount)
    def scaleBy(x: Double, y: Double): Box =
      scaleBy(Vector2(x, y))
    def withScale(newScale: Vector2): Box =
      this.copy(scale = newScale)

    def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Box =
      this.copy(dimensions = dimensions.moveTo(newPosition), rotation = newRotation, scale = newScale)

    def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Box =
      transformTo(position + positionDiff, rotation + rotationDiff, scale * scaleDiff)

    def withDepth(newDepth: Depth): Box =
      this.copy(depth = newDepth)

    def withRef(newRef: Point): Box =
      this.copy(ref = newRef)
    def withRef(x: Int, y: Int): Box =
      withRef(Point(x, y))

    def flipHorizontal(isFlipped: Boolean): Box =
      this.copy(flip = flip.withHorizontalFlip(isFlipped))
    def flipVertical(isFlipped: Boolean): Box =
      this.copy(flip = flip.withVerticalFlip(isFlipped))
    def withFlip(newFlip: Flip): Box =
      this.copy(flip = newFlip)

    def toShaderData: ShaderData =
      ShaderData(
        StandardShaders.ShapeBox.id,
        List(
          Uniform("ASPECT_RATIO") -> vec2(aspect.x, aspect.y),
          Uniform("STROKE_WIDTH") -> float(strokeWidth.toDouble),
          Uniform("STROKE_COLOR") -> vec4(strokeColor.r, strokeColor.g, strokeColor.b, strokeColor.a),
          Uniform("FILL_COLOR")   -> vec4(fill.r, fill.g, fill.b, fill.a)
        )
      )
  }
  object Box {

    def apply(dimensions: Rectangle, fill: RGBA): Box =
      Box(
        dimensions,
        fill,
        RGBA.Zero,
        0,
        Radians.zero,
        Vector2.one,
        Depth(1),
        Point.zero,
        Flip.default
      )

    def apply(dimensions: Rectangle, fill: RGBA, strokeColor: RGBA, strokeWidth: Int): Box =
      Box(
        dimensions,
        fill,
        strokeColor,
        strokeWidth,
        Radians.zero,
        Vector2.one,
        Depth(1),
        Point.zero,
        Flip.default
      )

  }

  final case class Circle(
      center: Point,
      radius: Int,
      fill: RGBA,
      strokeColor: RGBA,
      strokeWidth: Int,
      rotation: Radians,
      scale: Vector2,
      depth: Depth,
      ref: Point,
      flip: Flip
  ) extends Shape {

    lazy val position: Point =
      center - radius - (strokeWidth / 2)

    lazy val bounds: Rectangle =
      Rectangle(
        position,
        Point(radius * 2) + strokeWidth
      )

    def withFillColor(newFill: RGBA): Circle =
      this.copy(fill = newFill)

    def withStrokeColor(newStrokeColor: RGBA): Circle =
      this.copy(strokeColor = newStrokeColor)

    def withStrokeWidth(newWidth: Int): Circle =
      this.copy(strokeWidth = newWidth)

    def moveTo(pt: Point): Circle =
      this.copy(center = pt)
    def moveTo(x: Int, y: Int): Circle =
      moveTo(Point(x, y))
    def withPosition(newPosition: Point): Circle =
      moveTo(newPosition)

    def moveBy(pt: Point): Circle =
      this.copy(center = center + pt)
    def moveBy(x: Int, y: Int): Circle =
      moveBy(Point(x, y))

    def rotateTo(angle: Radians): Circle =
      this.copy(rotation = angle)
    def rotateBy(angle: Radians): Circle =
      rotateTo(rotation + angle)
    def withRotation(newRotation: Radians): Circle =
      rotateTo(newRotation)

    def scaleBy(amount: Vector2): Circle =
      this.copy(scale = scale * amount)
    def scaleBy(x: Double, y: Double): Circle =
      scaleBy(Vector2(x, y))
    def withScale(newScale: Vector2): Circle =
      this.copy(scale = newScale)

    def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Circle =
      this.copy(center = newPosition, rotation = newRotation, scale = newScale)

    def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Circle =
      transformTo(position + positionDiff, rotation + rotationDiff, scale * scaleDiff)

    def withDepth(newDepth: Depth): Circle =
      this.copy(depth = newDepth)

    def withRef(newRef: Point): Circle =
      this.copy(ref = newRef)
    def withRef(x: Int, y: Int): Circle =
      withRef(Point(x, y))

    def flipHorizontal(isFlipped: Boolean): Circle =
      this.copy(flip = flip.withHorizontalFlip(isFlipped))
    def flipVertical(isFlipped: Boolean): Circle =
      this.copy(flip = flip.withVerticalFlip(isFlipped))
    def withFlip(newFlip: Flip): Circle =
      this.copy(flip = newFlip)

    def toShaderData: ShaderData =
      ShaderData(
        StandardShaders.ShapeCircle.id,
        List(
          Uniform("STROKE_WIDTH") -> float(strokeWidth.toDouble),
          Uniform("STROKE_COLOR") -> vec4(strokeColor.r, strokeColor.g, strokeColor.b, strokeColor.a),
          Uniform("FILL_COLOR")   -> vec4(fill.r, fill.g, fill.b, fill.a)
        )
      )
  }
  object Circle {

    def apply(center: Point, radius: Int, fill: RGBA): Circle =
      Circle(
        center,
        radius,
        fill,
        RGBA.Zero,
        0,
        Radians.zero,
        Vector2.one,
        Depth(1),
        Point.zero,
        Flip.default
      )

    def apply(center: Point, radius: Int, fill: RGBA, strokeColor: RGBA, strokeWidth: Int): Circle =
      Circle(
        center,
        radius,
        fill,
        strokeColor,
        strokeWidth,
        Radians.zero,
        Vector2.one,
        Depth(1),
        Point.zero,
        Flip.default
      )

  }

  final case class Line(
      start: Point,
      end: Point,
      strokeColor: RGBA,
      strokeWidth: Int,
      rotation: Radians,
      scale: Vector2,
      depth: Depth,
      ref: Point,
      flip: Flip
  ) extends Shape {

    lazy val position: Point =
      Point(
        Math.min(start.x, end.x),
        Math.min(start.y, end.y)
      ) - (strokeWidth / 2)

    lazy val bounds: Rectangle = {
      val w = Math.max(start.x, end.x) - position.x
      val h = Math.max(start.y, end.y) - position.y

      Rectangle(position, Point(Math.max(w, h)) + strokeWidth)
    }

    def withStrokeColor(newStrokeColor: RGBA): Line =
      this.copy(strokeColor = newStrokeColor)

    def withStrokeWidth(newWidth: Int): Line =
      this.copy(strokeWidth = newWidth)

    def moveTo(pt: Point): Line =
      this.copy(start = pt)
    def moveTo(x: Int, y: Int): Line =
      moveTo(Point(x, y))
    def withPosition(newPosition: Point): Line =
      moveTo(newPosition)

    def moveBy(pt: Point): Line =
      this.copy(start = start + pt)
    def moveBy(x: Int, y: Int): Line =
      moveBy(Point(x, y))

    def rotateTo(angle: Radians): Line =
      this.copy(rotation = angle)
    def rotateBy(angle: Radians): Line =
      rotateTo(rotation + angle)
    def withRotation(newRotation: Radians): Line =
      rotateTo(newRotation)

    def scaleBy(amount: Vector2): Line =
      this.copy(scale = scale * amount)
    def scaleBy(x: Double, y: Double): Line =
      scaleBy(Vector2(x, y))
    def withScale(newScale: Vector2): Line =
      this.copy(scale = newScale)

    def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Line =
      this.copy(start = newPosition, rotation = newRotation, scale = newScale)

    def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Line =
      transformTo(position + positionDiff, rotation + rotationDiff, scale * scaleDiff)

    def withDepth(newDepth: Depth): Line =
      this.copy(depth = newDepth)

    def withRef(newRef: Point): Line =
      this.copy(ref = newRef)
    def withRef(x: Int, y: Int): Line =
      withRef(Point(x, y))

    def flipHorizontal(isFlipped: Boolean): Line =
      this.copy(flip = flip.withHorizontalFlip(isFlipped))
    def flipVertical(isFlipped: Boolean): Line =
      this.copy(flip = flip.withVerticalFlip(isFlipped))
    def withFlip(newFlip: Flip): Line =
      this.copy(flip = newFlip)

    def toShaderData: ShaderData = {
      val bounds: Rectangle =
        Rectangle.fromTwoPoints(start, end)

      // Relative to bounds
      val s = start - bounds.position + (strokeWidth / 2)
      val e = end - bounds.position + (strokeWidth / 2)

      ShaderData(
        StandardShaders.ShapeLine.id,
        List(
          Uniform("STROKE_WIDTH") -> float(strokeWidth.toDouble),
          Uniform("STROKE_COLOR") -> vec4(strokeColor.r, strokeColor.g, strokeColor.b, strokeColor.a),
          Uniform("START")        -> vec2(s.x.toDouble, s.y.toDouble),
          Uniform("END")          -> vec2(e.x.toDouble, e.y.toDouble)
        )
      )
    }
  }
  object Line {

    def apply(start: Point, end: Point, strokeWidth: Int): Line =
      Line(
        start,
        end,
        RGBA.Black,
        strokeWidth,
        Radians.zero,
        Vector2.one,
        Depth(1),
        Point.zero,
        Flip.default
      )

    def apply(start: Point, end: Point, strokeColor: RGBA, strokeWidth: Int): Line =
      Line(
        start,
        end,
        strokeColor,
        strokeWidth,
        Radians.zero,
        Vector2.one,
        Depth(1),
        Point.zero,
        Flip.default
      )

  }

}
