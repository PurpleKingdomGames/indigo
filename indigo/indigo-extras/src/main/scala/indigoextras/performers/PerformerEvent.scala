package indigoextras.performers

import indigo.shared.collections.Batch
import indigo.shared.events.GlobalEvent

enum PerformerEvent extends GlobalEvent:
  case Spawn(actor: Performer[?])
  case Kill(id: PerformerId)
  case KillAll(ids: Batch[PerformerId])
