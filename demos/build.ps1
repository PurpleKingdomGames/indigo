cd -Path snake -PassThru
mill snake.test
mill snake.fastOpt
mill snake.indigoBuild
cd -Path .. -PassThru

cd -Path pirate -PassThru
sbt test buildGame
cd -Path .. -PassThru
