package indigoplugin.utils

object AsciiLogo {

  val rawLogo: String =
    """
    |      //                  //  //
    |                         //
    |    //  //////      //////  //    ////      //////
    |   //  //    //  //    //  //  //    //  //    //
    |  //  //    //  ////////  //  ////////  //////
    |                                   //
    |                            //////
    |""".stripMargin

  val logo: String =
    rawLogo
      .flatMap(c =>
        if (c != ' ' && c != '\n') Console.MAGENTA + c.toString + Console.RESET
        else c.toString
      )
}
