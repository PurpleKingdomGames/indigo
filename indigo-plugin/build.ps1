
mill -i clean
mill -i clean indigo-plugin[2.12]
mill -i clean indigo-plugin[2.13]
mill -i indigo-plugin[2.12].compile
mill -i indigo-plugin[2.13].compile
mill -i indigo-plugin[2.12].publishLocal
mill -i indigo-plugin[2.13].publishLocal
