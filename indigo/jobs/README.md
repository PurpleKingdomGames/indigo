# The Jobs System

## Motivation

You'd like to organize large numbers of NPCs to work together as a community. A common use case is a town building game where you need to NPCs to carry materials to building sites, and other NPCs to use te materials to construct buildings, and yet more NPCs to work within them ot produce goods... which must be carried off and so on.

## Components and Concepts

1. Actor - Any entity that can do work
2. Job Market - the job market manages the global pool of available jobs waiting for a worker to carry them out.
3. Jobs - individual tasks to be carried out, but impose no requirements on how the work is carried out.
4. Work Schedules - each actor has a work schedule that manages their outstanding work load and tries to keep them busy.
5. Workers - An ad-hoc (typeclass) description of how any specific Actor would carry out work, and what type of work they do.

## Workers and jobs as an abstract concept

The Worker typeclass, in conjunction with a work schedule for any given actor, not only tries to find and accomplish tasks, but also generates work during down time. An easy to grasp idea of what this might be is a shepherd with no sleep, generating "jobs" to keep himself looking busy - wandering around, finding food, lamenting his lack of sheep etc.

Most abstractly, anything that could be a work/task/job emitter could be a "worker". An apple tree won't get up and go fishing, but it can drop fruit and with it post a job to the job market for someone to come and collect it.
