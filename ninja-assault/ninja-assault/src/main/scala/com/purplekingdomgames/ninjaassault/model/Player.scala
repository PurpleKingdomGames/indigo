package com.purplekingdomgames.ninjaassault.model

sealed trait Player
case object CPU   extends Player
case object Human extends Player
