Set-Location -Path indigo-plugin -PassThru
& .\build.ps1
Set-Location -Path .. -PassThru

Set-Location -Path sbt-indigo -PassThru
& .\build.ps1
Set-Location -Path .. -PassThru

Set-Location -Path mill-indigo -PassThru
& .\build.ps1
Set-Location -Path .. -PassThru

Set-Location -Path indigo -PassThru
& .\build.ps1
Set-Location -Path .. -PassThru
