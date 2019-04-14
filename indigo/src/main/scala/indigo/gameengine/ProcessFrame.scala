// package indigo.gameengine

// import indigo.dice.Dice
// import indigo.time.GameTime
// import indigo.gameengine.events.{FrameInputEvents, GlobalEvent, Signals}
// import indigo.gameengine.scenegraph.SceneUpdateFragment
// import indigoexts.scenemanager.Scene

// object ProcessFrame {

//   def now[Model, ViewModel](scene: Scene): (Model, ViewModel) => (GameTime, List[GlobalEvent], Signals, Dice) => (Outcome[(Model, ViewModel)], SceneUpdateFragment) =
//     (model, viewModel) =>
//       (gameTime, globalEvents, signals, dice) => {
//         val events: FrameInputEvents =
//           FrameInputEvents(globalEvents, signals)

//         val updatedModel: Outcome[Model] = globalEvents.foldLeft(Outcome(model)) { (acc, e) =>
//           acc.flatMapState { next =>
//             scene.updateSceneModel(gameTime, acc.state)(e)
//           }
//         }

//         val updatedViewModel: Outcome[ViewModel] =
//           updatedModel.flatMapState { m =>
//             scene.updateSceneViewModel(gameTime, m, viewModel, events)
//           }

//         val view: SceneUpdateFragment =
//           scene.updateSceneView(gameTime, updatedModel.state, updatedViewModel.state, events)

//         (updatedModel |+| updatedViewModel, view)
//       }

// }
