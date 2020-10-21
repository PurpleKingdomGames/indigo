# The Jobs System

## Motivation

You'd like to organize large numbers of NPCs to work together as a community. A common use case is a town building game where you need to NPCs to carry materials to building sites, and other NPCs to use the materials to construct buildings, and yet more NPCs to work within them to produce goods which must be carried off and so on and so on.

## Components and Concepts

1. Actor - Any entity that can do work (proof of which is the presence of an implicit Worker instance)
2. Job Market - the job market manages the global pool of available jobs waiting for a worker to carry them out.
3. Jobs - individual tasks to be carried out, but impose no requirements on how the work is carried out.
4. Work Schedules - each actor has a work schedule that manages their outstanding work load and tries to keep them busy.
5. Workers - An ad-hoc (typeclass) description of how any specific Actor would carry out work, and what type of work they do.

## Workers and jobs as an abstract concept

The Worker typeclass, in conjunction with a work schedule for any given actor, not only tries to find and accomplish tasks, but also generates work during down time. An easy to grasp idea of what this might be is a shepherd with no sheep, generating "jobs" to keep himself looking busy - wandering around, finding food, lamenting his lack of sheep etc.

More abstractly, anything that could be a work/task/job emitter could be a "worker". An apple tree won't get up and go fishing, but it can drop fruit and with it post a job to the job market for someone to come and collect it. Of course, you don't need a worker to post jobs, I mention that only to avoid confusion as the example code here uses Trees that are not workers!

## Our example

The example code here is a crude portrayal of a lumberjack, called Bob.

At the beginning of the demo Bob has nothing to do because the trees are growing (at slightly different rates). While he's waiting, Bob wanders around a bit. When each tree hit maturity, it posts a job to the Job Market to come and chop the tree down. _Anyone_ can pick up that message as long as their worker instance says they know how to do it, in our demo the only candidate is Bob.

Bob's work schedule automatically checks the Job Market when Bob has nothing to do. If it finds nothing, Bob makes his own entertainment.

When Bob is allocated a "chop me down" job, he walks to the trees location and _instantly_ fells said tree producing two perfectly piled up wood piles, which he picks up and secrets about his person - who knows where he's stashed them - but the counter goes up to show they're in his inventory. Somewhere.

The produced wood was also a job on the job market, but those jobs had a higher priority value that chopping down the trees, so Bob did those jobs first. Otherwise he'd have felled the whole grove and left wood lying around all over the place until the end when he'd have to walk around again and pick them all up.

Bob is allowed to be idle a few times in a row while his work schedule looks for work (takes 1 frame), but eventually he'll wander off again.

Bob will continue to fell the trees and collect the wood until there is nothing left. He'll then mournfully wander the now barren grasslands wishing there were more trees.
