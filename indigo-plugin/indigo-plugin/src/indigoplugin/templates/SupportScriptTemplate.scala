package indigoplugin.templates

object SupportScriptTemplate {

  def template(autoSize: Boolean): String =
    s"""
      |// Shamelessly borrowed from: https://davidwalsh.name/javascript-debounce-function
      |function debounce(func, wait, immediate) {
      |  var timeout;
      |  return function() {
      |    var context = this, args = arguments;
      |    var later = function() {
      |      timeout = null;
      |      if (!immediate) func.apply(context, args);
      |    };
      |    var callNow = immediate && !timeout;
      |    clearTimeout(timeout);
      |    timeout = setTimeout(later, wait);
      |    if (callNow) func.apply(context, args);
      |  };
      |};
      |
      |
      |function resizeCanvas() {
      |  var c = document.getElementById("indigo");
      |  c.height = window.innerHeight;
      |  c.width = window.innerWidth;
      |}
      |
      |${if(autoSize) "" else "// "}window.onresize = debounce(resizeCanvas, 500);
      |
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
    """.stripMargin

}
