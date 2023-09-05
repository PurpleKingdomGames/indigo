package indigoplugin

import indigoplugin.generators.EmbedText
import java.io.File
import indigoplugin.generators.EmbedGLSLShaderPair

/** Assists with setting up source code generators for Indigo projects
  *
  * @param outDirectory
  *   The base output directory, for Mill this will be `os.pwd / "out"`, and sbt will be `Compile / sourceManaged`
  * @param fullyQualifiedPackageName
  *   The package all generated sources will be placed under
  * @param sources
  *   Accumulated source paths
  */
final case class IndigoGenerators(outDirectory: os.Path, fullyQualifiedPackageName: String, sources: Seq[os.Path]) {

  def toSources: Seq[os.Path]  = sources
  def toSourceFiles: Seq[File] = sources.map(_.toIO)

  def withOutputDirectory(value: os.Path): IndigoGenerators =
    this.copy(outDirectory = value)
  def withOutputDirectory(value: File): IndigoGenerators =
    this.copy(outDirectory = os.Path(value))
  def withOutputDirectory(value: String): IndigoGenerators =
    this.copy(outDirectory = os.Path(value))

  /** Set a fully qualified package names for your output sources, e.g. com.mycompany.generated.code */
  def withPackage(packageName: String): IndigoGenerators =
    this.copy(fullyQualifiedPackageName = packageName)

  /** Embed raw text into a static variable.
    *
    * @param moduleName
    *   The name for the Scala module, e.g. 'MyModule' would be `object MyModule {}`
    * @param text
    *   The text to embed.
    */
  def embedText(moduleName: String, text: String): IndigoGenerators =
    this.copy(
      sources = sources ++
        EmbedText.generate(outDirectory, moduleName, fullyQualifiedPackageName, text)
    )

  /** Embed a GLSL shader pair into a Scala module.
    *
    * The shader pair can be raw GLSL, or they can make use of the indigo tags to defined the regions you want to embed.
    * E.g.,
    *
    * ```...Code above and below the tag will be ignored...```
    * ```//<indigo-vertex>```
    * ```...shader code goes here...```
    * ```//</indigo-vertex>```
    *
    * Available tags are: vertex, fragment, prepare, composite, light
    *
    * @param moduleName
    *   The name for the Scala module, e.g. 'MyModule' would be `object MyModule {}`
    * @param vertexShaderPath
    *   The path to the vertex shader file
    * @param fragmentShaderPath
    *   The path to the fragment shader file
    * @param validate
    *   Attempt to validate the GLSL, requires the glslang validator to be install locally on the machine.
    */
  def embedGLSLShaders(
      moduleName: String,
      vertexShaderPath: os.Path,
      fragmentShaderPath: os.Path,
      validate: Boolean
  ): IndigoGenerators =
    this.copy(
      sources = sources ++
        EmbedGLSLShaderPair.generate(
          outDirectory,
          moduleName,
          fullyQualifiedPackageName,
          vertexShaderPath,
          fragmentShaderPath,
          validate
        )
    )

  /** Embed a GLSL shader pair into a Scala module.
    *
    * The shader pair can be raw GLSL, or they can make use of the indigo tags to defined the regions you want to embed.
    * E.g.,
    *
    * ```...Code above and below the tag will be ignored...```
    * ```//<indigo-vertex>```
    * ```...shader code goes here...```
    * ```//</indigo-vertex>```
    *
    * Available tags are: vertex, fragment, prepare, composite, light
    *
    * @param moduleName
    *   The name for the Scala module, e.g. 'MyModule' would be `object MyModule {}`
    * @param vertexShaderPath
    *   The path to the vertex shader file
    * @param fragmentShaderPath
    *   The path to the fragment shader file
    * @param validate
    *   Attempt to validate the GLSL, requires the glslang validator to be install locally on the machine.
    */
  def embedGLSLShaders(
      moduleName: String,
      vertexShaderPath: File,
      fragmentShaderPath: File,
      validate: Boolean
  ): IndigoGenerators =
    this.copy(
      sources = sources ++
        EmbedGLSLShaderPair.generate(
          outDirectory,
          moduleName,
          fullyQualifiedPackageName,
          os.Path(vertexShaderPath),
          os.Path(fragmentShaderPath),
          validate
        )
    )

  /** Embed a GLSL shader pair into a Scala module.
    *
    * The shader pair can be raw GLSL, or they can make use of the indigo tags to defined the regions you want to embed.
    * E.g.,
    *
    * ```...Code above and below the tag will be ignored...```
    * ```//<indigo-vertex>```
    * ```...shader code goes here...```
    * ```//</indigo-vertex>```
    *
    * Available tags are: vertex, fragment, prepare, composite, light
    *
    * @param moduleName
    *   The name for the Scala module, e.g. 'MyModule' would be `object MyModule {}`
    * @param vertexShaderPath
    *   The path to the vertex shader file
    * @param fragmentShaderPath
    *   The path to the fragment shader file
    * @param validate
    *   Attempt to validate the GLSL, requires the glslang validator to be install locally on the machine.
    */
  def embedGLSLShaders(
      moduleName: String,
      vertexShaderPath: String,
      fragmentShaderPath: String,
      validate: Boolean
  ): IndigoGenerators =
    this.copy(
      sources = sources ++
        EmbedGLSLShaderPair.generate(
          outDirectory,
          moduleName,
          fullyQualifiedPackageName,
          os.RelPath(vertexShaderPath).resolveFrom(os.pwd),
          os.RelPath(fragmentShaderPath).resolveFrom(os.pwd),
          validate
        )
    )

}

object IndigoGenerators {

  val None: IndigoGenerators =
    IndigoGenerators(os.Path("/tmp/indigo-build-null"), "", Seq())

  def default(outputDirectory: os.Path, fullyQualifiedPackageName: String): IndigoGenerators =
    IndigoGenerators(outputDirectory, fullyQualifiedPackageName, Seq())

  def mill(fullyQualifiedPackageName: String): IndigoGenerators =
    default(os.pwd / "out", fullyQualifiedPackageName)

  def mill(outDirectory: os.Path, fullyQualifiedPackageName: String): IndigoGenerators =
    default(outDirectory, fullyQualifiedPackageName)

  def sbt(sourceManagedDirectory: File, fullyQualifiedPackageName: String): IndigoGenerators =
    default(os.Path(sourceManagedDirectory), fullyQualifiedPackageName)

}
