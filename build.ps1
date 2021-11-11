cd -Path indigo-plugin -PassThru
& .\build.ps1
cd -Path .. -PassThru

cd -Path sbt-indigo -PassThru
& .\build.ps1
cd -Path .. -PassThru

cd -Path mill-indigo -PassThru
& .\build.ps1
cd -Path .. -PassThru

cd -Path indigo -PassThru
& .\build.ps1
cd -Path .. -PassThru

cd -Path examples -PassThru
& .\build.ps1
cd -Path .. -PassThru

cd -Path demos -PassThru
& .\build.ps1
cd -Path .. -PassThru
