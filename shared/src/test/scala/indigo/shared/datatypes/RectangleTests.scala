package indigo.shared.datatypes

import utest._

object RectangleTests extends TestSuite {

  val tests: Tests =
    Tests {
      "creating rectangles" - {

        "should be able to construct a rectangle from two points" - {
          val pt1 = Point(5, 6)
          val pt2 = Point(1, 3)

          val expected = Rectangle(1, 3, 4, 3)

          Rectangle.fromTwoPoints(pt1, pt2) ==> expected
        }

      }

      "Expand to include two rectangles" - {

        "should return the original rectangle when it already encompasses the second one" - {
          val a = Rectangle(10, 20, 100, 200)
          val b = Rectangle(20, 20, 50, 50)

          Rectangle.expandToInclude(a, b) ==> a
        }

        "should expand to meet the bounds of both" - {
          val a = Rectangle(10, 10, 20, 20)
          val b = Rectangle(100, 100, 100, 100)

          Rectangle.expandToInclude(a, b) ==> Rectangle(10, 10, 190, 190)
        }

      }

      "intersecting rectangles" - {

        "should return true when two rectangles intersect" - {
          val a = Rectangle(10, 10, 25, 25)
          val b = Rectangle(20, 20, 50, 50)

          Rectangle.intersecting(a, b) ==> true
        }

        "should return false when they do not intersect" - {
          val a = Rectangle(10, 10, 20, 20)
          val b = Rectangle(100, 100, 100, 100)

          Rectangle.intersecting(a, b) ==> false
        }
      }

      "encompasing rectangles" - {
        "should return true when A encompases B" - {
          val a = Rectangle(10, 10, 110, 110)
          val b = Rectangle(20, 20, 10, 10)

          Rectangle.encompassing(a, b) ==> true
        }

        "should return false when A does not encompass B" - {
          val a = Rectangle(20, 20, 10, 10)
          val b = Rectangle(10, 10, 110, 110)

          Rectangle.encompassing(a, b) ==> false
        }

        "should return false when A and B merely intersect" - {
          val a = Rectangle(10, 10, 20, 200)
          val b = Rectangle(15, 15, 100, 10)

          Rectangle.encompassing(a, b) ==> false
        }
      }

      "overlapping rectangles" - {
        "should return true when A overlaps B" - {
          val a = Rectangle(10, 10, 20, 20)
          val b = Rectangle(15, 15, 100, 100)

          Rectangle.overlapping(a, b) ==> true
        }

        "should return false when A and B do not overlap" - {
          val a = Rectangle(10, 10, 20, 20)
          val b = Rectangle(100, 100, 100, 100)

          Rectangle.overlapping(a, b) ==> false
        }
      }

    }
}
