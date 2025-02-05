function mill {
    param (
        [string]$Command
    )

    .\millw.bat -i $Command
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

Remove-Item -Recurse -Force -Path .\out

mill clean
mill clean indigo-plugin[2.12]
mill clean indigo-plugin[2.13]
mill indigo-plugin[2.12].compile
mill indigo-plugin[2.13].compile
mill indigo-plugin[2.12].test
mill indigo-plugin[2.13].test
mill indigo-plugin[2.12].checkFormat
mill indigo-plugin[2.13].checkFormat
mill indigo-plugin[2.12].publishLocal
mill indigo-plugin[2.13].publishLocal