package apigen

import utest._

object LineParserTests extends TestSuite {

  val tests: Tests =
    Tests {

      "grouping entities" - {

        val in: List[EntityDefinition] =
          List(
            ClassEntity("class1", Nil),
            ValueEntity("value1", "Int"),
            ValueEntity("value2", "Int"),
            MethodEntity("method1"),
            MethodEntity("method2"),
            MethodEntity("method3"),
            StaticEntity("static1", Nil),
            FunctionEntity("function1"),
            FunctionEntity("function2"),
            FunctionEntity("function3")
          )

        val expected: List[EntityDefinition] =
          List(
            ClassEntity(
              "class1",
              List(
                ValueEntity("value1", "Int"),
                ValueEntity("value2", "Int"),
                MethodEntity("method1"),
                MethodEntity("method2"),
                MethodEntity("method3")
              )
            ),
            StaticEntity(
              "static1",
              List(
                FunctionEntity("function1"),
                FunctionEntity("function2"),
                FunctionEntity("function3")
              )
            )
          )

        val actual =
          LineParser.groupMembers(in)

        actual ==> expected

      }

    }

}
