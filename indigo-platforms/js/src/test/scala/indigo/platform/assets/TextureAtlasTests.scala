package indigo.platform.assets

import utest._

import indigo.shared.PowerOfTwo
import indigo.shared.datatypes.Point
import indigo.TestFail._
import indigo.shared.EqualTo._
import indigo.platform.assets._

object TextureAtlasTests extends TestSuite {

  val tests: Tests =
    Tests {
      "A texture atlas" - {

        "should be able to generate a TextureAtlas with the default maximum" - {

          val imageRefs = List(
            ImageRef("a", 10, 10),
            ImageRef("b", 1024, 1024),
            ImageRef("c", 512, 512),
            ImageRef("d", 700, 600),
            ImageRef("e", 5000, 300)
          )

          val lookupByName: String => Option[LoadedImageAsset]                           = _ => None
          val createAtlasFunc: (TextureMap, String => Option[LoadedImageAsset]) => Atlas = (_, _) => new Atlas(PowerOfTwo.Max, None)

          val actual: TextureAtlas = TextureAtlas.create(imageRefs, lookupByName, createAtlasFunc)

          actual.lookUpByName("a") === Some(
            new AtlasLookupResult("a", new AtlasId(TextureAtlas.IdPrefix + "0"), new Atlas(PowerOfTwo.Max, None), Point(512, 0))
          ) ==> true

          actual.lookUpByName("b") === Some(
            new AtlasLookupResult("b", new AtlasId(TextureAtlas.IdPrefix + "0"), new Atlas(PowerOfTwo.Max, None), Point(1024, 0))
          ) ==> true

          actual.lookUpByName("c") === Some(
            new AtlasLookupResult("c", new AtlasId(TextureAtlas.IdPrefix + "0"), new Atlas(PowerOfTwo.Max, None), Point.zero)
          ) ==> true

          actual.lookUpByName("d") === Some(
            new AtlasLookupResult("d", new AtlasId(TextureAtlas.IdPrefix + "0"), new Atlas(PowerOfTwo.Max, None), Point(0, 1024))
          ) ==> true

          actual.lookUpByName("e") === Some(
            new AtlasLookupResult("e", new AtlasId(TextureAtlas.IdPrefix + "1"), new Atlas(PowerOfTwo.Max, None), Point.zero)
          ) ==> true

        }

        "should be able to generate a tighter TextureAtlas" - {

          val imageRefs = List(
            ImageRef("a", 64, 64),
            ImageRef("b", 100, 100),
            ImageRef("c", 128, 128),
            ImageRef("d", 32, 32),
            ImageRef("e", 64, 64)
          )

          val lookupByName: String => Option[LoadedImageAsset]                           = _ => None
          val createAtlasFunc: (TextureMap, String => Option[LoadedImageAsset]) => Atlas = (_, _) => new Atlas(PowerOfTwo._128, None)

          val actual: TextureAtlas = TextureAtlas.createWithMaxSize(PowerOfTwo._128, imageRefs, lookupByName, createAtlasFunc)

          actual.lookUpByName("a") === Some(
            new AtlasLookupResult("a", new AtlasId(TextureAtlas.IdPrefix + "0"), new Atlas(PowerOfTwo._128, None), Point(0, 0))
          ) ==> true

          actual.lookUpByName("b") === Some(
            new AtlasLookupResult("b", new AtlasId(TextureAtlas.IdPrefix + "1"), new Atlas(PowerOfTwo._128, None), Point(0, 0))
          ) ==> true

          actual.lookUpByName("c") === Some(
            new AtlasLookupResult("c", new AtlasId(TextureAtlas.IdPrefix + "2"), new Atlas(PowerOfTwo._128, None), Point(0, 0))
          ) ==> true

          actual.lookUpByName("d") === Some(
            new AtlasLookupResult("d", new AtlasId(TextureAtlas.IdPrefix + "0"), new Atlas(PowerOfTwo._128, None), Point(64, 0))
          ) ==> true

          actual.lookUpByName("e") === Some(
            new AtlasLookupResult("e", new AtlasId(TextureAtlas.IdPrefix + "0"), new Atlas(PowerOfTwo._128, None), Point(0, 64))
          ) ==> true

        }
      }

      "The texture atlas functions" - {

        "should be able to pick the right bucket for my image size" - {
          TextureAtlasFunctions.pickPowerOfTwoSizeFor(TextureAtlas.supportedSizes, 116, 24).value ==> 128
        }

        "should be able to tell if an image is too big" - {

          TextureAtlasFunctions.isTooBig(PowerOfTwo.Max, 10, 10) ==> false
          TextureAtlasFunctions.isTooBig(PowerOfTwo._512, 1024, 1024) ==> true

        }

        "should be able to sort the images into descending size order" - {

          val original = List(
            ImageRef("a", 10, 10),
            ImageRef("b", 1024, 1024),
            ImageRef("c", 512, 512),
            ImageRef("d", 700, 600)
          )

          val expected = List(
            TextureDetails(ImageRef("d", 700, 600), PowerOfTwo._1024),
            TextureDetails(ImageRef("b", 1024, 1024), PowerOfTwo._1024),
            TextureDetails(ImageRef("c", 512, 512), PowerOfTwo._512),
            TextureDetails(ImageRef("a", 10, 10), PowerOfTwo._16)
          )

          TextureAtlasFunctions.inflateAndSortByPowerOfTwo(original) ==> expected

        }

        "should be able to create a tree from one image" - {

          val imageRef   = ImageRef("b", 1024, 1024)
          val powerOfTwo = PowerOfTwo._1024

          val original = TextureDetails(imageRef, powerOfTwo)

          val expected = AtlasQuadNode(
            powerOfTwo,
            AtlasTexture(
              imageRef
            )
          )

          TextureAtlasFunctions.convertTextureDetailsToTree(original) ==> expected

        }

        "should be able to take a list of texture details and group them into 'atlasable' groups" - {

          val tex = (name: String, pow: PowerOfTwo) => TextureDetails(ImageRef(name, 1, 1), pow)

          val list: List[TextureDetails] = List(
            tex("a", PowerOfTwo._256),
            tex("b", PowerOfTwo._256),
            tex("c", PowerOfTwo._128),
            tex("d", PowerOfTwo._64),
            tex("e", PowerOfTwo._256),
            tex("f", PowerOfTwo._8),
            tex("g", PowerOfTwo._4),
            tex("h", PowerOfTwo._64),
            tex("i", PowerOfTwo._128),
            tex("j", PowerOfTwo._2),
            tex("k", PowerOfTwo._256)
          )

          TextureAtlasFunctions
            .groupTexturesIntoAtlasBuckets(PowerOfTwo._256)(list)
            .forall(l => l.map(_.size.value).sum <= 256 * 2) ==> true

        }

      }

      "tree manipulation" - {

        val a =
          AtlasQuadNode(
            PowerOfTwo._1024,
            AtlasTexture(
              ImageRef("a", 1024, 768)
            )
          )

        val b =
          AtlasQuadNode(
            PowerOfTwo._512,
            AtlasTexture(
              ImageRef("b", 500, 400)
            )
          )

        val c =
          AtlasQuadNode(
            PowerOfTwo._64,
            AtlasTexture(
              ImageRef("c", 62, 48)
            )
          )

        val d =
          AtlasQuadNode(
            PowerOfTwo._128,
            AtlasTexture(
              ImageRef("d", 62, 127)
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

        "should be able to merge two single item trees together" - {

          val max = PowerOfTwo._4096

          TextureAtlasFunctions.mergeTrees(a, b, max) ==> Some(aPlusB)

        }

        "should be able to merge a single item tree with a more complex tree together" - {

          val expected =
            AtlasQuadNode(
              PowerOfTwo._2048,
              AtlasQuadDivision(
                AtlasQuadNode(PowerOfTwo._1024, AtlasTexture(ImageRef("a", 1024, 768))),
                AtlasQuadNode(
                  PowerOfTwo._1024,
                  AtlasQuadDivision(
                    AtlasQuadNode(PowerOfTwo._512, AtlasTexture(ImageRef("b", 500, 400))),
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
                                AtlasQuadNode(PowerOfTwo._64, AtlasTexture(ImageRef("c", 62, 48))),
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
                            AtlasQuadNode(PowerOfTwo._128, AtlasTexture(ImageRef("d", 62, 127))),
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
            case Some(aPlusBPlusC) => TextureAtlasFunctions.mergeTrees(aPlusBPlusC, d, max) ==> Some(expected)

            case _ =>
              fail("error")
          }

        }

        "should merge two trees where one is empty" - {

          val max = PowerOfTwo._4096

          TextureAtlasFunctions.mergeTrees(a, AtlasQuadEmpty(PowerOfTwo._128), max) ==> Some(a)
          TextureAtlasFunctions.mergeTrees(AtlasQuadEmpty(PowerOfTwo._128), b, max) ==> Some(b)

        }

        "should not merge tree B into empty tree A which cannot accommodate" - {

          val a = AtlasQuadNode(PowerOfTwo._4, AtlasQuadDivision.empty(PowerOfTwo._2))
          val b = AtlasQuadNode(PowerOfTwo._128, AtlasTexture(ImageRef("b", 128, 128)))

          TextureAtlasFunctions.mergeTreeBIntoA(a, b) ==> None

        }

        "should be able to merge tree B into empty tree A which can accommodate" - {

          val a = AtlasQuadNode(PowerOfTwo._256, AtlasQuadDivision.empty(PowerOfTwo._128))
          val b = AtlasQuadNode(PowerOfTwo._128, AtlasTexture(ImageRef("b", 128, 128)))

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

          TextureAtlasFunctions.mergeTreeBIntoA(a, b) ==> expected

        }

        "should not merge two trees that would result in a texture too large" - {

          val max = PowerOfTwo._1024

          TextureAtlasFunctions.mergeTrees(a, b, max) ==> None

        }

        "should be able to report if it can accomodate another tree of size" - {

          aPlusB.canAccommodate(PowerOfTwo._1024) ==> true

        }

        "should be able to fill a small tree (A)" - {

          val initial: AtlasQuadTree =
            TextureAtlasFunctions.createEmptyTree(PowerOfTwo._16)

          val quad = (id: String, size: PowerOfTwo) => AtlasQuadNode(size, AtlasTexture(ImageRef(id, 1, 1)))

          val quads: List[AtlasQuadTree] = List(
            quad("8_1", PowerOfTwo._8),
            quad("8_2", PowerOfTwo._8),
            quad("8_3", PowerOfTwo._8),
            quad("8_4", PowerOfTwo._8)
          )

          val res = quads.foldLeft(initial)((a, b) => TextureAtlasFunctions.mergeTreeBIntoA(a, b).get)

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

          res ==> expected
        }

        "should be able to fill a small tree (B)" - {

          val quad = (id: String, size: PowerOfTwo) => AtlasQuadNode(size, AtlasTexture(ImageRef(id, 1, 1)))

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

          res ==> expected
        }

        "should be able to create a texture map of a small tree" - {

          val quad = (id: String, size: PowerOfTwo) => AtlasQuadNode(size, AtlasTexture(ImageRef(id, 1, 1)))

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

          actual ==> expected

          actual match {
            case node: AtlasQuadNode =>
              val textureMap = node.toTextureMap

              textureMap.size === PowerOfTwo._16 ==> true

              textureMap.textureCoords.find(_.imageRef.name == "8_1").map(_.coords) === Some(Point(0, 0)) ==> true
              textureMap.textureCoords.find(_.imageRef.name == "8_2").map(_.coords) === Some(Point(8, 0)) ==> true
              textureMap.textureCoords.find(_.imageRef.name == "8_3").map(_.coords) === Some(Point(0, 8)) ==> true
              textureMap.textureCoords.find(_.imageRef.name == "8_4").map(_.coords) === Some(Point(8, 8)) ==> true

            case _ =>
              fail("error")
          }
        }

      }

    }
}
