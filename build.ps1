function build {
    param (
        [string]$ProjectPath
    )

    echo ">>> $ProjectPath"
    Set-Location -Path $ProjectPath -PassThru
    & .\build.ps1
    Set-Location -Path .. -PassThru
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

build "indigo-plugin"
build "sbt-indigo"
build "mill-indigo"
build "indigo"