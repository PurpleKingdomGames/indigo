package indigoplugin.templates

object HtmlTemplate {

  def template(title: String, showCursor: Boolean, scriptName: String): String =
    s"""<!DOCTYPE html>
      |<html>
      |  <head>
      |    <meta http-equiv="Content-Security-Policy" content="script-src 'self' 'unsafe-inline'">
      |    <meta charset="UTF-8">
      |    <title>$title</title>
      |    <style>
      |      body {
      |        padding:0px;
      |        margin:0px;
      |        overflow-x: hidden;
      |        overflow-y: hidden;
      |      }
      |      #indigo-container {
      |        padding:0px;
      |        margin:0px;
      |      }
      |
      |      ${if (!showCursor) "canvas { cursor: none }" else ""}
      |    </style>
      |  </head>
      |  <body>
      |    <script type="text/javascript">
      |window.onload = function () {
      |    if (typeof history.pushState === "function") {
      |        history.pushState("jibberish", null, null);
      |        window.onpopstate = function () {
      |            history.pushState('newjibberish', null, null);
      |            // Handle the back (or forward) buttons here
      |            // Will NOT handle refresh, use onbeforeunload for this.
      |        };
      |    }
      |    else {
      |        var ignoreHashChange = true;
      |        window.onhashchange = function () {
      |            if (!ignoreHashChange) {
      |                ignoreHashChange = true;
      |                window.location.hash = Math.random();
      |                // Detect and redirect change here
      |                // Works in older FF and IE9
      |                // * it does mess with your hash symbol (anchor?) pound sign
      |                // delimiter on the end of the URL
      |            }
      |            else {
      |                ignoreHashChange = false;
      |            }
      |        };
      |    }
      |}
      |    </script>
      |    <div id="indigo-container"></div>
      |    <script type="text/javascript" src="scripts/$scriptName"></script>
      |    <script type="text/javascript">
      |      IndigoGame.launch({"width": window.innerWidth.toString(), "height": window.innerHeight.toString()})
      |    </script>
      |  </body>
      |</html>
    """.stripMargin

}
