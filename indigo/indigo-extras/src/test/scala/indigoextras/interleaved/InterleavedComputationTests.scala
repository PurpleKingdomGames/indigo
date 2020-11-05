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

        computation ==> InterleavedComputation(0, (), List(MyStep(), MyStep(), MyStep()), 0, 0)

        computation.performStep ==> InterleavedComputation(1, (), List(MyStep(), MyStep()), 0, 1)

        computation.performStep.performStep ==> InterleavedComputation(2, (), List(MyStep()), 0, 2)

        computation.performStep.performStep.performStep ==> InterleavedComputation(3, (), Nil, 0, 3)
      }

    }
}

final case class MyStep() extends MonitoredStep[Unit, Int] {
  def perform(reference: Unit, current: Int): Int = current + 1
  val size: Int                                   = 1
}
