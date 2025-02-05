function mill {
    param (
        [string]$Command
    )

    .\millw.bat -i $Command
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

mill clean
mill clean mill-indigo[2.13]
mill mill-indigo[2.13].compile
mill mill-indigo[2.13].test
mill mill-indigo[2.13].checkFormat
mill mill-indigo[2.13].publishLocal
