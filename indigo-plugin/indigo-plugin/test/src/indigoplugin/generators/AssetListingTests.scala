package indigoplugin.generators

class AssetListingTests extends munit.FunSuite {

  test("should be able to convert file and folder names into something safe (using default)") {
    def toSafeName(name: String) = AssetListing.toDefaultSafeName((n, _) => n)(name, "")

    assertEquals(toSafeName("hello"), "hello")
    assertEquals(toSafeName("hello-there-01"), "helloThere01")
    assertEquals(toSafeName("hello-there-01.jpg"), "helloThere01Jpg")
    assertEquals(toSafeName("^hello!there_0 1.jpg"), "helloThere01Jpg")
    assertEquals(toSafeName("00-hello"), "_00Hello")
  }

  test("It should be able to render a simple tree of assets") {

    val paths: List[os.RelPath] =
      List(
        os.RelPath.rel / "assets" / "some_text.txt",
        os.RelPath.rel / "assets" / "images" / "fancy logo!.svg"
      )

    val actual =
      AssetListing.renderContent(paths, AssetListing.toDefaultSafeName((n, _) => n))

    val expected =
      """
  object assets:
    object images:
      val fancyLogo: AssetName               = AssetName("fancy logo!.svg")
      val fancyLogoMaterial: Material.Bitmap = Material.Bitmap(fancyLogo)

      def assetSet(baseUrl: String): Set[AssetType] =
        Set(
          AssetType.Image(fancyLogo, AssetPath(baseUrl + "assets/images/fancy logo!.svg"), Option(AssetTag("images")))
        )
      def assetSet: Set[AssetType] = assetSet("./")

      def assetNameSet: Set[AssetName] =
        Set(
          fancyLogo
        )

    val someText: AssetName = AssetName("some_text.txt")

    def assetSet(baseUrl: String): Set[AssetType] =
      Set(
        AssetType.Text(someText, AssetPath(baseUrl + "assets/some_text.txt"))
      )
    def assetSet: Set[AssetType] = assetSet("./")

    def assetNameSet: Set[AssetName] =
      Set(
        someText
      )

      """.trim

    assertEquals(actual.trim, expected.trim)

  }

  test("It should be able to render a tree of asset details") {

    val paths: List[os.RelPath] =
      List(
        os.RelPath.rel / "assets" / "folderA" / "folderB" / "d.jpg",
        os.RelPath.rel / "assets" / "folderC" / "f.mp3",
        os.RelPath.rel / "assets" / "folderA" / "folderB" / "b.png",
        os.RelPath.rel / "assets" / "a.txt",
        os.RelPath.rel / "assets" / "folderA" / "folderB" / "c.png",
        os.RelPath.rel / "assets" / "folderA" / "e.svg"
      )

    val rename: (String, String) => String = {
      case ("e", "svg") => "ee"
      case (n, _)       => n
    }

    val actual =
      AssetListing.renderContent(paths, AssetListing.toDefaultSafeName(rename))

    val expected =
      """
  object assets:
    object folderA:
      object folderB:
        val b: AssetName               = AssetName("b.png")
        val bMaterial: Material.Bitmap = Material.Bitmap(b)
        val c: AssetName               = AssetName("c.png")
        val cMaterial: Material.Bitmap = Material.Bitmap(c)
        val d: AssetName               = AssetName("d.jpg")
        val dMaterial: Material.Bitmap = Material.Bitmap(d)

        def assetSet(baseUrl: String): Set[AssetType] =
          Set(
            AssetType.Image(b, AssetPath(baseUrl + "assets/folderA/folderB/b.png"), Option(AssetTag("folderB"))),
            AssetType.Image(c, AssetPath(baseUrl + "assets/folderA/folderB/c.png"), Option(AssetTag("folderB"))),
            AssetType.Image(d, AssetPath(baseUrl + "assets/folderA/folderB/d.jpg"), Option(AssetTag("folderB")))
          )
        def assetSet: Set[AssetType] = assetSet("./")

        def assetNameSet: Set[AssetName] =
          Set(
            b,
            c,
            d
          )

      val ee: AssetName               = AssetName("e.svg")
      val eeMaterial: Material.Bitmap = Material.Bitmap(ee)

      def assetSet(baseUrl: String): Set[AssetType] =
        Set(
          AssetType.Image(ee, AssetPath(baseUrl + "assets/folderA/e.svg"), Option(AssetTag("folderA")))
        )
      def assetSet: Set[AssetType] = assetSet("./")

      def assetNameSet: Set[AssetName] =
        Set(
          ee
        )

    object folderC:
      val f: AssetName            = AssetName("f.mp3")
      val fPlay: PlaySound        = PlaySound(f, Volume.Max)
      val fSceneAudio: SceneAudio = SceneAudio(SceneAudioSource(BindingKey("f.mp3"), PlaybackPattern.SingleTrackLoop(Track(f))))

      def assetSet(baseUrl: String): Set[AssetType] =
        Set(
          AssetType.Audio(f, AssetPath(baseUrl + "assets/folderC/f.mp3"))
        )
      def assetSet: Set[AssetType] = assetSet("./")

      def assetNameSet: Set[AssetName] =
        Set(
          f
        )

    val a: AssetName = AssetName("a.txt")

    def assetSet(baseUrl: String): Set[AssetType] =
      Set(
        AssetType.Text(a, AssetPath(baseUrl + "assets/a.txt"))
      )
    def assetSet: Set[AssetType] = assetSet("./")

    def assetNameSet: Set[AssetName] =
      Set(
        a
      )
      """.trim

    assertEquals(actual.trim, expected.trim)

  }

}
