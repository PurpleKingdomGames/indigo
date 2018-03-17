package com.purplekingdomgames.indigo.networking

import com.purplekingdomgames.indigo.gameengine.events.GlobalEventStream
import com.purplekingdomgames.indigo.gameengine.events.HttpEvent.{HttpError, HttpResponse}
import org.scalajs.dom
import org.scalajs.dom.XMLHttpRequest

object Http {

  def doGet(url: String): Unit = {

    try {

      val xhr = new XMLHttpRequest

      xhr.open("GET", url)

      xhr.onload = (_: dom.Event) => {
        GlobalEventStream.push(HttpResponse(xhr.status))
      }

      xhr.onerror = (_: dom.Event) => {
        GlobalEventStream.push(HttpError)
      }

      xhr.send()

    } catch {
      case t: Throwable =>
        GlobalEventStream.push(HttpError)
    }
  }

}
