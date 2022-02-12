package indigoplugin.templates

object ElectronTemplates {

  def mainFileTemplate(windowWidth: Int, windowHeight: Int): String =
    s"""
// Modules to control application life and create native browser window
const {app, BrowserWindow} = require('electron')
const path = require('path')

function createWindow () {
  // Create the browser window.
  const mainWindow = new BrowserWindow({
    width: ${windowWidth.toString()},
    height: ${windowHeight.toString()},
    useContentSize: true,
    webPreferences: {
      worldSafeExecuteJavaScript: true,
      preload: path.join(__dirname, 'preload.js')
    }
  })

  // and load the index.html of the app.
  mainWindow.loadFile('index.html')

  // Open the DevTools.
  // mainWindow.webContents.openDevTools()
}

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.whenReady().then(() => {
  createWindow()
  
  app.on('activate', function () {
    // On macOS it's common to re-create a window in the app when the
    // dock icon is clicked and there are no other windows open.
    if (BrowserWindow.getAllWindows().length === 0) createWindow()
  })
})

// Quit when all windows are closed, except on macOS. There, it's common
// for applications and their menu bar to stay active until the user quits
// explicitly with Cmd + Q.
app.on('window-all-closed', function () {
  if (process.platform !== 'darwin') app.quit()
})

// In this file you can include the rest of your app's specific main process
// code. You can also put them in separate files and require them here.
    """

  def packageFileTemplate(disableFrameRateLimit: Boolean): String =
    s"""{
  "name": "indigo-runner",
  "version": "1.0.0",
  "description": "Indigo Runner",
  "main": "main.js",
  "scripts": {
    "start": "electron${if(disableFrameRateLimit) " --disable-frame-rate-limit" else ""} ."
  },
  "repository": "",
  "author": "Purple Kingdom Games",
  "license": "MIT"
}
    """

  lazy val preloadFileTemplate: String =
    """// All of the Node.js APIs are available in the preload process.
// It has the same sandbox as a Chrome extension.
window.addEventListener('DOMContentLoaded', () => {
  const replaceText = (selector, text) => {
    const element = document.getElementById(selector)
    if (element) element.innerText = text
  }

  for (const type of ['chrome', 'node', 'electron']) {
    replaceText(`${type}-version`, process.versions[type])
  }
})
    """

}
