cd -Path snake -PassThru
mill -i clean
mill -i snake.test
mill -i snake.fastOpt
mill -i snake.indigoBuild
cd -Path .. -PassThru

cd -Path pirate -PassThru
sbt test buildGame
cd -Path .. -PassThru
