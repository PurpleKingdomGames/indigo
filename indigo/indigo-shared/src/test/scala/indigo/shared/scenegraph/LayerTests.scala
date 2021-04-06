package indigo.shared.scenegraph

import indigo.shared.datatypes.BindingKey

class LayerTests extends munit.FunSuite {

  test("combining layer should preserve left hand side magnification") {

    assertEquals(Layer.empty(BindingKey("key A")).key, Some(BindingKey("key A")))
    assertEquals((Layer.empty(BindingKey("key A")) |+| Layer.empty(BindingKey("key B"))).key, Some(BindingKey("key A")))
    assertEquals((Layer.empty(BindingKey("key A")) |+| Layer(Nil)).key, Some(BindingKey("key A")))
    assertEquals((Layer(Nil) |+| Layer.empty(BindingKey("key B"))).key, Some(BindingKey("key B")))

  }

}
