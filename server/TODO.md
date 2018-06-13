
## Server TODO's
We have a basic service, it needs to be extended to:

Phase 1: Serve a game
- //Serve static config
- //Serve static assets list
- //Serve the assets listed
- //Serve a static game definition
- On request to /play
  - serve up an html page with the framework embedded
  - framework must be configured to look for resources from remote
  - game should load
  
Phase 2: Move static to DB
- GameConfig to come from lookup
- Assets to come from lookup
- GameDefinition to come from lookup
- Assets to come from some sort of storage

Phase 3: Terrible login
- Just enough so we can have different user spaces during dev.

Phase 4: Viewing the data
- Can see game config
- Can see assets
- Can see a scene list

Phase 4: Configurable basics
- Choose a game to work on
- Configure and save gameconfig
- Upload new assets
- Serve assets list based on uploaded assets (for now)

Phase 5: New Game
- Ability to add a new game
- Auto add default config
- Auto add empty assets
- Auto add empty GameDefinition

Phase 6: Scene Manager
- Can add new scenes
- Can edit scene details
- Can reorder scenes

Phase 7: Level Editor
Erm...?