# Technical debt / Things I think should be different

SubSystems:
- AudioPlayer should be a subsystem
- Networking should be subsystem
- AnimationsRegister should be a subsystem

Core overhaul thoughts:
I think ideally it should be more like discrete form of push based AFRP (Arrowised FRP). If that's a thing.
Push based means the tick is forced upon the system ready or not.
Discrete timing means, again, it runs at regular intervals dictated by the environment.
Arrows are, I think, essentially Monads that carry point-free combinators and have functions to combine them in interesting ways, where you essentially run everything at the end.
A proper FRP system would take it's next discrete update (from a signal or stream) and produce the output to another signal or stream. In our case that output would be renderer agnostic and easy to test. We would then pick up the output and draw it.
Is the "basic" template in fact a finally tagless style interface? It does lack the Functor, but essentially you are providing an interpreter to our program...
Does all this mean that IO should be an Arrow that maps an input event stream into a rendered output stream? Possibly! Otherwise is it a reader writer state monad? Increasingly I think not.

State:
There's a big lump of state stored in very few var's at the moment, and they have no possibility of history. Based on the above, we might be able to move to streams / signals and handle the state outside of the update process.

Performance:
Look at either a scala.js benchmarking thing or maybe JMH?

Refined types:
The exciting thing about refined types is that they remove the need for some runtime validations thus speeding up your code. Worth a look, especially at the core structures level.

Law checking:
Should our fundamental structures be lawful? Would we get better guarantees around behaviour?

Data structures:
It's probably worth doing some serious head scratching around our core data structures like QuadTrees. Should they be built more like "hickey tries" (pronounced trees)? Where do we want performance? Read? Write? Concat? Should do the performance / benchmarking work above first, but also, what about stack size? At the moment the structures are nested...

