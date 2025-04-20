// package indigoextras.actors

// import indigo.shared.Outcome
// import indigo.shared.collections.Batch
// import indigo.shared.events.GlobalEvent
// import indigo.shared.scenegraph.Layer
// import indigo.shared.scenegraph.LayerKey
// import indigo.shared.scenegraph.SceneNode
// import indigo.shared.scenegraph.SceneUpdateFragment
// import indigo.shared.subsystems.SubSystem
// import indigo.shared.subsystems.SubSystemContext
// import indigo.shared.subsystems.SubSystemId

// final case class ActorSystem[GameModel, RefData, A](
//     id: SubSystemId,
//     layerKey: Option[LayerKey],
//     extractReference: GameModel => RefData,
//     _initialActors: Batch[ActorInstance[RefData, A]],
//     _updateActorPool: ActorPool[RefData, A] => PartialFunction[GlobalEvent, Outcome[ActorPool[RefData, A]]]
// ) extends SubSystem[GameModel]:

//   type EventType      = GlobalEvent
//   type SubSystemModel = ActorPool[RefData, A]
//   type ReferenceData  = RefData

//   def eventFilter: GlobalEvent => Option[GlobalEvent] =
//     e => Some(e)

//   def reference(model: GameModel): ReferenceData =
//     extractReference(model)

//   def initialModel: Outcome[SubSystemModel] =
//     Outcome(ActorPool(_initialActors))

//   def update(
//       context: SubSystemContext[ReferenceData],
//       model: ActorPool[ReferenceData, A]
//   ): GlobalEvent => Outcome[ActorPool[ReferenceData, A]] =

//     _updateActorPool(model)
//       .orElse { case e =>
//         model.pool
//           .map { ai =>
//             val ctx =
//               ActorContext(context)
//                 .withReference(ai.actor.reference(context.reference))

//             val instance =
//               ai.actor.updateModel(ctx, ai.instance)(e).map { updated =>
//                 ai.copy(instance = updated)
//               }

//             (
//               instance,
//               ai.actor.depth(ctx, ai.instance)
//             )
//           }
//           .sortBy(_._2)
//           .map(_._1)
//           .sequence
//           .map(as => model.copy(pool = as))
//       }

//   def present(
//       context: SubSystemContext[ReferenceData],
//       model: ActorPool[ReferenceData, A]
//   ): Outcome[SceneUpdateFragment] =
//     val nodes: Outcome[Batch[SceneNode]] =
//       model.pool
//         .map { ai =>
//           val ctx =
//             ActorContext(context)
//               .withReference(ai.actor.reference(context.reference))

//           ai.actor.present(ctx, ai.instance)
//         }
//         .sequence
//         .map(_.flatten)

//     layerKey match
//       case None =>
//         nodes.map { ns =>
//           SceneUpdateFragment(
//             Layer.Content(ns)
//           )
//         }

//       case Some(key) =>
//         nodes.map { ns =>
//           SceneUpdateFragment(
//             key -> Layer.Content(ns)
//           )
//         }

//   def withId(id: SubSystemId): ActorSystem[GameModel, ReferenceData, A] =
//     this.copy(id = id)

//   def withLayerKey(key: LayerKey): ActorSystem[GameModel, ReferenceData, A] =
//     this.copy(layerKey = Some(key))
//   def clearLayerKey: ActorSystem[GameModel, ReferenceData, A] =
//     this.copy(layerKey = None)

//   def spawn[B <: A](actor: B)(using a: Actor[ReferenceData, B]): ActorSystem[GameModel, ReferenceData, A] =
//     this.copy(_initialActors = _initialActors :+ ActorInstance(actor, a.asInstanceOf[Actor[ReferenceData, A]]))

//   def updateActors(
//       f: ActorPool[ReferenceData, A] => PartialFunction[GlobalEvent, Outcome[ActorPool[ReferenceData, A]]]
//   ): ActorSystem[GameModel, ReferenceData, A] =
//     this.copy(_updateActorPool = f)

// object ActorSystem:

//   private def noUpdate[ReferenceData, A]
//       : ActorPool[ReferenceData, A] => PartialFunction[GlobalEvent, Outcome[ActorPool[ReferenceData, A]]] =
//     _ => PartialFunction.empty

//   def apply[GameModel, A](
//       id: SubSystemId
//   ): ActorSystem[GameModel, GameModel, A] =
//     ActorSystem(id, None, identity, Batch.empty, noUpdate[GameModel, A])

//   def apply[GameModel, A](
//       id: SubSystemId,
//       layerKey: LayerKey
//   ): ActorSystem[GameModel, GameModel, A] =
//     ActorSystem(id, Some(layerKey), identity, Batch.empty, noUpdate[GameModel, A])

//   def apply[GameModel, ReferenceData, A](
//       id: SubSystemId,
//       layerKey: LayerKey,
//       extractReference: GameModel => ReferenceData
//   ): ActorSystem[GameModel, ReferenceData, A] =
//     ActorSystem(id, Some(layerKey), extractReference, Batch.empty, noUpdate[ReferenceData, A])

// final case class ActorInstance[ReferenceData, A](instance: A, actor: Actor[ReferenceData, A])
