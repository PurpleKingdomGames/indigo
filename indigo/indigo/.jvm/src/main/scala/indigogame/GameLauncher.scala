package indigogame


trait GameLauncher {

  protected def ready(flags: Map[String, String]): Unit

  def launch(): Unit =
  ready(Map[String, String]())

  def launch(flags: Map[String, String]): Unit =
    ready(flags)

}
