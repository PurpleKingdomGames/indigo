
The snake has to:
- Operate on a fixed grid size
- Advance forward
- Remember where it's tail is including corners
- Be able to turn left and right
- Operate on a grid
- Grow
- Collide with walls
- Collide with itself
- Collide with / pick up collectibles
- Wrap the world if no wall present

Movement is either:
1. Advance head and remove last.
2. Moving everything into the next space.

Version 1 has advantages in that it's easier, more efficient, makes growing trivial, also "remembering" the course is not a thing. Drawbacks are that you can't control individual segments and you can't see movement because you're locked to the grid.

Version 2 is more complicated but more flexible.

Sneaky option 3 is to say that v1 gives you the path, and that we can split the path creation from the rendering. Yes. That one.