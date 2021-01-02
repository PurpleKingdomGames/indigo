package indigoextras.animation

import indigo.shared.time.GameTime
import indigo.shared.time.Seconds

class TimelineTests extends munit.FunSuite {

  /*

  play/start an animaiton
  stop -  same as not calling play?
  Jump to next marker
  Jump to previous marker
  Jump to marker
  Jump to start
  Jump to end
  Report overall progress
  Report progress through current tween
  Report which marker it's in.
   */

  test("Playback controls: play") {
    val t = Timeline(Marker(MarkerLabel("a"), Seconds(11000)))

    assertEquals(t.play(GameTime.withDelta(Seconds.zero, Seconds(1))).playhead, Seconds(1))
    assertEquals(t.play(GameTime.withDelta(Seconds.zero, Seconds(10))).playhead, Seconds(10))
    assertEquals(t.play(GameTime.withDelta(Seconds.zero, Seconds(11000))).playhead, Seconds(11000))
  }

  test("Playback controls: reverse / play backwards") {
    val t = Timeline(List(Marker(MarkerLabel("a"), Seconds(300))), Seconds(300))

    assertEquals(t.reverse(GameTime.withDelta(Seconds.zero, Seconds(1))).playhead, Seconds(299))
    assertEquals(t.reverse(GameTime.withDelta(Seconds.zero, Seconds(10))).playhead, Seconds(290))
    assertEquals(t.reverse(GameTime.withDelta(Seconds.zero, Seconds(11000))).playhead, Seconds(0))
  }

  test("Playback controls: pause / stop (or do nothing)") {
    val t = Timeline(Marker(MarkerLabel("a"), Seconds(100)))
      .play(GameTime.withDelta(Seconds.zero, Seconds(10)))
      .pause
      .play(GameTime.withDelta(Seconds.zero, Seconds(10)))
      .stop
      .play(GameTime.withDelta(Seconds.zero, Seconds(10)))

    assertEquals(t.playhead, Seconds(30))
  }

  test("Playback controls: skip to a time") {
    val t = Timeline().skipTo(Seconds(100))

    assertEquals(t.playhead, Seconds(100))
  }

  test("Playback controls: jump to start") {
    val t = Timeline().skipTo(Seconds(10)).jumpToStart

    assertEquals(t.playhead, Seconds(0))
  }

  test("Playback controls: jump to end") {
    val t =
      Timeline(
        Marker(MarkerLabel("a"), Seconds(2)),
        Marker(MarkerLabel("b"), Seconds(4))
      ).jumpToEnd

    assertEquals(t.playhead, Seconds(4))
  }

  test("Playback controls: jump to marker") {
    val t =
      Timeline(
        Marker(MarkerLabel("a"), Seconds(2)),
        Marker(MarkerLabel("b"), Seconds(4)),
        Marker(MarkerLabel("c"), Seconds(10))
      ).jumpTo(MarkerLabel("b"))

    assertEquals(t.playhead, Seconds(4))
  }

  test("Playback controls: jump to next marker") {
    val t =
      Timeline(
        Marker(MarkerLabel("a"), Seconds(2)),
        Marker(MarkerLabel("b"), Seconds(4)),
        Marker(MarkerLabel("c"), Seconds(10))
      ).skipTo(Seconds(3))

    assertEquals(t.jumpToNext.playhead, Seconds(4))
  }

  test("Playback controls: jump to previous marker") {
    val t =
      Timeline(
        Marker(MarkerLabel("a"), Seconds(2)),
        Marker(MarkerLabel("b"), Seconds(4)),
        Marker(MarkerLabel("c"), Seconds(10))
      ).skipTo(Seconds(3))

    assertEquals(t.jumpToPrevious.playhead, Seconds(2))
  }

  test("Reporting: Overall progress") {
    val t =
      Timeline(
        Marker(MarkerLabel("a"), Seconds(2)),
        Marker(MarkerLabel("b"), Seconds(4)),
        Marker(MarkerLabel("c"), Seconds(10))
      ).skipTo(Seconds(5))

    assertEquals(t.duration, Seconds(10))
    assertEquals(t.playhead, Seconds(5))
    assertEquals(t.progress, 0.5d)
  }

  test("Reporting: Progress through current tween") {
    val t =
      Timeline(
        Marker(MarkerLabel("a"), Seconds(2)),
        Marker(MarkerLabel("b"), Seconds(4)),
        Marker(MarkerLabel("c"), Seconds(10))
      )

    assertEquals(t.skipTo(Seconds(0)).tweenProgress, 0.0d)
    assertEquals(t.skipTo(Seconds(1)).tweenProgress, 0.5d)
    assertEquals(t.skipTo(Seconds(2)).tweenProgress, 1.0d)
    assertEquals(t.skipTo(Seconds(3)).tweenProgress, 0.5d)
    assertEquals(t.skipTo(Seconds(4)).tweenProgress, 1.0d)
    assert(nearEnoughEqual(clue(t.skipTo(Seconds(5)).tweenProgress), clue(0.16667d * 1), 0.001))
    assert(nearEnoughEqual(clue(t.skipTo(Seconds(6)).tweenProgress), clue(0.16667d * 2), 0.001))
    assert(nearEnoughEqual(clue(t.skipTo(Seconds(7)).tweenProgress), clue(0.16667d * 3), 0.001))
    assert(nearEnoughEqual(clue(t.skipTo(Seconds(8)).tweenProgress), clue(0.16667d * 4), 0.001))
    assert(nearEnoughEqual(clue(t.skipTo(Seconds(9)).tweenProgress), clue(0.16667d * 5), 0.001))
    assert(nearEnoughEqual(clue(t.skipTo(Seconds(10)).tweenProgress), clue(0.16667d * 6), 0.001))
  }

  def nearEnoughEqual(d1: Double, d2: Double, tolerance: Double): Boolean =
    d1 >= d2 - tolerance && d1 <= d2 + tolerance

  test("Reporting: Next marker") {
    val t =
      Timeline(
        Marker(MarkerLabel("a"), Seconds(2)),
        Marker(MarkerLabel("b"), Seconds(4)),
        Marker(MarkerLabel("c"), Seconds(10))
      ).skipTo(Seconds(5))

    assertEquals(t.nextMarker.get, MarkerLabel("c"))
  }

  test("Reporting: Previous marker") {
    val t =
      Timeline(
        Marker(MarkerLabel("a"), Seconds(2)),
        Marker(MarkerLabel("b"), Seconds(4)),
        Marker(MarkerLabel("c"), Seconds(10))
      ).skipTo(Seconds(5))

    assertEquals(t.previousMarker.get, MarkerLabel("b"))
  }

}
