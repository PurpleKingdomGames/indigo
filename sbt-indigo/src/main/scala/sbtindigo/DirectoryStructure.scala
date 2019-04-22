package sbtindigo

import sbt.File

final case class DirectoryStructure(base: File, assets: File, artefacts: File)