package indigoplugin.generators

class AssetListingTests extends munit.FunSuite {

  test("toSafeName should be able to convert file and folder names into something safe") {
    assertEquals(AssetListing.toSafeName("hello"), "Hello")
    assertEquals(AssetListing.toSafeName("hello-there-01"), "HelloThere01")
    assertEquals(AssetListing.toSafeName("hello-there-01.jpg"), "HelloThere01Jpg")
    assertEquals(AssetListing.toSafeName("^hello!there_0 1.jpg"), "HelloThere01Jpg")
    assertEquals(AssetListing.toSafeName("00-hello"), "_00Hello")
  }

  test("It should be able to render some asset details") {

    val paths: List[os.RelPath] =
      List(
        os.RelPath.rel / "folderA" / "folderB" / "d.jpg",
        os.RelPath.rel / "folderC" / "f.mp3",
        os.RelPath.rel / "folderA" / "folderB" / "b.png",
        os.RelPath.rel / "a.txt",
        os.RelPath.rel / "folderA" / "folderB" / "c.png",
        os.RelPath.rel / "folderA" / "e.svg"
      )

    val actual =
      AssetListing.renderContent(paths)

    val expected =
      """
  object FolderA:
    object FolderB:
      val BName: AssetName           = AssetName("b.png")
      val BMaterial: Material.Bitmap = Material.Bitmap(BName)
      val CName: AssetName           = AssetName("c.png")
      val CMaterial: Material.Bitmap = Material.Bitmap(CName)
      val DName: AssetName           = AssetName("d.jpg")
      val DMaterial: Material.Bitmap = Material.Bitmap(DName)

      def assets(baseUrl: String): Set[AssetType] =
        Set(
          AssetType.Image(BName, AssetPath(baseUrl + "folderA/folderB/b.png"), Option(AssetTag("FolderB"))),
          AssetType.Image(CName, AssetPath(baseUrl + "folderA/folderB/c.png"), Option(AssetTag("FolderB"))),
          AssetType.Image(DName, AssetPath(baseUrl + "folderA/folderB/d.jpg"), Option(AssetTag("FolderB"))),
        )

    val EName: AssetName           = AssetName("e.svg")
    val EMaterial: Material.Bitmap = Material.Bitmap(EName)

    def assets(baseUrl: String): Set[AssetType] =
      Set(
        AssetType.Image(EName, AssetPath(baseUrl + "folderA/e.svg"), Option(AssetTag("FolderA"))),
      )

  object FolderC:
    val FName: AssetName        = AssetName("f.mp3")
    val FPlay: PlaySound        = PlaySound(FName, Volume.Max)
    val FSceneAudio: SceneAudio = SceneAudio(SceneAudioSource(BindingKey("f.mp3"), PlaybackPattern.SingleTrackLoop(Track(FName))))

    def assets(baseUrl: String): Set[AssetType] =
      Set(
        AssetType.Audio(FName, AssetPath(baseUrl + "folderC/f.mp3")),
      )

  val AName: AssetName  = AssetName("a.txt")

  def assets(baseUrl: String): Set[AssetType] =
    Set(
      AssetType.Text(AName, AssetPath(baseUrl + "a.txt")),
    )
      """.trim

    assertEquals(actual.trim, expected.trim)

  }

}
