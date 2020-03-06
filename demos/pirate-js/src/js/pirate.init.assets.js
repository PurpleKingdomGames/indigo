class StaticAssets {
    constructor() {
        this.backgroundRef = "background"
        this.levelRef      = "level";
        this.levelGraphic  = Graphic(Rectangle(0, 0, 646, 374), 2, 0, 1, 1, this.levelRef, new Point(0, 0), Rectangle(0, 0, 646, 374), EffectsHelper.None);
        this.chestRef      = "Chest Close 01";
        this.chestGraphic  = Graphic(Rectangle(0, 0, 64, 35), 4, 0, 1, 1, this.chestRef, new Point(0, 0), Rectangle(0, 0, 646, 374), EffectsHelper.None).withRef(33, 34);
        this.assets =  [
            AssetType.Image(this.backgroundRef, "assets/bg.png"),
            AssetType.Image(this.chestRef, "assets/" + this.chestRef + ".png"),
            AssetType.Image(this.levelRef, "assets/level.png")
        ];
    }
}

class SoundAssets {
    constructor() {
        this.shanty        = "shanty";
        this.walkSound     = "walk";
        this.respawnSound  = "respawn";

        this.assets = [
            AssetType.Audio(this.shanty, "assets/bgmusic.mp3"),
            AssetType.Audio(this.walkSound, "assets/walk.mp3"),
            AssetType.Audio(this.respawnSound, "assets/respawn.mp3")
        ];
    }
}

class FontAssets {
    constructor() {
        this.smallFontName = "smallFontName";
        this.fontKey       = "boxy font";
        this.FontInfo = (new FontInfo(this.fontKey, this.smallFontName, 320, 230, FontChar("?", new Rectangle(47, 26, 11, 12)), [], false))
            .addChar(FontChar("A", new Rectangle(2, 39, 10, 12)))
            .addChar(FontChar("B", new Rectangle(14, 39, 9, 12)))
            .addChar(FontChar("C", new Rectangle(25, 39, 10, 12)))
            .addChar(FontChar("D", new Rectangle(37, 39, 9, 12)))
            .addChar(FontChar("E", new Rectangle(49, 39, 9, 12)))
            .addChar(FontChar("F", new Rectangle(60, 39, 9, 12)))
            .addChar(FontChar("G", new Rectangle(72, 39, 9, 12)))
            .addChar(FontChar("H", new Rectangle(83, 39, 9, 12)))
            .addChar(FontChar("I", new Rectangle(95, 39, 5, 12)))
            .addChar(FontChar("J", new Rectangle(102, 39, 9, 12)))
            .addChar(FontChar("K", new Rectangle(113, 39, 10, 12)))
            .addChar(FontChar("L", new Rectangle(125, 39, 9, 12)))
            .addChar(FontChar("M", new Rectangle(136, 39, 13, 12)))
            .addChar(FontChar("N", new Rectangle(2, 52, 11, 12)))
            .addChar(FontChar("O", new Rectangle(15, 52, 10, 12)))
            .addChar(FontChar("P", new Rectangle(27, 52, 9, 12)))
            .addChar(FontChar("Q", new Rectangle(38, 52, 11, 12)))
            .addChar(FontChar("R", new Rectangle(51, 52, 10, 12)))
            .addChar(FontChar("S", new Rectangle(63, 52, 9, 12)))
            .addChar(FontChar("T", new Rectangle(74, 52, 11, 12)))
            .addChar(FontChar("U", new Rectangle(87, 52, 10, 12)))
            .addChar(FontChar("V", new Rectangle(99, 52, 9, 12)))
            .addChar(FontChar("W", new Rectangle(110, 52, 13, 12)))
            .addChar(FontChar("X", new Rectangle(125, 52, 9, 12)))
            .addChar(FontChar("Y", new Rectangle(136, 52, 11, 12)))
            .addChar(FontChar("Z", new Rectangle(149, 52, 10, 12)))
            .addChar(FontChar("0", new Rectangle(2, 13, 10, 12)))
            .addChar(FontChar("1", new Rectangle(13, 13, 7, 12)))
            .addChar(FontChar("2", new Rectangle(21, 13, 9, 12)))
            .addChar(FontChar("3", new Rectangle(33, 13, 9, 12)))
            .addChar(FontChar("4", new Rectangle(44, 13, 9, 12)))
            .addChar(FontChar("5", new Rectangle(56, 13, 9, 12)))
            .addChar(FontChar("6", new Rectangle(67, 13, 9, 12)))
            .addChar(FontChar("7", new Rectangle(79, 13, 9, 12)))
            .addChar(FontChar("8", new Rectangle(90, 13, 10, 12)))
            .addChar(FontChar("9", new Rectangle(102, 13, 9, 12)))
            .addChar(FontChar("?", new Rectangle(47, 26, 11, 12)))
            .addChar(FontChar("!", new Rectangle(2, 0, 6, 12)))
            .addChar(FontChar(".", new Rectangle(143, 0, 6, 12)))
            .addChar(FontChar(",", new Rectangle(124, 0, 8, 12)))
            .addChar(FontChar("-", new Rectangle(133, 0, 9, 12)))
            .addChar(FontChar(" ", new Rectangle(112, 13, 8, 12)))
            .addChar(FontChar("[", new Rectangle(2, 65, 7, 12)))
            .addChar(FontChar("]", new Rectangle(21, 65, 7, 12)))
            .addChar(FontChar("(", new Rectangle(84, 0, 7, 12)))
            .addChar(FontChar(")", new Rectangle(93, 0, 7, 12)))
            .addChar(FontChar("\\",new Rectangle( 11, 65, 8, 12)))
            .addChar(FontChar("/", new Rectangle(150, 0, 9, 12)))
            .addChar(FontChar(":", new Rectangle(2, 26, 5, 12)))
            .addChar(FontChar("@", new Rectangle(60, 26, 11, 12)))
            .addChar(FontChar("_", new Rectangle(42, 65, 9, 12)))
        ;

        this.assets = [
            AssetType.Image(this.smallFontName, "assets/boxy_font_small.png")
        ];
    }
}

class Assets {
}

Assets.Static = new StaticAssets();
Assets.Sounds = new SoundAssets();
Assets.Fonts = new FontAssets();
Assets.assets = []
    .concat(Assets.Static.assets)
    .concat(Assets.Sounds.assets)
    .concat(Assets.Fonts.assets)
;