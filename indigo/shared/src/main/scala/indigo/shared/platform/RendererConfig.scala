package indigo.shared.platform

import indigo.shared.ClearColor
import indigo.shared.config.RenderingTechnology

final class RendererConfig(val renderingTechnology: RenderingTechnology, val clearColor: ClearColor, val magnification: Int, val maxBatchSize: Int, val antiAliasing: Boolean)
