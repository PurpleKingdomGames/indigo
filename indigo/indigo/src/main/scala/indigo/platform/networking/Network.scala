package indigo.platform.networking

import org.scalajs.dom.window

object Network:
  /** Whether the network is online or not. A network is considered online if there is access to the local network only.
    * As such, an online network is not a guarantee that the internet or indeed a single resource on the internet is
    * available
    */
  def isOnline = window.navigator.onLine
