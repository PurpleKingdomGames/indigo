package indigo.shared.shader.library

import ultraviolet.syntax.*

import scala.annotation.nowarn

object NoOp:

  @nowarn("msg=unused")
  inline def vertex[E] =
    Shader[E] { _ =>
      def vertex(v: vec4): vec4 = v
    }

  @nowarn("msg=unused")
  inline def fragment[E] =
    Shader[E] { _ =>
      def fragment(v: vec4): vec4 = v
    }

  @nowarn("msg=unused")
  inline def prepare[E] =
    Shader[E] { _ =>
      def prepare: Unit = ()
    }

  @nowarn("msg=unused")
  inline def light[E] =
    Shader[E] { _ =>
      def light: Unit = ()
    }

  @nowarn("msg=unused")
  inline def composite[E] =
    Shader[E] { _ =>
      def composite: Unit = ()
    }
