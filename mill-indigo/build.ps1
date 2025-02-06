function RunMill {
    .\millw.bat @Args
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

RunMill -i clean
RunMill -i clean mill-indigo[2.13]
RunMill -i mill-indigo[2.13].compile
RunMill -i mill-indigo[2.13].test
RunMill -i mill-indigo[2.13].checkFormat
RunMill -i mill-indigo[2.13].publishLocal
