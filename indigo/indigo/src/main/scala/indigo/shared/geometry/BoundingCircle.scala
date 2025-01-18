package indigo.shared.geometry

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Circle
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Vector2

final case class BoundingCircle(position: Vertex, radius: Double) derives CanEqual:
  lazy val x: Double        = position.x
  lazy val y: Double        = position.y
  lazy val diameter: Double = radius * 2

  lazy val left: Double   = x - radius
  lazy val right: Double  = x + radius
  lazy val top: Double    = y - radius
  lazy val bottom: Double = y + radius

  lazy val center: Vertex = position

  def contains(vertex: Vertex): Boolean =
    vertex.distanceTo(position) <= radius
  def contains(x: Double, y: Double): Boolean =
    contains(Vertex(x, y))
  def contains(vector: Vector2): Boolean =
    contains(Vertex.fromVector2(vector))

  def +(d: Double): BoundingCircle = resize(radius + d)
  def -(d: Double): BoundingCircle = resize(radius - d)
  def *(d: Double): BoundingCircle = resize(radius * d)
  def /(d: Double): BoundingCircle = resize(radius / d)

  def sdf(vertex: Vertex): Double =
    BoundingCircle.signedDistanceFunction(vertex - position, radius)
  def sdf(vector: Vector2): Double =
    sdf(Vertex.fromVector2(vector))

  def distanceToBoundary(vertex: Vertex): Double =
    sdf(vertex)
  def distanceToBoundary(vector: Vector2): Double =
    sdf(Vertex.fromVector2(vector))

  def expandToInclude(other: BoundingCircle): BoundingCircle =
    BoundingCircle.expandToInclude(this, other)

  def encompasses(other: BoundingCircle): Boolean =
    BoundingCircle.encompassing(this, other)
  def encompasses(other: BoundingBox): Boolean =
    BoundingCircle.encompassing(this, other)

  def overlaps(other: BoundingCircle): Boolean =
    BoundingCircle.overlapping(this, other)
  def overlaps(other: BoundingBox): Boolean =
    BoundingCircle.overlapping(this, other)
  def overlaps(other: LineSegment): Boolean =
    contains(other.start) || contains(other.end) || lineIntersects(other)

  def moveBy(amount: Vertex): BoundingCircle =
    this.copy(position = position + amount)
  def moveBy(x: Double, y: Double): BoundingCircle =
    moveBy(Vertex(x, y))
  def moveBy(amount: Vector2): BoundingCircle =
    moveBy(Vertex.fromVector2(amount))

  def moveTo(newPosition: Vertex): BoundingCircle =
    this.copy(position = newPosition)
  def moveTo(x: Double, y: Double): BoundingCircle =
    moveTo(Vertex(x, y))
  def moveTo(newPosition: Vector2): BoundingCircle =
    moveTo(Vertex.fromVector2(newPosition))

  def resize(newRadius: Double): BoundingCircle =
    this.copy(radius = newRadius)
  def resizeTo(newRadius: Double): BoundingCircle =
    resize(newRadius)
  def resizeBy(amount: Double): BoundingCircle =
    expand(amount)
  def withRadius(newRadius: Double): BoundingCircle =
    resize(newRadius)
  def expand(by: Double): BoundingCircle =
    resize(radius + by)
  def contract(by: Double): BoundingCircle =
    resize(radius - by)

  def toCircle: Circle =
    Circle(position.toPoint, radius.toInt)

  @deprecated("Please use `toIncircleRectangle`, or alternatively `toCircumcircleRectangle`.")
  def toRectangle: Rectangle =
    Rectangle.fromIncircle(this.toCircle)
  def toIncircleRectangle: Rectangle =
    Rectangle.fromIncircle(this.toCircle)
  def toCircumcircleRectangle: Rectangle =
    Rectangle.fromCircumcircle(this.toCircle)

  @deprecated("Please use `toIncircleBoundingBox`, or alternatively `toCircumcircleBoundingBox`.")
  def toBoundingBox: BoundingBox =
    BoundingBox.fromIncircle(this)
  def toIncircleBoundingBox: BoundingBox =
    BoundingBox.fromIncircle(this)
  def toCircumcircleBoundingBox: BoundingBox =
    BoundingBox.fromCircumcircle(this)

  def lineIntersects(line: LineSegment): Boolean =
    BoundingCircle.lineIntersects(this, line)

  def lineIntersectsAt(line: LineSegment): BoundingCircleLineIntersect =
    BoundingCircle.lineIntersectsAt(this, line)

  /** Reflects the incoming 'ray' off of the BoundingCircle.
    */
  def reflect(ray: LineSegment): Option[ReflectionData] =
    lineIntersectsAt(ray).nearest.map { at =>
      val nrml      = (at - center).toVector2.normalise
      val incident  = (at - ray.start).toVector2.normalise
      val reflected = (incident - nrml * (2.0 * incident.dot(nrml))).normalise

      ReflectionData(
        at,
        nrml,
        incident,
        reflected
      )
    }

  def ~==(other: BoundingCircle): Boolean =
    (position ~== other.position) && Math.abs(radius - other.radius) < 0.0001

object BoundingCircle:

  val zero: BoundingCircle =
    BoundingCircle(Vertex.zero, 0)

  def apply(x: Double, y: Double, radius: Double): BoundingCircle =
    BoundingCircle(Vertex(x, y), radius)

  /** Creates a `BoundingCircle` from two vertices where the first represents the center of the circle and the second is
    * used to calculate the radius by measuring the distance to the center.
    */
  def fromTwoVertices(center: Vertex, boundary: Vertex): BoundingCircle =
    BoundingCircle(center, center.distanceTo(boundary))

  /** Creates a `BoundingCircle` from three vertices such that all of the vertices lie on the circles circumference.
    */
  def fromThreeVertices(a: Vertex, b: Vertex, c: Vertex): Option[BoundingCircle] =
    // Sides
    val sideA = a.distanceTo(b)
    val sideB = b.distanceTo(c)
    val sideC = c.distanceTo(a)

    // Find the three angles from the sides using the law of cosine
    val angleA = Math.acos((Math.pow(sideB, 2) + Math.pow(sideC, 2) - Math.pow(sideA, 2)) / (2 * sideB * sideC))
    val angleB = Math.acos((Math.pow(sideC, 2) + Math.pow(sideA, 2) - Math.pow(sideB, 2)) / (2 * sideC * sideA))
    val angleC = Math.acos((Math.pow(sideA, 2) + Math.pow(sideB, 2) - Math.pow(sideC, 2)) / (2 * sideA * sideB))

    // Then find the widest angle, the point there connects to the other two
    List(angleA -> c, angleB -> a, angleC -> b).sortBy(_._1).map(_._2) match
      case vtxC :: vtxB :: vtxA :: Nil =>
        // To form two `LineSegments`
        val lsA = LineSegment(vtxA, vtxB)
        val lsB = LineSegment(vtxA, vtxC)

        // We then take a normal from the center of the line segment
        val lineA = LineSegment(lsA.center, lsA.center + lsA.normal).toLine
        val lineB = LineSegment(lsB.center, lsB.center + lsB.normal).toLine

        // Where the two normal `Line`'s meet is our circle center
        lineA.intersectsAt(lineB).map { center =>
          BoundingCircle(center, center.distanceTo(vtxA))
        }

      case _ =>
        None

  /** Creates a `BoundingCircle` from three vertices such that all of the vertices lie on the circles circumference.
    */
  def circumcircle(a: Vertex, b: Vertex, c: Vertex): Option[BoundingCircle] =
    fromThreeVertices(a, b, c)

  /** Creates a `BoundingCircle` that contains all of the points provided.
    */
  def fromVertices(vertices: Batch[Vertex]): BoundingCircle =
    val bb = BoundingBox.fromVertices(vertices)
    BoundingCircle(bb.center, bb.center.distanceTo(bb.topLeft))

  @deprecated("Please use `fromVertices` which is functionally the same as `fromVertexCloud`.")
  def fromVertexCloud(vertices: Batch[Vertex]): BoundingCircle =
    fromVertices(vertices)

  def fromCircle(circle: Circle): BoundingCircle =
    BoundingCircle(
      Vertex.fromPoint(circle.position),
      circle.radius.toDouble
    )

  @deprecated("Please use `BoundingCircle.incircle`, or alternatively `BoundingCircle.circumcircle`.")
  def fromBoundingBox(boundingBox: BoundingBox): BoundingCircle =
    incircle(boundingBox)

  /** Creates a `Circle` from a square (BoundingBox's are squared off by the max width/height) where the circle fits
    * inside the square.
    */
  def incircle(bounds: BoundingBox): BoundingCircle =
    BoundingCircle(bounds.center, Math.max(bounds.halfSize.x, bounds.halfSize.y))

  /** Creates a `BoundingCircle` from a square (BoundingBox's are squared off by the max width/height) such that all of
    * the corners lie on the circle's circumference.
    */
  def circumcircle(bounds: BoundingBox): BoundingCircle =
    val b = bounds.toSquare
    BoundingCircle(b.center, b.center.distanceTo(b.topLeft).toInt)

  def expandToInclude(a: BoundingCircle, b: BoundingCircle): BoundingCircle =
    a.resize(a.position.distanceTo(b.position) + Math.abs(b.radius))

  def encompassing(a: BoundingCircle, b: BoundingCircle): Boolean =
    a.position.distanceTo(b.position) <= Math.abs(a.radius) - Math.abs(b.radius)
  def encompassing(a: BoundingCircle, b: BoundingBox): Boolean =
    b.corners.forall(a.contains)

  def overlapping(a: BoundingCircle, b: BoundingCircle): Boolean =
    a.position.distanceTo(b.position) < Math.abs(a.radius) + Math.abs(b.radius)

  def overlapping(a: BoundingCircle, b: BoundingBox): Boolean =
    b.contains(a.position) || Math.abs(b.sdf(a.position)) <= Math.abs(a.radius)

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
