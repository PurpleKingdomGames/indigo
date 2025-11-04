package indigo.platform.assets

import indigo.platform.assets.*
import indigo.shared.PowerOfTwo
import indigo.shared.assets.AssetName
import indigo.shared.assets.AssetTag
import indigo.shared.datatypes.Point

class TextureAtlasTests extends munit.FunSuite {

  given CanEqual[Option[AtlasLookupResult], Option[AtlasLookupResult]] = CanEqual.derived

  test("A texture atlas.should be able to generate a TextureAtlas with the default maximum") {

    val imageRefs = List(
      ImageRef(AssetName("a"), 10, 10, None),
      ImageRef(AssetName("b"), 1024, 1024, None),
      ImageRef(AssetName("c"), 512, 512, None),
      ImageRef(AssetName("d"), 700, 600, None),
      ImageRef(AssetName("e"), 5000, 300, None)
    )

    val lookupByName: AssetName => Option[LoadedImageAsset] = _ => None
    val createAtlasFunc: (TextureMap, AssetName => Option[LoadedImageAsset]) => Atlas =
      (_, _) => new Atlas(PowerOfTwo.Max, None)

    val actual: TextureAtlas = TextureAtlas.create(imageRefs, lookupByName, createAtlasFunc)

    assert(
      actual.lookUpByName(AssetName("a")) == Some(
        new AtlasLookupResult(
          AssetName("a"),
          AtlasId(TextureAtlas.IdPrefix + "0"),
          new Atlas(PowerOfTwo.Max, None),
          Point(512, 0)
        )
      )
    )

    assert(
      actual.lookUpByName(AssetName("b")) == Some(
        new AtlasLookupResult(
          AssetName("b"),
          AtlasId(TextureAtlas.IdPrefix + "0"),
          new Atlas(PowerOfTwo.Max, None),
          Point(1024, 0)
        )
      )
    )

    assert(
      actual.lookUpByName(AssetName("c")) == Some(
        new AtlasLookupResult(
          AssetName("c"),
          AtlasId(TextureAtlas.IdPrefix + "0"),
          new Atlas(PowerOfTwo.Max, None),
          Point.zero
        )
      )
    )

    assert(
      actual.lookUpByName(AssetName("d")) == Some(
        new AtlasLookupResult(
          AssetName("d"),
          AtlasId(TextureAtlas.IdPrefix + "0"),
          new Atlas(PowerOfTwo.Max, None),
          Point(0, 1024)
        )
      )
    )

    assert(
      actual.lookUpByName(AssetName("e")) == Some(
        new AtlasLookupResult(
          AssetName("e"),
          AtlasId(TextureAtlas.IdPrefix + "1"),
          new Atlas(PowerOfTwo.Max, None),
          Point.zero
        )
      )
    )

  }

  test("A texture atlas.should be able to generate a tighter TextureAtlas") {

    val imageRefs = List(
      ImageRef(AssetName("a"), 64, 64, None),
      ImageRef(AssetName("b"), 100, 100, None),
      ImageRef(AssetName("c"), 128, 128, None),
      ImageRef(AssetName("d"), 32, 32, None),
      ImageRef(AssetName("e"), 64, 64, None)
    )

    val lookupByName: AssetName => Option[LoadedImageAsset] = _ => None
    val createAtlasFunc: (TextureMap, AssetName => Option[LoadedImageAsset]) => Atlas =
      (_, _) => new Atlas(PowerOfTwo._128, None)

    val actual: TextureAtlas = TextureAtlas.createWithMaxSize(PowerOfTwo._128, imageRefs, lookupByName, createAtlasFunc)

    assert(
      actual.lookUpByName(AssetName("a")) == Some(
        new AtlasLookupResult(
          AssetName("a"),
          AtlasId(TextureAtlas.IdPrefix + "0"),
          new Atlas(PowerOfTwo._128, None),
          Point(0, 0)
        )
      )
    )

    assert(
      actual.lookUpByName(AssetName("b")) == Some(
        new AtlasLookupResult(
          AssetName("b"),
          AtlasId(TextureAtlas.IdPrefix + "1"),
          new Atlas(PowerOfTwo._128, None),
          Point(0, 0)
        )
      )
    )

    assert(
      actual.lookUpByName(AssetName("c")) == Some(
        new AtlasLookupResult(
          AssetName("c"),
          AtlasId(TextureAtlas.IdPrefix + "2"),
          new Atlas(PowerOfTwo._128, None),
          Point(0, 0)
        )
      )
    )

    assert(
      actual.lookUpByName(AssetName("d")) == Some(
        new AtlasLookupResult(
          AssetName("d"),
          AtlasId(TextureAtlas.IdPrefix + "0"),
          new Atlas(PowerOfTwo._128, None),
          Point(64, 0)
        )
      )
    )

    assert(
      actual.lookUpByName(AssetName("e")) == Some(
        new AtlasLookupResult(
          AssetName("e"),
          AtlasId(TextureAtlas.IdPrefix + "0"),
          new Atlas(PowerOfTwo._128, None),
          Point(0, 64)
        )
      )
    )

  }

  test("The texture atlas functions.should be able to pick the right bucket for my image size") {
    assertEquals(TextureAtlasFunctions.pickPowerOfTwoSizeFor(TextureAtlas.supportedSizes, 116, 24).value, 128)
  }

  test("The texture atlas functions.should be able to tell if an image is too big") {

    assertEquals(TextureAtlasFunctions.isTooBig(PowerOfTwo.Max, 10, 10), false)
    assertEquals(TextureAtlasFunctions.isTooBig(PowerOfTwo._512, 1024, 1024), true)

  }

  test("The texture atlas functions.should be able to sort the images into descending size order") {

    val original = List(
      ImageRef(AssetName("a"), 10, 10, None),
      ImageRef(AssetName("b"), 1024, 1024, None),
      ImageRef(AssetName("c"), 512, 512, None),
      ImageRef(AssetName("d"), 700, 600, None)
    )

    val expected = List(
      TextureDetails(ImageRef(AssetName("d"), 700, 600, None), PowerOfTwo._1024, None),
      TextureDetails(ImageRef(AssetName("b"), 1024, 1024, None), PowerOfTwo._1024, None),
      TextureDetails(ImageRef(AssetName("c"), 512, 512, None), PowerOfTwo._512, None),
      TextureDetails(ImageRef(AssetName("a"), 10, 10, None), PowerOfTwo._16, None)
    )

    assertEquals(TextureAtlasFunctions.inflateAndSortByPowerOfTwo(original), expected)

  }

  test("The texture atlas functions.should be able to create a tree from one image") {

    val imageRef   = ImageRef(AssetName("b"), 1024, 1024, None)
    val powerOfTwo = PowerOfTwo._1024

    val original = TextureDetails(imageRef, powerOfTwo, None)

    val expected = AtlasQuadNode(
      powerOfTwo,
      AtlasTexture(
        imageRef
      )
    )

    assertEquals(TextureAtlasFunctions.convertTextureDetailsToTree(original), expected)

  }

  test(
    "The texture atlas functions.should be able to take a list of texture details and group them into 'atlasable' groups"
  ) {

    val tex = (name: AssetName, pow: PowerOfTwo) => TextureDetails(ImageRef(name, 1, 1, None), pow, None)

    val list: List[TextureDetails] = List(
      tex(AssetName("a"), PowerOfTwo._256),
      tex(AssetName("b"), PowerOfTwo._256),
      tex(AssetName("c"), PowerOfTwo._128),
      tex(AssetName("d"), PowerOfTwo._64),
      tex(AssetName("e"), PowerOfTwo._256),
      tex(AssetName("f"), PowerOfTwo._8),
      tex(AssetName("g"), PowerOfTwo._4),
      tex(AssetName("h"), PowerOfTwo._64),
      tex(AssetName("i"), PowerOfTwo._128),
      tex(AssetName("j"), PowerOfTwo._2),
      tex(AssetName("k"), PowerOfTwo._256)
    )

    val result = TextureAtlasFunctions
      .groupTexturesIntoAtlasBuckets(PowerOfTwo._256)(list)

    assertEquals(result.forall(l => l.map(_.size.value).sum <= 256 * 2), true)

  }

  test("The texture atlas functions.grouping with tags") {

    val tex = (name: AssetName, pow: PowerOfTwo, tag: AssetTag) =>
      TextureDetails(ImageRef(name, 1, 1, None), pow, if (tag.toString.isEmpty()) None else Some(tag))

    val list: List[TextureDetails] = List(
      tex(AssetName("a"), PowerOfTwo._256, AssetTag("tag 1")),
      tex(AssetName("b"), PowerOfTwo._256, AssetTag("tag 1")),
      tex(AssetName("c"), PowerOfTwo._128, AssetTag("")),
      tex(AssetName("d"), PowerOfTwo._64, AssetTag("tag 2")),
      tex(AssetName("e"), PowerOfTwo._256, AssetTag("tag 3")),
      tex(AssetName("f"), PowerOfTwo._8, AssetTag("tag 3")),
      tex(AssetName("g"), PowerOfTwo._4, AssetTag("tag 1")),
      tex(AssetName("h"), PowerOfTwo._64, AssetTag("tag 2")),
      tex(AssetName("i"), PowerOfTwo._128, AssetTag("tag 2")),
      tex(AssetName("j"), PowerOfTwo._2, AssetTag("tag 3")),
      tex(AssetName("k"), PowerOfTwo._256, AssetTag(""))
    )

    /*
          no tag = Atlas 0
          tag 1  = Atlas 1
          tag 2  = Atlas 2
          tag 3  = Atlas 3
     */

    val result =
      TextureAtlasFunctions
        .groupTexturesIntoAtlasBuckets(PowerOfTwo._512)(list)

    // val pretty: String =
    //   result.map { l =>
    //     l.map(_.toString()).mkString("  ", ",\n  ", "")
    //   }.mkString("\n\n")

    // println(pretty)

    assertEquals(result.length, 4)

    assertEquals(result(0).length, 2)
    assertEquals(result(1).length, 3)
    assertEquals(result(2).length, 3)
    assertEquals(result(3).length, 3)

    assertEquals(result(0).forall(_.tag.isEmpty), true)
    assertEquals(result(1).forall(_.tag.get == AssetTag("tag 1")), true)
    assertEquals(result(2).forall(_.tag.get == AssetTag("tag 2")), true)
    assertEquals(result(3).forall(_.tag.get == AssetTag("tag 3")), true)

    assertEquals(result(2).map(_.imageRef.name).contains("d"), true)
    assertEquals(result(2).map(_.imageRef.name).contains("h"), true)
    assertEquals(result(2).map(_.imageRef.name).contains("i"), true)
  }

  val a =
    AtlasQuadNode(
      PowerOfTwo._1024,
      AtlasTexture(
        ImageRef(AssetName("a"), 1024, 768, None)
      )
    )

  val b =
    AtlasQuadNode(
      PowerOfTwo._512,
      AtlasTexture(
        ImageRef(AssetName("b"), 500, 400, None)
      )
    )

  val c =
    AtlasQuadNode(
      PowerOfTwo._64,
      AtlasTexture(
        ImageRef(AssetName("c"), 62, 48, None)
      )
    )

  val d =
    AtlasQuadNode(
      PowerOfTwo._128,
      AtlasTexture(
        ImageRef(AssetName("d"), 62, 127, None)
      )
    )

  val aPlusB =
    AtlasQuadNode(
      PowerOfTwo._2048,
      AtlasQuadDivision(
        a,
        AtlasQuadNode(
          PowerOfTwo._1024,
          AtlasQuadDivision(
            b,
            AtlasQuadEmpty(PowerOfTwo._512),
            AtlasQuadEmpty(PowerOfTwo._512),
            AtlasQuadEmpty(PowerOfTwo._512)
          )
        ),
        AtlasQuadEmpty(PowerOfTwo._1024),
        AtlasQuadEmpty(PowerOfTwo._1024)
      )
    )

  test("tree manipulation.should be able to merge two single item trees together") {

    val max = PowerOfTwo._4096

    assertEquals(TextureAtlasFunctions.mergeTrees(a, b, max), Some(aPlusB))

  }

  test("tree manipulation.should be able to merge a single item tree with a more complex tree together") {

    val expected =
      AtlasQuadNode(
        PowerOfTwo._2048,
        AtlasQuadDivision(
          AtlasQuadNode(PowerOfTwo._1024, AtlasTexture(ImageRef(AssetName("a"), 1024, 768, None))),
          AtlasQuadNode(
            PowerOfTwo._1024,
            AtlasQuadDivision(
              AtlasQuadNode(PowerOfTwo._512, AtlasTexture(ImageRef(AssetName("b"), 500, 400, None))),
              AtlasQuadEmpty(PowerOfTwo._512),
              AtlasQuadEmpty(PowerOfTwo._512),
              AtlasQuadEmpty(PowerOfTwo._512)
            )
          ),
          AtlasQuadNode(
            PowerOfTwo._1024,
            AtlasQuadDivision(
              AtlasQuadNode(
                PowerOfTwo._512,
                AtlasQuadDivision(
                  AtlasQuadNode(
                    PowerOfTwo._256,
                    AtlasQuadDivision(
                      AtlasQuadNode(
                        PowerOfTwo._128,
                        AtlasQuadDivision(
                          AtlasQuadNode(PowerOfTwo._64, AtlasTexture(ImageRef(AssetName("c"), 62, 48, None))),
                          AtlasQuadEmpty(PowerOfTwo._64),
                          AtlasQuadEmpty(PowerOfTwo._64),
                          AtlasQuadEmpty(PowerOfTwo._64)
                        )
                      ),
                      AtlasQuadEmpty(PowerOfTwo._128),
                      AtlasQuadEmpty(PowerOfTwo._128),
                      AtlasQuadEmpty(PowerOfTwo._128)
                    )
                  ),
                  AtlasQuadEmpty(PowerOfTwo._256),
                  AtlasQuadEmpty(PowerOfTwo._256),
                  AtlasQuadEmpty(PowerOfTwo._256)
                )
              ),
              AtlasQuadEmpty(PowerOfTwo._512),
              AtlasQuadEmpty(PowerOfTwo._512),
              AtlasQuadEmpty(PowerOfTwo._512)
            )
          ),
          AtlasQuadNode(
            PowerOfTwo._1024,
            AtlasQuadDivision(
              AtlasQuadNode(
                PowerOfTwo._512,
                AtlasQuadDivision(
                  AtlasQuadNode(
                    PowerOfTwo._256,
                    AtlasQuadDivision(
                      AtlasQuadNode(PowerOfTwo._128, AtlasTexture(ImageRef(AssetName("d"), 62, 127, None))),
                      AtlasQuadEmpty(PowerOfTwo._128),
                      AtlasQuadEmpty(PowerOfTwo._128),
                      AtlasQuadEmpty(PowerOfTwo._128)
                    )
                  ),
                  AtlasQuadEmpty(PowerOfTwo._256),
                  AtlasQuadEmpty(PowerOfTwo._256),
                  AtlasQuadEmpty(PowerOfTwo._256)
                )
              ),
              AtlasQuadEmpty(PowerOfTwo._512),
              AtlasQuadEmpty(PowerOfTwo._512),
              AtlasQuadEmpty(PowerOfTwo._512)
            )
          )
        )
      )

    val max = PowerOfTwo._4096

    TextureAtlasFunctions.mergeTrees(aPlusB, c, max) match {
      case Some(aPlusBPlusC) =>
        assertEquals(TextureAtlasFunctions.mergeTrees(aPlusBPlusC, d, max), Some(expected))

      case _ =>
        fail("error")
    }

  }

  test("tree manipulation.should merge two trees where one is empty") {

    val max = PowerOfTwo._4096

    assertEquals(TextureAtlasFunctions.mergeTrees(a, AtlasQuadEmpty(PowerOfTwo._128), max), Some(a))
    assertEquals(TextureAtlasFunctions.mergeTrees(AtlasQuadEmpty(PowerOfTwo._128), b, max), Some(b))

  }

  test("tree manipulation.should not merge tree B into empty tree A which cannot accommodate") {

    val a = AtlasQuadNode(PowerOfTwo._4, AtlasQuadDivision.empty(PowerOfTwo._2))
    val b = AtlasQuadNode(PowerOfTwo._128, AtlasTexture(ImageRef(AssetName("b"), 128, 128, None)))

    assertEquals(TextureAtlasFunctions.mergeTreeBIntoA(a, b), None)

  }

  test("tree manipulation.should be able to merge tree B into empty tree A which can accommodate") {

    val a = AtlasQuadNode(PowerOfTwo._256, AtlasQuadDivision.empty(PowerOfTwo._128))
    val b = AtlasQuadNode(PowerOfTwo._128, AtlasTexture(ImageRef(AssetName("b"), 128, 128, None)))

    val expected = Some(
      AtlasQuadNode(
        PowerOfTwo._256,
        AtlasQuadDivision(
          b,
          AtlasQuadEmpty(PowerOfTwo._128),
          AtlasQuadEmpty(PowerOfTwo._128),
          AtlasQuadEmpty(PowerOfTwo._128)
        )
      )
    )

    assertEquals(TextureAtlasFunctions.mergeTreeBIntoA(a, b), expected)

  }

  test("tree manipulation.should not merge two trees that would result in a texture too large") {

    val max = PowerOfTwo._1024

    assertEquals(TextureAtlasFunctions.mergeTrees(a, b, max), None)

  }

  test("tree manipulation.should be able to report if it can accomodate another tree of size") {

    assertEquals(aPlusB.canAccommodate(PowerOfTwo._1024), true)

  }

  test("tree manipulation.should be able to fill a small tree (A)") {

    val initial: AtlasQuadTree =
      TextureAtlasFunctions.createEmptyTree(PowerOfTwo._16)

    val quad = (id: AssetName, size: PowerOfTwo) => AtlasQuadNode(size, AtlasTexture(ImageRef(id, 1, 1, None)))

    val quads: List[AtlasQuadTree] = List(
      quad(AssetName("8_1"), PowerOfTwo._8),
      quad(AssetName("8_2"), PowerOfTwo._8),
      quad(AssetName("8_3"), PowerOfTwo._8),
      quad(AssetName("8_4"), PowerOfTwo._8)
    )

    val res = quads.foldLeft(initial)((a, b) => TextureAtlasFunctions.mergeTreeBIntoA(a, b).get)

    val expected =
      AtlasQuadNode(
        PowerOfTwo._16,
        AtlasQuadDivision(
          quad(AssetName("8_1"), PowerOfTwo._8),
          quad(AssetName("8_2"), PowerOfTwo._8),
          quad(AssetName("8_3"), PowerOfTwo._8),
          quad(AssetName("8_4"), PowerOfTwo._8)
        )
      )

    assertEquals(res, expected)
  }

  test("tree manipulation.should be able to fill a small tree (B)") {

    val quad = (id: String, size: PowerOfTwo) => AtlasQuadNode(size, AtlasTexture(ImageRef(AssetName(id), 1, 1, None)))

    val quads: List[AtlasQuadTree] = List(
      quad("8_1", PowerOfTwo._8),
      quad("8_2", PowerOfTwo._8),
      quad("8_3", PowerOfTwo._8),
      quad("4_4", PowerOfTwo._4),
      quad("4_5", PowerOfTwo._4),
      quad("4_6", PowerOfTwo._4),
      quad("4_7", PowerOfTwo._4)
    )

    val res: AtlasQuadTree = quads.foldLeft(AtlasQuadTree.identity)(_ + _)

    val expected =
      AtlasQuadNode(
        PowerOfTwo._16,
        AtlasQuadDivision(
          quad("8_1", PowerOfTwo._8),
          quad("8_2", PowerOfTwo._8),
          quad("8_3", PowerOfTwo._8),
          AtlasQuadNode(
            PowerOfTwo._8,
            AtlasQuadDivision(
              quad("4_4", PowerOfTwo._4),
              quad("4_5", PowerOfTwo._4),
              quad("4_6", PowerOfTwo._4),
              quad("4_7", PowerOfTwo._4)
            )
          )
        )
      )

    assertEquals(res, expected)
  }

  test("tree manipulation.should be able to create a texture map of a small tree") {

    val quad = (id: String, size: PowerOfTwo) => AtlasQuadNode(size, AtlasTexture(ImageRef(AssetName(id), 1, 1, None)))

    val quads: List[AtlasQuadTree] = List(
      quad("8_1", PowerOfTwo._8),
      quad("8_2", PowerOfTwo._8),
      quad("8_3", PowerOfTwo._8),
      quad("8_4", PowerOfTwo._8)
    )

    val actual = quads.foldLeft(AtlasQuadTree.identity)(_ + _)

    val expected =
      AtlasQuadNode(
        PowerOfTwo._16,
        AtlasQuadDivision(
          quad("8_1", PowerOfTwo._8),
          quad("8_2", PowerOfTwo._8),
          quad("8_3", PowerOfTwo._8),
          quad("8_4", PowerOfTwo._8)
        )
      )

    assertEquals(actual, expected)

    actual match {
      case node: AtlasQuadNode =>
        val textureMap = node.toTextureMap

        assertEquals(textureMap.size == PowerOfTwo._16, true)

        assertEquals(
          textureMap.textureCoords.find(_.imageRef.name == AssetName("8_1")).map(_.coords) == Some(Point(0, 0)),
          true
        )
        assertEquals(
          textureMap.textureCoords.find(_.imageRef.name == AssetName("8_2")).map(_.coords) == Some(Point(8, 0)),
          true
        )
        assertEquals(
          textureMap.textureCoords.find(_.imageRef.name == AssetName("8_3")).map(_.coords) == Some(Point(0, 8)),
          true
        )
        assertEquals(
          textureMap.textureCoords.find(_.imageRef.name == AssetName("8_4")).map(_.coords) == Some(Point(8, 8)),
          true
        )

      case _ =>
        fail("error")
    }
  }

}
