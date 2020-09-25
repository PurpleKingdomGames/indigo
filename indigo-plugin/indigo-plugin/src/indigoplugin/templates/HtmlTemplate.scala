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
      |    <div id="indigo-container"></div>
      |    <script type="text/javascript" src="scripts/indigo-support.js"></script>
      |    <script type="text/javascript" src="scripts/$scriptName"></script>
      |    <script type="text/javascript">
      |      IndigoGame.launch({"width": window.innerWidth.toString(), "height": window.innerHeight.toString()})
      |    </script>
      |    <script src="cordova.js"></script> <!-- only needed for Cordova builds -->
      |  </body>
      |</html>
    """.stripMargin

}
