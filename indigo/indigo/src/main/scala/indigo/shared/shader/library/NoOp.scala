package indigo.shared.shader.library

import indigo.shared.shader.library.IndigoUV.*
import ultraviolet.syntax.*

object NoOp:

  inline def vertex[E] =
    Shader[E] { _ =>
      def vertex(v: vec4): vec4 = v
    }

  inline def fragment[E] =
    Shader[E] { _ =>
      def fragment(v: vec4): vec4 = v
    }

  inline def prepare[E] =
    Shader[E] { _ =>
      def prepare: Unit = ()
    }

  inline def light[E] =
    Shader[E] { _ =>
      def light: Unit = ()
    }

  inline def composite[E] =
    Shader[E] { _ =>
      def composite: Unit = ()
    }
