class Model {
    navRegion             = new Rectangle(137, 0, 338, 272)
    beat                  = new Millis(250)
    walkDistancePerSecond = 128
    fallDistancePerSecond = 300

    initialModel = function(screenDimensions) {
        return new Model(PirateState.Falling, true, Point(screenDimensions.horizontalCenter, 0), Millis.zero, Millis.zero);
    }

    update = function (gameTime, model, inputState, screenDimensions) {
        return function(event) {
            switch (typeof(event)) {
                case FrameTick:
                    if (model.pirateIsSafe && model.position.y == Model.navRegion.bottom - 1)
                        return convertStateToModel(gameTime, InputMapper(inputState), model, screenDimensions);
                    else
                        return convertStateToModel(gameTime, model.pirateState, model, screenDimensions)

                default:
                    return Outcome(model)
            }
        }
    }

    convertStateToModel = function (gameTime, nextState, model, screenDimensions) {
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
                    return Outcome(model.copy(position = model.position + Point(0, fallSpeed)));

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
                    return Outcome(
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

                    return Outcome(
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
                    return Outcome(
                        model.copy(
                        pirateState = PirateState.Falling,
                        facingRight = true
                        )
                    )

            // Reset state
            case PirateState.Idle:
                return Outcome(model.copy(pirateState = nextState))
        }
    }

    updateWalkSound = function(gameTime, model) {
        if (gameTime.running > model.soundLastPlayed + Model.beat)
            return ([PlaySound(Assets.Sounds.walkSound, Volume(0.5))], gameTime.running)
        else
            return (null, model.soundLastPlayed)
    }
}
