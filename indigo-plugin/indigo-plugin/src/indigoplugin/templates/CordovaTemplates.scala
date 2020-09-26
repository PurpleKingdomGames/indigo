package indigoplugin.templates

object CordovaTemplates {

  val packageFileTemplate: String =
    s"""
{
  "dependencies": {},
  "cordova": {
    "plugins": {
      "cordova-plugin-whitelist": {}
    },
    "platforms": [
      "ios",
      "electron",
      "browser"
    ]
  },
  "devDependencies": {
    "cordova-browser": "^6.0.0",
    "cordova-electron": "^1.1.1",
    "cordova-ios": "^6.1.1",
    "cordova-plugin-whitelist": "^1.3.4",
    "whitelist": "^1.0.2"
  }
}
    """

  def configFileTemplate(title: String): String =
    s"""
<?xml version='1.0' encoding='utf-8'?>
<widget id="com.your.game" version="0.0.1" xmlns="http://www.w3.org/ns/widgets" xmlns:cdv="http://cordova.apache.org/ns/1.0">
    <name>$title</name>
    <description>$title</description>
    <author email="yourgame@email.com">Your name</author>
    <content src="index.html" />
    <access origin="*" />
    <allow-navigation href="*"/>
    <allow-intent href="http://*/*" />
    <allow-intent href="https://*/*" />
    <allow-intent href="tel:*" />
    <allow-intent href="sms:*" />
    <allow-intent href="mailto:*" />
    <allow-intent href="geo:*" />

    <preference name="AutoHideSplashScreen" value="true" />

    <platform name="ios">
        <allow-intent href="app:*" />
        <allow-intent href="itms:*" />
        <allow-intent href="itms-apps:*" />
        <preference name="scheme" value="app" />
        <preference name="hostname" value="localhost" />
    </platform>

    <platform name="browser"></platform>

    <platform name="electron">
      <preference name="ElectronSettingsFilePath" value="res/electron/settings.json" />
    </platform>
</widget>
    """

  // the `+ 22` is for the window frame border...
  def electronSettingsFileTemplate(windowWidth: Int, windowHeight: Int): String =
    s"""
{
    "browserWindow": {
        "width": ${windowWidth.toString()},
        "height": ${(windowHeight + 22).toString()},
        "resizable": true,
        "fullscreen": false,
        "webPreferences": {
            "nodeIntegration": false
        }
    }
}
    """

}
