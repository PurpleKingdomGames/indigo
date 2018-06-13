package com.purplekingdomgames.indigo.runtime.metrics

case class MetricWrapper(metric: Metric, time: Long)

sealed trait Metric {
  val name: String
}

// In Order (unless otherwise stated)!
case object FrameStartMetric extends Metric { val name: String = "frame start" }

case object UpdateStartMetric              extends Metric { val name: String = "update model start"      }
case object CallUpdateGameModelStartMetric extends Metric { val name: String = "call update model start" } //nested
case object CallUpdateGameModelEndMetric   extends Metric { val name: String = "call update model end"   } //nested
case object UpdateEndMetric                extends Metric { val name: String = "update model end"        }

case object CallUpdateViewStartMetric extends Metric { val name: String = "call update view start" }
case object CallUpdateViewEndMetric   extends Metric { val name: String = "call update view end"   }
case object ProcessViewStartMetric    extends Metric { val name: String = "process view start"     }
// Process metrics (below) go here.
case object ProcessViewEndMetric     extends Metric { val name: String = "process view end"             }
case object ToDisplayableStartMetric extends Metric { val name: String = "convert to displayable start" }
case object ToDisplayableEndMetric   extends Metric { val name: String = "convert to displayable end"   }
case object RenderStartMetric        extends Metric { val name: String = "render start"                 }
// Renderer metrics (below) go here
case object RenderEndMetric extends Metric { val name: String = "render end" }

case object SkippedModelUpdateMetric extends Metric { val name: String = "skipped model update" }
case object SkippedViewUpdateMetric  extends Metric { val name: String = "skipped view update"  }

case object FrameEndMetric extends Metric { val name: String = "frame end" }

case object AudioStartMetric extends Metric { val name: String = "audio start" }
case object AudioEndMetric   extends Metric { val name: String = "audio end"   }

// Process view metrics
case object PersistGlobalViewEventsStartMetric extends Metric { val name: String = "persist global view events start" }
case object PersistGlobalViewEventsEndMetric   extends Metric { val name: String = "persist global view events end"   }

case object PersistNodeViewEventsStartMetric extends Metric { val name: String = "persist node view events start" }
case object PersistNodeViewEventsEndMetric   extends Metric { val name: String = "persist node view events end"   }

case object ApplyAnimationMementoStartMetric extends Metric { val name: String = "apply animation mementos start" }
case object ApplyAnimationMementoEndMetric   extends Metric { val name: String = "apply animation mementos end"   }

case object RunAnimationActionsStartMetric extends Metric { val name: String = "run animation actions start" }
case object RunAnimationActionsEndMetric   extends Metric { val name: String = "run animation actions end"   }

case object PersistAnimationStatesStartMetric extends Metric { val name: String = "persist animation states start" }
case object PersistAnimationStatesEndMetric   extends Metric { val name: String = "persist animation states end"   }

// Renderer metrics
case object DrawGameLayerStartMetric extends Metric { val name: String = "draw game layer start" }
case object DrawGameLayerEndMetric   extends Metric { val name: String = "draw game layer end"   }

case object DrawLightingLayerStartMetric extends Metric { val name: String = "draw lighting layer start" }
case object DrawLightingLayerEndMetric   extends Metric { val name: String = "draw lighting layer end"   }

case object DrawUiLayerStartMetric extends Metric { val name: String = "draw ui layer start" }
case object DrawUiLayerEndMetric   extends Metric { val name: String = "draw ui layer end"   }

case object RenderToConvasStartMetric extends Metric { val name: String = "render to canvas start" }
case object RenderToConvasEndMetric   extends Metric { val name: String = "render to canvas end"   }

case object LightingDrawCallMetric    extends Metric { val name: String = "draw call: lighting"  }
case object NormalLayerDrawCallMetric extends Metric { val name: String = "draw call: normal"    }
case object ToCanvasDrawCallMetric    extends Metric { val name: String = "draw call: to canvas" }

case object NormalDrawCallLengthStartMetric   extends Metric { val name: String = "draw call length start: normal"   }
case object NormalDrawCallLengthEndMetric     extends Metric { val name: String = "draw call length end: normal"     }
case object LightingDrawCallLengthStartMetric extends Metric { val name: String = "draw call length start: lighting" }
case object LightingDrawCallLengthEndMetric   extends Metric { val name: String = "draw call length end: lighting"   }
case object ToCanvasDrawCallLengthStartMetric extends Metric { val name: String = "draw call length start: canvas"   }
case object ToCanvasDrawCallLengthEndMetric   extends Metric { val name: String = "draw call length end: canvas"     }
