package indigoplugin

import indigoplugin.generators.EmbedText
import java.io.File
import indigoplugin.generators.EmbedGLSLShaderPair
import indigoplugin.generators.AssetListing
import indigoplugin.generators.ConfigGen
import indigoplugin.generators.EmbedData
import indigoplugin.generators.EmbedAseprite

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
    * @param file
    *   The path to the text file to embed.
    */
  def embedText(moduleName: String, file: os.Path): IndigoGenerators =
    this.copy(
      sources = sources ++
        EmbedText.generate(outDirectory, moduleName, fullyQualifiedPackageName, file)
    )

  /** Embed raw text into a static variable.
    *
    * @param moduleName
    *   The name for the Scala module, e.g. 'MyModule' would be `object MyModule {}`
    * @param file
    *   The text file to embed.
    */
  def embedText(moduleName: String, file: File): IndigoGenerators =
    this.copy(
      sources = sources ++
        EmbedText.generate(outDirectory, moduleName, fullyQualifiedPackageName, os.Path(file))
    )

  /** Embed raw text into a static variable.
    *
    * @param moduleName
    *   The name for the Scala module, e.g. 'MyModule' would be `object MyModule {}`
    * @param file
    *   The relative path to the text file to embed.
    */
  def embedText(moduleName: String, file: String): IndigoGenerators =
    this.copy(
      sources = sources ++
        EmbedText.generate(outDirectory, moduleName, fullyQualifiedPackageName, os.RelPath(file).resolveFrom(os.pwd))
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
    *   The vertex shader file
    * @param fragmentShaderPath
    *   The fragment shader file
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
    *   The relative path to the vertex shader file
    * @param fragmentShaderPath
    *   The relative path to the fragment shader file
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

  /** Generate a module that conveniently lists all of your assets with some helper / pre-constructed instances ready
    * for use in your game.
    *
    * @param moduleName
    *   The name for the Scala module, e.g. 'MyModule' would be `object MyModule {}`
    * @param indigoAssets
    *   The IndigoAssets config object, used to locate and fitler your assets.
    */
  def listAssets(
      moduleName: String,
      indigoAssets: IndigoAssets
  ): IndigoGenerators =
    this.copy(
      sources = sources ++
        AssetListing.generate(
          outDirectory,
          moduleName,
          fullyQualifiedPackageName,
          indigoAssets
        )
    )

  /** Generate a module that provides a default `GameConfig` instance that is synchronised with your build settings.
    *
    * @param moduleName
    *   The name for the Scala module, e.g. 'MyModule' would be `object MyModule {}`
    * @param indigoOptions
    *   The IndigoOptions config object
    */
  def generateConfig(
      moduleName: String,
      indigoOptions: IndigoOptions
  ): IndigoGenerators =
    this.copy(
      sources = sources ++
        ConfigGen.generate(
          outDirectory,
          moduleName,
          fullyQualifiedPackageName,
          indigoOptions
        )
    )

  /** Used to embed CSV (Comma Separated Value) data, usage: embedCSV.asEnum(moduleName, filePath), or
    * embedCSV.asMap(moduleName, filePath)
    */
  def embedCSV: DataEmbed = new DataEmbed(
    this,
    delimiter = ",",
    rowFilter = (row: String) =>
      row match {
        case r if r.isEmpty => false
        case _              => true
      }
  )

  /** Used to embed TSV (Tab Separated Value) data, usage: embedTSV.asEnum(moduleName, filePath), or
    * embedTSV.asMap(moduleName, filePath)
    */
  def embedTSV: DataEmbed = new DataEmbed(
    this,
    delimiter = "\t",
    rowFilter = (row: String) =>
      row match {
        case r if r.isEmpty => false
        case _              => true
      }
  )

  /** Used to embed markdown table data, usage: embedMarkdownTable.asEnum(moduleName, filePath), or
    * embedMarkdownTable.asMap(moduleName, filePath)
    */
  def embedMarkdownTable: DataEmbed = new DataEmbed(
    this,
    delimiter = "\\|",
    rowFilter = (row: String) => {
      val rgx = """---[ ?]*\|""".r

      row match {
        case r if r.isEmpty                    => false
        case r if rgx.findFirstIn(r).isDefined => false
        case _                                 => true
      }
    }
  )

  /** Used to embed data separated by some value other than commas, tabs, or pipes.
    *
    * @param delimiter
    *   The String to be used as a separator
    * @param rowFilter
    *   Allows you to ignore certain kinds of rows, for example empty rows or rows that divide headers from data
    */
  def embedSeparatedData(delimiter: String, rowFilter: String => Boolean): DataEmbed = new DataEmbed(
    this,
    delimiter,
    rowFilter
  )

  final class DataEmbed(gens: IndigoGenerators, delimiter: String, rowFilter: String => Boolean) {

    /** Embed the data as a Scala 3 Enum. */
    def asEnum(moduleName: String, file: os.Path): IndigoGenerators =
      gens.copy(
        sources = sources ++
          EmbedData.generate(
            outDirectory,
            moduleName,
            fullyQualifiedPackageName,
            file,
            delimiter,
            rowFilter,
            asEnum = true,
            extendsFrom = None
          )
      )

    /** Embed the data as a Scala 3 Enum that extends some fully qualified module name. E.g. `com.example.MyData`. */
    def asEnum(moduleName: String, file: os.Path, extendsFrom: String): IndigoGenerators =
      gens.copy(
        sources = sources ++
          EmbedData.generate(
            outDirectory,
            moduleName,
            fullyQualifiedPackageName,
            file,
            delimiter,
            rowFilter,
            asEnum = true,
            extendsFrom = Option(extendsFrom)
          )
      )

    /** Embed the data as a Scala 3 Enum. */
    def asEnum(moduleName: String, file: File): IndigoGenerators =
      gens.copy(
        sources = sources ++
          EmbedData.generate(
            outDirectory,
            moduleName,
            fullyQualifiedPackageName,
            os.Path(file),
            delimiter,
            rowFilter,
            asEnum = true,
            extendsFrom = None
          )
      )

    /** Embed the data as a Scala 3 Enum that extends some fully qualified module name. E.g. `com.example.MyData`. */
    def asEnum(moduleName: String, file: File, extendsFrom: String): IndigoGenerators =
      gens.copy(
        sources = sources ++
          EmbedData.generate(
            outDirectory,
            moduleName,
            fullyQualifiedPackageName,
            os.Path(file),
            delimiter,
            rowFilter,
            asEnum = true,
            extendsFrom = Option(extendsFrom)
          )
      )

    /** Embed the data as a Scala 3 Enum. */
    def asEnum(moduleName: String, file: String): IndigoGenerators =
      gens.copy(
        sources = sources ++
          EmbedData.generate(
            outDirectory,
            moduleName,
            fullyQualifiedPackageName,
            os.RelPath(file).resolveFrom(os.pwd),
            delimiter,
            rowFilter,
            asEnum = true,
            extendsFrom = None
          )
      )

    /** Embed the data as a Scala 3 Enum that extends some fully qualified module name. E.g. `com.example.MyData`. */
    def asEnum(moduleName: String, file: String, extendsFrom: String): IndigoGenerators =
      gens.copy(
        sources = sources ++
          EmbedData.generate(
            outDirectory,
            moduleName,
            fullyQualifiedPackageName,
            os.RelPath(file).resolveFrom(os.pwd),
            delimiter,
            rowFilter,
            asEnum = true,
            extendsFrom = Option(extendsFrom)
          )
      )

    /** Embed the data as a Map. */
    def asMap(moduleName: String, file: os.Path): IndigoGenerators =
      gens.copy(
        sources = sources ++
          EmbedData.generate(
            outDirectory,
            moduleName,
            fullyQualifiedPackageName,
            file,
            delimiter,
            rowFilter,
            asEnum = false,
            None
          )
      )

    /** Embed the data as a Map. */
    def asMap(moduleName: String, file: File): IndigoGenerators =
      gens.copy(
        sources = sources ++
          EmbedData.generate(
            outDirectory,
            moduleName,
            fullyQualifiedPackageName,
            os.Path(file),
            delimiter,
            rowFilter,
            asEnum = false,
            None
          )
      )

    /** Embed the data as a Map. */
    def asMap(moduleName: String, file: String): IndigoGenerators =
      gens.copy(
        sources = sources ++
          EmbedData.generate(
            outDirectory,
            moduleName,
            fullyQualifiedPackageName,
            os.RelPath(file).resolveFrom(os.pwd),
            delimiter,
            rowFilter,
            asEnum = false,
            None
          )
      )
  }

  /** Embed Aseprite data in a module.
    *
    * PLEASE NOTE: Aseprite data must be exported using the 'array' option, the 'hash' format is not supported.
    *
    * @param moduleName
    *   The name for the Scala module, e.g. 'MyModule' would be `object MyModule {}`
    * @param file
    *   The path to the Asprite JSON data file.
    */
  def embedAseprite(moduleName: String, file: os.Path): IndigoGenerators =
    this.copy(
      sources = sources ++
        EmbedAseprite.generate(outDirectory, moduleName, fullyQualifiedPackageName, file)
    )

  /** Embed Aseprite data in a module.
    *
    * PLEASE NOTE: Aseprite data must be exported using the 'array' option, the 'hash' format is not supported.
    *
    * @param moduleName
    *   The name for the Scala module, e.g. 'MyModule' would be `object MyModule {}`
    * @param file
    *   The path to the Asprite JSON data file.
    */
  def embedAseprite(moduleName: String, file: File): IndigoGenerators =
    this.copy(
      sources = sources ++
        EmbedAseprite.generate(outDirectory, moduleName, fullyQualifiedPackageName, os.Path(file))
    )

  /** Embed Aseprite data in a module.
    *
    * PLEASE NOTE: Aseprite data must be exported using the 'array' option, the 'hash' format is not supported.
    *
    * @param moduleName
    *   The name for the Scala module, e.g. 'MyModule' would be `object MyModule {}`
    * @param file
    *   The path to the Asprite JSON data file.
    */
  def embedAseprite(moduleName: String, file: String): IndigoGenerators =
    this.copy(
      sources = sources ++
        EmbedAseprite.generate(
          outDirectory,
          moduleName,
          fullyQualifiedPackageName,
          os.RelPath(file).resolveFrom(os.pwd)
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
