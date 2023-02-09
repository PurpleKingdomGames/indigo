package indigoextras.ui

import indigo.shared.Outcome
import indigo.shared.input.Pointers
import indigoextras.geometry.Polygon
import indigoextras.geometry.Vertex

// https://zingchart.github.io/zingtouch
// https://hammerjs.github.io

// tap    - Recognized when the pointer is doing a small tap/click. Multiple taps are recognized if they occur between the given interval and position
// swipe  - Recognized when the pointer is moving fast (velocity), with enough distance in the allowed direction.
// pinch  - Recognized when two or more pointers are moving toward (zoom-in) or away from each other (zoom-out).
// press  - Recognized when the pointer is down for x ms without any movement.
// pan    - Recognized when the pointer is down and moved in the allowed direction.
// rotate - Recognized when two or more pointer are moving in a circular motion.


final case class GestureArea(
    area: Polygon.Closed
) derives CanEqual:
  def update(pointers: Pointers): Outcome[GestureArea] =
    val pointersInBounds = area.contains(Vertex.fromPoint(pointers.position))

    Outcome(
      this
    )
