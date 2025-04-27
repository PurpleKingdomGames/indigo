package indigoextras.performers

import indigo.shared.collections.Batch
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.LayerKey

enum PerformerEvent extends GlobalEvent:
  case Add(layerKey: LayerKey, actor: Performer[?])
  case AddAll(layerKey: LayerKey, actor: Batch[Performer[?]])
  case Remove(id: PerformerId)
  case RemoveAll(ids: Batch[PerformerId])
  case ChangeLayer(id: PerformerId, layerKey: LayerKey)
