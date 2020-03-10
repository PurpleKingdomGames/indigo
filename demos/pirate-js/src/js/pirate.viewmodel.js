'use strict';
class ViewModel {
    backTallPalm;
    waterReflections;
    flag;
    captain;
    helm;

    /*constructor (waterReflections, flag, captain, helm, palm) {
        this.waterReflections = waterReflections;
        this.flag = flag;
        this.captain = captain;
        this.helm = help;
        this.palm = palm;

        this. backTallPalm = palm
            .withBindingKey(BindingKey("Back Tall Palm"))
            .withAnimationKey(AnimationKey("P Back Tall"))
            .withDepth(Depth(10))
        ;
    }*/

    static initialViewModel(startupData, screenDimensions) {
        return new ViewModel(
            /*startupData.waterReflections
                .withRef(85, 0)
                .moveTo(screenDimensions.horizontalCenter, screenDimensions.verticalCenter + 5),
            startupData.flag.withRef(22, 105).moveTo(200, 270),
            startupData.captain.withRef(37, 63).moveTo(300, 271),
            startupData.helm.moveTo(605, 137).withRef(31, 49),
            startupData.palm*/
        );
    }
}