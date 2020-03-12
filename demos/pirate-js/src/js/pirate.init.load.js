
class InitialLoad {

    static setup(assetCollection) {
        const loader      = InitialLoad.loadAnimation(assetCollection);
        const reflections = loader(Assets.Water.jsonRef, Assets.Water.ref, 20);
        const flag        = loader(Assets.Flag.jsonRef, Assets.Flag.ref, 10);
        const captain     = loader(Assets.Captain.jsonRef, Assets.Captain.ref, 2);
        const helm        = loader(Assets.Helm.jsonRef, Assets.Helm.ref, 9);
        const palm        = loader(Assets.Trees.jsonRef, Assets.Trees.ref, 1);

        return InitialLoad.makeStartupData(reflections, flag, captain, helm, palm)
    }

    static loadAnimation(assetCollection) {
        return function(jsonRef, name, depth) {
            const json     = assetCollection.findTextDataByName(jsonRef);
            const aseprite = Json.asepriteFromJson(json);

            return AsepriteConverter.toSpriteAndAnimations(aseprite, depth, name);
        }
    }

    static makeStartupData(waterReflections, flag, captain, helm, palm) {
        return Startup
            .Success(
                new StartupData(waterReflections.sprite, flag.sprite, captain.sprite, helm.sprite, palm.sprite)
            )
            .addAnimations(
                waterReflections.animations,
                flag.animations,
                captain.animations,
                helm.animations,
                palm.animations,
                palm.animations.withAnimationKey("P Back Tall")
            )
    }
}

class StartupData {
    waterReflections;
    flag;
    captain;helm;
    palm;

    constructor(waterReflections, flag, captain, helm, palm) {
        this.waterReflections = waterReflections;
        this.flag = flag;
        this.captain = captain;
        this.helm = helm;
        this.palm = palm;
    }
}
