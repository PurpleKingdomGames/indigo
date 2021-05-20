package indigo.shared.scenegraph

import indigo.shared.scenegraph.syntax.BasicSpatial

import indigo.shared.datatypes._

/** A single cloned instance of a cloneblank
  *
  * @param id
  *   @param depth
  * @param transform
  */
final case class Clone(id: CloneId, depth: Depth, transform: CloneTransformData) extends DependentNode
    derives CanEqual {
  lazy val x: Int                  = transform.position.x
  lazy val y: Int                  = transform.position.y
  lazy val rotation: Radians       = transform.rotation
  lazy val scale: Vector2          = transform.scale
  lazy val flipHorizontal: Boolean = transform.flipHorizontal
  lazy val flipVertical: Boolean   = transform.flipVertical
  lazy val ref: Point              = Point.zero

  def position: Point = Point(transform.position.x, transform.position.y)
  def flip: Flip      = Flip(transform.flipHorizontal, transform.flipVertical)

  def withCloneId(newCloneId: CloneId): Clone =
    this.copy(id = newCloneId)

  def withDepth(newDepth: Depth): Clone =
    this.copy(depth = newDepth)

  def withTransforms(
      newPosition: Point,
      newRotation: Radians,
      newScale: Vector2,
      flipHorizontal: Boolean,
      flipVertical: Boolean
  ): Clone =
    this.copy(transform = CloneTransformData(newPosition, newRotation, newScale, flipHorizontal, flipVertical))

  def withHorizontalFlip(isFlipped: Boolean): Clone =
    this.copy(transform = transform.withHorizontalFlip(isFlipped))

  def withVerticalFlip(isFlipped: Boolean): Clone =
    this.copy(transform = transform.withVerticalFlip(isFlipped))
}

object Clone:
  def apply(id: CloneId): Clone =
    Clone(id, Depth(1), CloneTransformData.identity)

  given BasicSpatial[Clone] with
    extension (clone: Clone)
      def position: Point =
        clone.position
      def rotation: Radians =
        clone.rotation
      def scale: Vector2 =
        clone.scale
      def depth: Depth =
        clone.depth
      def ref: Point =
        clone.ref
      def flip: Flip =
        clone.flip

      def withPosition(newPosition: Point): Clone =
        clone.copy(transform = clone.transform.withPosition(newPosition))
      def withRotation(newRotation: Radians): Clone =
        clone.copy(transform = clone.transform.withRotation(newRotation))
      def withScale(newScale: Vector2): Clone =
        clone.copy(transform = clone.transform.withScale(newScale))
      def withRef(newRef: Point): Clone =
        clone
      def withRef(x: Int, y: Int): Clone =
        withRef(Point(x, y))
      def withDepth(newDepth: Depth): Clone =
        clone.copy(depth = newDepth)
      def withFlip(newFlip: Flip): Clone =
        clone.copy(
          transform = clone.transform
            .withVerticalFlip(newFlip.vertical)
            .withHorizontalFlip(newFlip.horizontal)
        )

/** Represents many clones of the same cloneblank, differentiated only by their transform data.
  *
  * @param id
  *   @param depth
  * @param transform
  *   @param clones
  * @param staticBatchKey
  */
final case class CloneBatch(
    id: CloneId,
    depth: Depth,
    transform: CloneTransformData,
    clones: List[CloneTransformData],
    staticBatchKey: Option[BindingKey]
) extends DependentNode derives CanEqual {
  lazy val x: Int                  = transform.position.x
  lazy val y: Int                  = transform.position.y
  lazy val rotation: Radians       = transform.rotation
  lazy val scale: Vector2          = transform.scale
  lazy val flipHorizontal: Boolean = transform.flipHorizontal
  lazy val flipVertical: Boolean   = transform.flipVertical
  lazy val ref: Point              = Point.zero

  def position: Point = Point(transform.position.x, transform.position.y)
  def flip: Flip      = Flip(transform.flipHorizontal, transform.flipVertical)

  def withCloneId(newCloneId: CloneId): CloneBatch =
    this.copy(id = newCloneId)

  def withDepth(newDepth: Depth): CloneBatch =
    this.copy(depth = newDepth)

  def withTransforms(
      newPosition: Point,
      newRotation: Radians,
      newScale: Vector2,
      flipHorizontal: Boolean,
      flipVertical: Boolean
  ): CloneBatch =
    this.copy(transform = CloneTransformData(newPosition, newRotation, newScale, flipHorizontal, flipVertical))

  def withClones(newClones: List[CloneTransformData]): CloneBatch =
    this.copy(clones = newClones)

  def addClones(additionalClones: List[CloneTransformData]): CloneBatch =
    this.copy(clones = clones ++ additionalClones)

  def withMaybeStaticBatchKey(maybeKey: Option[BindingKey]): CloneBatch =
    this.copy(staticBatchKey = maybeKey)

  def withStaticBatchKey(key: BindingKey): CloneBatch =
    withMaybeStaticBatchKey(Option(key))

  def clearStaticBatchKey: CloneBatch =
    withMaybeStaticBatchKey(None)
}

object CloneBatch:

  given BasicSpatial[CloneBatch] with
    extension (cloneBatch: CloneBatch)
      def position: Point =
        cloneBatch.position
      def rotation: Radians =
        cloneBatch.rotation
      def scale: Vector2 =
        cloneBatch.scale
      def depth: Depth =
        cloneBatch.depth
      def ref: Point =
        cloneBatch.ref
      def flip: Flip =
        cloneBatch.flip

      def withPosition(newPosition: Point): CloneBatch =
        cloneBatch.copy(transform = cloneBatch.transform.withPosition(newPosition))
      def withRotation(newRotation: Radians): CloneBatch =
        cloneBatch.copy(transform = cloneBatch.transform.withRotation(newRotation))
      def withScale(newScale: Vector2): CloneBatch =
        cloneBatch.copy(transform = cloneBatch.transform.withScale(newScale))
      def withRef(newRef: Point): CloneBatch =
        cloneBatch
      def withRef(x: Int, y: Int): CloneBatch =
        withRef(Point(x, y))
      def withDepth(newDepth: Depth): CloneBatch =
        cloneBatch.copy(depth = newDepth)
      def withFlip(newFlip: Flip): CloneBatch =
        cloneBatch.copy(
          transform = cloneBatch.transform
            .withVerticalFlip(newFlip.vertical)
            .withHorizontalFlip(newFlip.horizontal)
        )
