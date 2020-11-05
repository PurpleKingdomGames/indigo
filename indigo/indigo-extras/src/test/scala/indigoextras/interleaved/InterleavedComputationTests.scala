package indigoextras.interleaved

import utest._

object InterleavedComputationTests extends TestSuite {

  val tests: Tests =
    Tests {

      "perform step" - {

        val monitoredStep =
          MyStep()

        val computation =
          InterleavedComputation(0, List(MyStep(), MyStep(), MyStep()))

        val actual =
          computation.performStep.performStep.performStep

        val expected =
          InterleavedComputation(3, (), Nil, 0, 3)

        actual ==> expected
      }

    }
}

final case class MyStep() extends MonitoredStep[Unit, Int] {
  def perform(reference: Unit, current: Int): Int = current + 1
  val size: Int                                   = 1
}
