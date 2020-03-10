'use strict';
class Model {
    navRegion             = new Rectangle(137, 0, 338, 272)
    beat                  = 250

    static initialModel(screenDimensions) {
        return new Model(PirateState.Falling, true, Point(screenDimensions.horizontalCenter, 0), 0, 0);
    }

    static update(gameTime, model, inputState, screenDimensions) {
        return function(event) {
            switch (event.eventType) {
                case "frameTick":
                    if (model.pirateIsSafe && model.position.y == Model.navRegion.bottom - 1)
                        return Model.convertStateToModel(gameTime, new InputMapper(inputState), model, screenDimensions);
                    else
                        return Model.convertStateToModel(gameTime, model.pirateState, model, screenDimensions)

                default:
                    return new Outcome(model)
            }
        }
    }

    static convertStateToModel(gameTime, nextState, model, screenDimensions) {
        const walkDistancePerSecond = 128
        const fallDistancePerSecond = 300
        const walkSpeed = parseInt(walkDistancePerSecond.toDouble * gameTime.delta.value);
        const fallSpeed = parseInt(fallDistancePerSecond.toDouble * gameTime.delta.value);

        switch (nextState) {
            // Landed
            case PirateState.Falling:
                if (model.pirateIsSafe && model.position.y + fallSpeed >= Model.navRegion.bottom)
                    return Outcome(model.copy(pirateState = PirateState.Idle, position = Point(model.position.x, Model.navRegion.bottom - 1)));
                else if (model.position.y > screenDimensions.height + 50)
                    return Outcome(model.copy(position = Point(screenDimensions.horizontalCenter, 20), lastRespawn = gameTime.running))
                            .addGlobalEvents(PlaySound(Assets.Sounds.respawnSound, Volume.Max));
                else
                    // Otherwise, fall normally
                    return new Outcome(model.copy(position = model.position + Point(0, fallSpeed)));

            // Move left while on the ground
            case PirateState.MoveLeft:
                if (model.pirateIsSafe)
                {
                    const soundUpdate = updateWalkSound(gameTime, model);
                    return Outcome(
                        model.copy(
                            pirateState = nextState,
                            facingRight = false,
                            position = model.position - Point(walkSpeed, 0),
                            soundLastPlayed = soundUpdate.soundLastPlayed
                        )
                    ).addGlobalEvents(soundUpdate.walkingSound);

                }
                else
                    // Moving left but not safe = Falling
                    return new Outcome(
                        model.copy(
                            pirateState = PirateState.Falling,
                            facingRight = false
                        )
                    )

            // Move left while on the ground
            case PirateState.MoveRight:
                if (model.pirateIsSafe)
                {
                    const soundUpdate = updateWalkSound(gameTime, model);

                    return new Outcome(
                        model.copy(
                            pirateState = nextState,
                            facingRight = true,
                            position = model.position + Point(walkSpeed, 0),
                            soundLastPlayed = soundUpdate.soundLastPlayed
                        )
                    ).addGlobalEvents(soundUpdate.walkingSound)
                }
                else
                    // Moving right but not safe = Falling
                    return new Outcome(
                        model.copy(
                        pirateState = PirateState.Falling,
                        facingRight = true
                        )
                    )

            // Reset state
            case PirateState.Idle:
                return new Outcome(model.copy(pirateState = nextState))
        }
    }

    updateWalkSound = function(gameTime, model) {
        if (gameTime.running > model.soundLastPlayed + Model.beat)
            return ([PlaySound(Assets.Sounds.walkSound, Volume(0.5))], gameTime.running)
        else
            return (null, model.soundLastPlayed)
    }
}
