package indigo.shared.scenegraph

import indigo.shared.datatypes.BindingKey

class LayerTests extends munit.FunSuite {

  test("combining layer should preserve left hand side magnification") {

    assertEquals(Layer(BindingKey("key A")).key, Some(BindingKey("key A")))
    assertEquals((Layer(BindingKey("key A")) |+| Layer(BindingKey("key B"))).key, Some(BindingKey("key A")))
    assertEquals((Layer(BindingKey("key A")) |+| Layer(Nil)).key, Some(BindingKey("key A")))
    assertEquals((Layer(Nil) |+| Layer(BindingKey("key B"))).key, Some(BindingKey("key B")))

  }

}
