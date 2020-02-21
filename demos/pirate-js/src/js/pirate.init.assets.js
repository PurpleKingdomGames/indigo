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
        this.fontKey       = FontKey("boxy font");
        this.FontInfo = (new FontInfo(this.fontKey, this.smallFontName, 320, 230, FontChar("?", 47, 26, 11, 12)))
            .addChar(FontChar("A", 2, 39, 10, 12))
            .addChar(FontChar("B", 14, 39, 9, 12))
            .addChar(FontChar("C", 25, 39, 10, 12))
            .addChar(FontChar("D", 37, 39, 9, 12))
            .addChar(FontChar("E", 49, 39, 9, 12))
            .addChar(FontChar("F", 60, 39, 9, 12))
            .addChar(FontChar("G", 72, 39, 9, 12))
            .addChar(FontChar("H", 83, 39, 9, 12))
            .addChar(FontChar("I", 95, 39, 5, 12))
            .addChar(FontChar("J", 102, 39, 9, 12))
            .addChar(FontChar("K", 113, 39, 10, 12))
            .addChar(FontChar("L", 125, 39, 9, 12))
            .addChar(FontChar("M", 136, 39, 13, 12))
            .addChar(FontChar("N", 2, 52, 11, 12))
            .addChar(FontChar("O", 15, 52, 10, 12))
            .addChar(FontChar("P", 27, 52, 9, 12))
            .addChar(FontChar("Q", 38, 52, 11, 12))
            .addChar(FontChar("R", 51, 52, 10, 12))
            .addChar(FontChar("S", 63, 52, 9, 12))
            .addChar(FontChar("T", 74, 52, 11, 12))
            .addChar(FontChar("U", 87, 52, 10, 12))
            .addChar(FontChar("V", 99, 52, 9, 12))
            .addChar(FontChar("W", 110, 52, 13, 12))
            .addChar(FontChar("X", 125, 52, 9, 12))
            .addChar(FontChar("Y", 136, 52, 11, 12))
            .addChar(FontChar("Z", 149, 52, 10, 12))
            .addChar(FontChar("0", 2, 13, 10, 12))
            .addChar(FontChar("1", 13, 13, 7, 12))
            .addChar(FontChar("2", 21, 13, 9, 12))
            .addChar(FontChar("3", 33, 13, 9, 12))
            .addChar(FontChar("4", 44, 13, 9, 12))
            .addChar(FontChar("5", 56, 13, 9, 12))
            .addChar(FontChar("6", 67, 13, 9, 12))
            .addChar(FontChar("7", 79, 13, 9, 12))
            .addChar(FontChar("8", 90, 13, 10, 12))
            .addChar(FontChar("9", 102, 13, 9, 12))
            .addChar(FontChar("?", 47, 26, 11, 12))
            .addChar(FontChar("!", 2, 0, 6, 12))
            .addChar(FontChar(".", 143, 0, 6, 12))
            .addChar(FontChar(",", 124, 0, 8, 12))
            .addChar(FontChar("-", 133, 0, 9, 12))
            .addChar(FontChar(" ", 112, 13, 8, 12))
            .addChar(FontChar("[", 2, 65, 7, 12))
            .addChar(FontChar("]", 21, 65, 7, 12))
            .addChar(FontChar("(", 84, 0, 7, 12))
            .addChar(FontChar(")", 93, 0, 7, 12))
            .addChar(FontChar("\\", 11, 65, 8, 12))
            .addChar(FontChar("/", 150, 0, 9, 12))
            .addChar(FontChar(":", 2, 26, 5, 12))
            .addChar(FontChar("@", 60, 26, 11, 12))
            .addChar(FontChar("_", 42, 65, 9, 12))
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
Assets.assets = (
    Static.assets +
    Sounds.assets +
    Fonts.assets
);