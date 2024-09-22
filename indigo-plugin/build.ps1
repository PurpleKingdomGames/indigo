
Remove-Item -Recurse -Force -Path .\out

.\millw.bat -i clean
.\millw.bat -i clean indigo-plugin[2.12]
.\millw.bat -i clean indigo-plugin[2.13]
.\millw.bat -i indigo-plugin[2.12].compile
.\millw.bat -i indigo-plugin[2.13].compile
.\millw.bat -i indigo-plugin[2.12].test
.\millw.bat -i indigo-plugin[2.13].test
.\millw.bat -i indigo-plugin[2.12].checkFormat
.\millw.bat -i indigo-plugin[2.13].checkFormat
.\millw.bat -i indigo-plugin[2.12].publishLocal
.\millw.bat -i indigo-plugin[2.13].publishLocal
