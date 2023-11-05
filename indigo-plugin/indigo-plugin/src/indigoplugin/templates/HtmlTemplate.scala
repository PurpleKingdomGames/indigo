package indigoplugin.templates

object HtmlTemplate {

  def template(
      title: String,
      showCursor: Boolean,
      scriptName: String,
      backgroundColor: String
  ): String =
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
      |        background-color: $backgroundColor;
      |      }
      |      #indigo-container {
      |        display: flex;
      |        align-items: center;
      |        justify-content: center;
      |        padding:0px;
      |        margin:0px;
      |        width: 100vw;
      |        height: 100vh;
      |      }
      |      #indigo-container canvas {
      |        ${if (!showCursor) "cursor: none;" else ""}
      |      }
      |    </style>
      |  </head>
      |  <body>
      |    <!-- This div's id is hardcoded, and several parts of this reference implementation look for it. -->
      |    <div id="indigo-container"></div>
      |    <script type="text/javascript" src="scripts/indigo-support.js"></script>
      |    <script type="text/javascript" src="scripts/$scriptName"></script>
      |    <script type="text/javascript">
      |      IndigoGame.launch("indigo-container", {"width": window.innerWidth.toString(), "height": window.innerHeight.toString()})
      |    </script>
      |    <script src="cordova.js"></script> <!-- only needed for Cordova builds -->
      |  </body>
      |</html>
    """.stripMargin

}
