package indigoextras.geometry

import indigo.shared.datatypes.Vector2

final case class BoundingCircle(position: Vertex, radius: Double) derives CanEqual:
  lazy val x: Double        = position.x
  lazy val y: Double        = position.y
  lazy val diameter: Double = radius * 2

  lazy val left: Double   = x - radius
  lazy val right: Double  = x + radius
  lazy val top: Double    = y - radius
  lazy val bottom: Double = y + radius

  def toBoundingBox: BoundingBox =
    BoundingBox(Vertex(left, top), Vertex(diameter, diameter))

  def contains(vertex: Vertex): Boolean =
    vertex.distanceTo(position) <= radius
  def contains(x: Double, y: Double): Boolean =
    contains(Vertex(x, y))
  def contains(vector: Vector2): Boolean =
    contains(Vertex.fromVector(vector))

  def +(d: Double): BoundingCircle = resize(radius + d)
  def -(d: Double): BoundingCircle = resize(radius - d)
  def *(d: Double): BoundingCircle = resize(radius * d)
  def /(d: Double): BoundingCircle = resize(radius / d)

  def sdf(vertex: Vertex): Double =
    BoundingCircle.signedDistanceFunction(vertex - position, radius)
  def sdf(vector: Vector2): Double =
    sdf(Vertex.fromVector(vector))

  def distanceToBoundary(vertex: Vertex): Double =
    sdf(vertex)
  def distanceToBoundary(vector: Vector2): Double =
    sdf(Vertex.fromVector(vector))

  def expandToInclude(other: BoundingCircle): BoundingCircle =
    BoundingCircle.expandToInclude(this, other)

  def encompasses(other: BoundingCircle): Boolean =
    BoundingCircle.encompassing(this, other)

  def overlaps(other: BoundingCircle): Boolean =
    BoundingCircle.overlapping(this, other)

  def moveBy(amount: Vertex): BoundingCircle =
    this.copy(position = position + amount)
  def moveBy(x: Double, y: Double): BoundingCircle =
    moveBy(Vertex(x, y))
  def moveBy(amount: Vector2): BoundingCircle =
    moveBy(Vertex.fromVector(amount))

  def moveTo(newPosition: Vertex): BoundingCircle =
    this.copy(position = newPosition)
  def moveTo(x: Double, y: Double): BoundingCircle =
    moveTo(Vertex(x, y))
  def moveTo(newPosition: Vector2): BoundingCircle =
    moveTo(Vertex.fromVector(newPosition))

  def resize(newRadius: Double): BoundingCircle =
    this.copy(radius = newRadius)

  def lineIntersects(line: LineSegment): Boolean =
    BoundingCircle.lineIntersects(this, line)

  def lineIntersectsAt(line: LineSegment): BoundingCircleLineIntersect =
    BoundingCircle.lineIntersectsAt(this, line)

object BoundingCircle:

  val zero: BoundingCircle =
    BoundingCircle(Vertex.zero, 0)

  def apply(x: Double, y: Double, radius: Double): BoundingCircle =
    BoundingCircle(Vertex(x, y), radius)

  def fromTwoVertices(center: Vertex, boundary: Vertex): BoundingCircle =
    BoundingCircle(center, center.distanceTo(boundary))

  def fromVertices(vertices: List[Vertex]): BoundingCircle =
    val bb = BoundingBox.fromVertices(vertices)
    BoundingCircle(bb.center, bb.center.distanceTo(bb.topLeft))

  def fromVertexCloud(vertices: List[Vertex]): BoundingCircle =
    fromVertices(vertices)

  def fromBoundingBox(boundingBox: BoundingBox): BoundingCircle =
    BoundingCircle(boundingBox.center, Math.max(boundingBox.halfSize.x, boundingBox.halfSize.y))

  def expandToInclude(a: BoundingCircle, b: BoundingCircle): BoundingCircle =
    a.resize(a.position.distanceTo(b.position) + Math.abs(b.radius))

  def encompassing(a: BoundingCircle, b: BoundingCircle): Boolean =
    a.position.distanceTo(b.position) <= Math.abs(a.radius) - Math.abs(b.radius)

  def overlapping(a: BoundingCircle, b: BoundingCircle): Boolean =
    a.position.distanceTo(b.position) < Math.abs(a.radius) + Math.abs(b.radius)

  def lineIntersects(boundingCircle: BoundingCircle, line: LineSegment): Boolean =
    Math.abs(line.sdf(boundingCircle.position)) <= boundingCircle.radius

  def lineIntersectsAt(boundingCircle: BoundingCircle, line: LineSegment): BoundingCircleLineIntersect =
    val aX = line.start.x
    val aY = line.start.y
    val bX = line.end.x
    val bY = line.end.y
    val dX = bX - aX
    val dY = bY - aY

    if dX == 0 && dY == 0 then BoundingCircleLineIntersect.Zero
    else
      val cX       = boundingCircle.position.x
      val cY       = boundingCircle.position.y
      val dl       = dX * dX + dY * dY
      val t        = ((cX - aX) * dX + (cY - aY) * dY) / dl
      val nearestX = aX + t * dX;
      val nearestY = aY + t * dY;
      val dist     = Vector2.distance(Vector2(nearestX, nearestY), Vector2(cX, cY))
      val r        = boundingCircle.radius

      if dist == r then
        val iX = nearestX;
        val iY = nearestY;

        if t < 0 || t > 1 then BoundingCircleLineIntersect.Zero
        else BoundingCircleLineIntersect.One(Vertex(iX, iY))
      else if dist < r then
        val dt  = Math.sqrt(r * r - dist * dist) / Math.sqrt(dl);
        val t1  = t - dt
        val i1X = aX + t1 * dX
        val i1Y = aY + t1 * dY

        val near =
          if t1 < 0 || t1 > 1 then BoundingCircleLineIntersect.Zero
          else BoundingCircleLineIntersect.One(Vertex(i1X, i1Y))

        val t2  = t + dt
        val i2X = aX + t2 * dX
        val i2Y = aY + t2 * dY

        val far =
          if t2 < 0 || t2 > 1 then BoundingCircleLineIntersect.Zero
          else BoundingCircleLineIntersect.One(Vertex(i2X, i2Y))

        near |+| far
      else BoundingCircleLineIntersect.Zero

  // Centered at the origin
  def signedDistanceFunction(point: Vertex, radius: Double): Double =
    point.length - radius

sealed trait BoundingCircleLineIntersect derives CanEqual:
  def nearest: Option[Vertex] =
    this match
      case BoundingCircleLineIntersect.Zero         => None
      case BoundingCircleLineIntersect.One(at)      => Some(at)
      case BoundingCircleLineIntersect.Two(near, _) => Some(near)

  def furthest: Option[Vertex] =
    this match
      case BoundingCircleLineIntersect.Zero        => None
      case BoundingCircleLineIntersect.One(at)     => Some(at)
      case BoundingCircleLineIntersect.Two(_, far) => Some(far)

  def toOption: Option[List[Vertex]] =
    this match
      case BoundingCircleLineIntersect.Zero           => None
      case BoundingCircleLineIntersect.One(at)        => Some(List(at))
      case BoundingCircleLineIntersect.Two(near, far) => Some(List(near, far))

  def |+|(other: BoundingCircleLineIntersect): BoundingCircleLineIntersect

object BoundingCircleLineIntersect:
  case object Zero extends BoundingCircleLineIntersect:
    def |+|(other: BoundingCircleLineIntersect): BoundingCircleLineIntersect =
      other

  final case class One(at: Vertex) extends BoundingCircleLineIntersect:
    def |+|(other: BoundingCircleLineIntersect): BoundingCircleLineIntersect =
      other match {
        case Zero         => this
        case One(otherAt) => Two(at, otherAt)
        case _: Two       => other
      }

  final case class Two(near: Vertex, far: Vertex) extends BoundingCircleLineIntersect:
    def |+|(other: BoundingCircleLineIntersect): BoundingCircleLineIntersect =
      this
