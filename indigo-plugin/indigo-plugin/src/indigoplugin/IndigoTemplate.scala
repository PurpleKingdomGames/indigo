package indigoplugin

/** Tells Indigo whether to generate the default static site for your game, or use a custom provided template instead.
  */
sealed trait IndigoTemplate
object IndigoTemplate {

  /** Use the detault static site template */
  case object Default extends IndigoTemplate

  /** Use a custom provided static site template */
  case class Custom(inputs: Inputs, outputs: Outputs) extends IndigoTemplate

  /** Input parameters for a custom static template
    *
    * @param templateSource
    *   The directory holding all the files and folders to be copied across to use in the template.
    */
  final case class Inputs(templateSource: os.Path)

  /** Output parameters for a custom static template
    *
    * @param assets
    *   The directory to copy assets into. This directory must exist in the folder specified in `Inputs#templateSource`.
    * @param gameScripts
    *   The directory to copy the compiled game script files into. This directory must exist in the folder specified in
    *   `Inputs#templateSource`.
    */
  final case class Outputs(
      assets: os.RelPath,
      gameScripts: os.RelPath
  )
}
