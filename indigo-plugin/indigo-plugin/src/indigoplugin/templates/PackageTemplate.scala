package indigoplugin.templates

object PackageTemplate {

  lazy val template: String =
    """
{
  "name": "indigo-runner",
  "version": "1.0.0",
  "description": "Indigo Runner",
  "main": "main.js",
  "scripts": {
    "start": "electron ."
  },
  "repository": "",
  "author": "Purple Kingdom Games",
  "license": "MIT"
}
    """

}
