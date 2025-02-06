function RunMill {
    .\millw.bat @Args
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

Remove-Item -Recurse -Force -Path .\out

RunMill -i clean
RunMill -i clean indigo-plugin[2.12]
RunMill -i clean indigo-plugin[2.13]
RunMill -i indigo-plugin[2.12].compile
RunMill -i indigo-plugin[2.13].compile
RunMill -i indigo-plugin[2.12].test
RunMill -i indigo-plugin[2.13].test
RunMill -i indigo-plugin[2.12].checkFormat
RunMill -i indigo-plugin[2.13].checkFormat
RunMill -i indigo-plugin[2.12].publishLocal
RunMill -i indigo-plugin[2.13].publishLocal