package indigoextras.performers

import indigo.physics.Collider
import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneNode

/** Performers represent entities in your scene. Indigo uses performance metaphors (like Lead and Extra) to represent
  * different levels of performer complexity. It’s meant to be fun, but also to suggest how much responsibility each
  * type takes on in the simulation. From Leads who take the starring roles, to silent Extras filling the background, to
  * Narrators who influence events without being seen — each has a role to play.
  */
sealed trait Performer[ReferenceData]:

  /** A unique identifier for the performer.
    */
  def id: PerformerId

  /** Provide the depth / order of the performer
    */
  def depth: PerformerDepth

  /** A flag signifying whether the performer is a type of stunt performer or not.
    */
  def hasCollider: Boolean

object Performer:

  /** Extra performers are the background characters of the performance. They are responsible for rendering themselves
    * and updating their state, but they cannot interact with the game directly since they have no way to listen to or
    * emit events.
    */
  trait Extra[ReferenceData] extends Performer[ReferenceData]:

    /** Update the performer
      */
    def update(context: PerformerContext[ReferenceData]): Performer.Extra[ReferenceData]

    /** Draw the performer
      */
    def present(context: PerformerContext[ReferenceData]): Batch[SceneNode]

    val hasCollider: Boolean = false

  /** Stunt performers are like Extras, but they can do their own stunts! In practical terms, they are background
    * performers like extras, but have their motion controlled by a physics simulation.
    */
  trait Stunt[ReferenceData] extends Performer[ReferenceData]:

    /** The physics collider for the performer, used for collision detection.
      */
    def initialCollider: Collider[PerformerId]

    /** Update the performer
      */
    def update(context: PerformerContext[ReferenceData]): Performer.Stunt[ReferenceData]

    /** Update the physics collider for the performer
      */
    def updateCollider(context: PerformerContext[ReferenceData], collider: Collider[PerformerId]): Collider[PerformerId]

    /** Draw the performer
      */
    def present(context: PerformerContext[ReferenceData], collider: Collider[PerformerId]): Batch[SceneNode]

    val hasCollider: Boolean = true

  /** Support performers are the main character actors. They are responsible for rendering themselves, updating their
    * state, and can also listen to and emit events, but they leave physical work to Stunt and Lead performers.
    */
  trait Support[ReferenceData] extends Performer[ReferenceData]:

    /** Update the performer
      */
    def update(context: PerformerContext[ReferenceData]): GlobalEvent => Outcome[Performer.Support[ReferenceData]]

    /** Draw the performer
      */
    def present(context: PerformerContext[ReferenceData]): Outcome[Batch[SceneNode]]

    val hasCollider: Boolean = false

  /** Lead performers are the stars of the show. They are responsible for rendering themselves, updating their state,
    * listen to and emitting events, and even doing their own stunts! They are the most complex type of performer, but
    * also the most powerful.
    */
  trait Lead[ReferenceData] extends Performer[ReferenceData]:

    /** The physics collider for the performer, used for collision detection.
      */
    def initialCollider: Collider[PerformerId]

    /** Update the performer
      */
    def update(context: PerformerContext[ReferenceData]): GlobalEvent => Outcome[Performer.Lead[ReferenceData]]

    /** Update the physics collider for the performer
      */
    def updateCollider(context: PerformerContext[ReferenceData], collider: Collider[PerformerId]): Collider[PerformerId]

    /** Draw the performer
      */
    def present(context: PerformerContext[ReferenceData], collider: Collider[PerformerId]): Outcome[Batch[SceneNode]]

    val hasCollider: Boolean = true
