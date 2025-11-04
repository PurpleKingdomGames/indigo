package indigo.shared.collections

import org.scalacheck.*

import scala.annotation.nowarn

@nowarn("msg=unused")
class BatchSpecification extends Properties("Batch") {
  property("hashCode - both Wrapped") = Prop.forAll { (a: List[Int]) =>
    Batch(a*).hashCode == Batch(a*).hashCode
  }

  property("hashCode - both Combined") = Prop.forAll { (a: List[Int], b: List[Int], c: List[Int]) =>
    Batch.Combine(Batch.Combine(Batch(a*), Batch(b*)), Batch(c*)).hashCode ==
      Batch.Combine(Batch(a*), Batch.Combine(Batch(b*), Batch(c*))).hashCode
  }

  property("hashCode - Wrapped and Combined, same elements") = Prop.forAll { (a: List[Int], b: List[Int]) =>
    Batch((a ++ b)*).hashCode == Batch.Combine(Batch(a*), Batch(b*)).hashCode
  }
}
