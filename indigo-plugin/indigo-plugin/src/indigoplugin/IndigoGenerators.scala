package indigoplugin

import indigoplugin.generators.EmbedText
import java.io.File
import indigoplugin.generators.EmbedGLSLShaderPair
import indigoplugin.generators.AssetListing
import indigoplugin.generators.ConfigGen
import indigoplugin.generators.EmbedData
import indigoplugin.generators.EmbedAseprite
import indigoplugin.generators.FontGen
import indigoplugin.utils.Utils

/** Assists with setting up source code generators for Indigo projects
  *
  * @param fullyQualifiedPackageName
  *   The package all generated sources will be placed under
  * @param sources
  *   Accumulated source paths
  */
final case class IndigoGenerators(fullyQualifiedPackageName: String, sources: Seq[os.Path => Seq[os.Path]]) {

  val workspaceDir = Utils.findWorkspace

  def toSourcePaths(destination: os.Path): Seq[os.Path] = sources.flatMap(_(destination))
  def toSourcePaths(destination: File): Seq[os.Path]    = sources.flatMap(_(os.Path(destination)))
  def toSourceFiles(destination: os.Path): Seq[File]    = sources.flatMap(_(destination)).map(_.toIO)
  def toSourceFiles(destination: File): Seq[File]       = sources.flatMap(_(os.Path(destination))).map(_.toIO)

  /** Set a fully qualified package names for your output sources, e.g. com.mycompany.generated.code */
  def withPackage(packageName: String): IndigoGenerators =
    this.copy(fullyQualifiedPackageName = packageName)

  /** Takes the contents of a text file, and leaves it to you to decide how to turn it into Scala code. The template
    * provides nothing except the package declaration.
    *
    * @param moduleName
    *   The name for the Scala module, in this case, acts as the file name only.
    * @param file
    *   The path to the text file to embed.
    * @param present
    *   A function that takes and String and expects you to create a String of Scala code. You could parse JSON or read
    *   a list of files or... anything!
    */
  def embed(moduleName: String, file: os.Path)(present: String => String): IndigoGenerators =
    this.copy(
      sources = sources :+
        EmbedText.generate(moduleName, fullyQualifiedPackageName, file, present)
    )

  /** Takes the contents of a text file, and leaves it to you to decide how to turn it into Scala code. The template
    * provides nothing except the package declaration.
    *
    * @param moduleName
    *   The name for the Scala module, in this case, acts as the file name only.
    * @param file
    *   The relative path to the text file to embed.
    * @param present
    *   A function that takes and String and expects you to create a String of Scala code. You could parse JSON or read
    *   a list of files or... anything!
    */
  def embed(moduleName: String, file: File)(present: String => String): IndigoGenerators =
    this.copy(
      sources = sources :+
        EmbedText.generate(moduleName, fullyQualifiedPackageName, os.RelPath(file).resolveFrom(workspaceDir), present)
    )

  /** Takes the contents of a text file, and leaves it to you to decide how to turn it into Scala code. The template
    * provides nothing except the package declaration.
    *
    * @param moduleName
    *   The name for the Scala module, in this case, acts as the file name only.
    * @param file
    *   The relative path to the text file to embed.
    * @param present
    *   A function that takes and String and expects you to create a String of Scala code. You could parse JSON or read
    *   a list of files or... anything!
    */
  def embed(moduleName: String, file: String)(present: String => String): IndigoGenerators =
    this.copy(
      sources = sources :+
        EmbedText.generate(moduleName, fullyQualifiedPackageName, os.RelPath(file).resolveFrom(workspaceDir), present)
    )

  /** Embed raw text into a static variable.
    *
    * @param moduleName
    *   The name for the Scala module, e.g. 'MyModule' would be `object MyModule {}`
    * @param file
    *   The path to the text file to embed.
    */
  def embedText(moduleName: String, file: os.Path): IndigoGenerators =
    this.copy(
      sources = sources :+
        EmbedText.generate(moduleName, fullyQualifiedPackageName, file)
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
      sources = sources :+
        EmbedText.generate(moduleName, fullyQualifiedPackageName, os.Path(file))
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
      sources = sources :+
        EmbedText.generate(moduleName, fullyQualifiedPackageName, os.RelPath(file).resolveFrom(workspaceDir))
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
      sources = sources :+
        EmbedGLSLShaderPair.generate(
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
      sources = sources :+
        EmbedGLSLShaderPair.generate(
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
      sources = sources :+
        EmbedGLSLShaderPair.generate(
          moduleName,
          fullyQualifiedPackageName,
          os.RelPath(vertexShaderPath).resolveFrom(workspaceDir),
          os.RelPath(fragmentShaderPath).resolveFrom(workspaceDir),
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
      sources = sources :+
        AssetListing.generate(
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
      sources = sources :+
        ConfigGen.generate(
          moduleName,
          fullyQualifiedPackageName,
          indigoOptions
        )
    )

  /** Used to generate a rendered font sheet and `FontInfo` instance based on a supplied font source file.
    *
    * @param moduleName
    *   The name for the Scala module, e.g. 'MyModule' would be `object MyModule {}`
    * @param font
    *   The path to the font file, e.g. a TrueType *.ttf file
    * @param fontOptions
    *   Parameters for the font, such as its identifier (font key), size, and anti-aliasing.
    * @param imageOut
    *   The destination directory for the font-sheet image to be written into, typically somewhere in your assets
    *   directory so that your game can load it.
    */
  def embedFont(
      moduleName: String,
      font: os.Path,
      fontOptions: FontOptions,
      imageOut: os.Path
  ): IndigoGenerators =
    this.copy(
      sources = sources :+
        FontGen.generate(
          moduleName,
          fullyQualifiedPackageName,
          font,
          fontOptions,
          imageOut
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
        sources = sources :+
          EmbedData.generate(
            moduleName,
            fullyQualifiedPackageName,
            file,
            delimiter,
            rowFilter,
            embedMode = EmbedData.Mode.AsEnum(None)
          )
      )

    /** Embed the data as a Scala 3 Enum that extends some fully qualified module name. E.g. `com.example.MyData`. */
    def asEnum(moduleName: String, file: os.Path, extendsFrom: String): IndigoGenerators =
      gens.copy(
        sources = sources :+
          EmbedData.generate(
            moduleName,
            fullyQualifiedPackageName,
            file,
            delimiter,
            rowFilter,
            embedMode = EmbedData.Mode.AsEnum(Option(extendsFrom))
          )
      )

    /** Embed the data as a Scala 3 Enum. */
    def asEnum(moduleName: String, file: File): IndigoGenerators =
      gens.copy(
        sources = sources :+
          EmbedData.generate(
            moduleName,
            fullyQualifiedPackageName,
            os.Path(file),
            delimiter,
            rowFilter,
            embedMode = EmbedData.Mode.AsEnum(None)
          )
      )

    /** Embed the data as a Scala 3 Enum that extends some fully qualified module name. E.g. `com.example.MyData`. */
    def asEnum(moduleName: String, file: File, extendsFrom: String): IndigoGenerators =
      gens.copy(
        sources = sources :+
          EmbedData.generate(
            moduleName,
            fullyQualifiedPackageName,
            os.Path(file),
            delimiter,
            rowFilter,
            embedMode = EmbedData.Mode.AsEnum(Option(extendsFrom))
          )
      )

    /** Embed the data as a Scala 3 Enum. */
    def asEnum(moduleName: String, file: String): IndigoGenerators =
      gens.copy(
        sources = sources :+
          EmbedData.generate(
            moduleName,
            fullyQualifiedPackageName,
            os.RelPath(file).resolveFrom(workspaceDir),
            delimiter,
            rowFilter,
            embedMode = EmbedData.Mode.AsEnum(None)
          )
      )

    /** Embed the data as a Scala 3 Enum that extends some fully qualified module name. E.g. `com.example.MyData`. */
    def asEnum(moduleName: String, file: String, extendsFrom: String): IndigoGenerators =
      gens.copy(
        sources = sources :+
          EmbedData.generate(
            moduleName,
            fullyQualifiedPackageName,
            os.RelPath(file).resolveFrom(workspaceDir),
            delimiter,
            rowFilter,
            embedMode = EmbedData.Mode.AsEnum(Option(extendsFrom))
          )
      )

    /** Embed the data as a Map. */
    def asMap(moduleName: String, file: os.Path): IndigoGenerators =
      gens.copy(
        sources = sources :+
          EmbedData.generate(
            moduleName,
            fullyQualifiedPackageName,
            file,
            delimiter,
            rowFilter,
            embedMode = EmbedData.Mode.AsMap
          )
      )

    /** Embed the data as a Map. */
    def asMap(moduleName: String, file: File): IndigoGenerators =
      gens.copy(
        sources = sources :+
          EmbedData.generate(
            moduleName,
            fullyQualifiedPackageName,
            os.Path(file),
            delimiter,
            rowFilter,
            embedMode = EmbedData.Mode.AsMap
          )
      )

    /** Embed the data as a Map. */
    def asMap(moduleName: String, file: String): IndigoGenerators =
      gens.copy(
        sources = sources :+
          EmbedData.generate(
            moduleName,
            fullyQualifiedPackageName,
            os.RelPath(file).resolveFrom(workspaceDir),
            delimiter,
            rowFilter,
            embedMode = EmbedData.Mode.AsMap
          )
      )

    /** Embed the data using a custom present function. In this mode we make no assumptions about what code you plan to
      * generate. The result of the present function will be placed inside a scala file named after the module name,
      * that includes the package, but nothing else.
      *
      * We provide the same mechanism and guarantees as using the other data embed methods, things like consistent row
      * lengths, and all columns having the same type of data (`StringData`, `IntData`, `DouleData`, or `BooleanData`).
      * The `DataType` provides information about the type in the Cell, and some simple/useful methods, and can be
      * exhustively pattern matched.
      *
      * @param moduleName
      *   In this mode, this is the name of the file only. If you wish to use it in your present function, you can
      *   always apply it as an argument to a curried function.
      * @param file
      *   The data file to read.
      * @param present
      *   A function from a list of all the rows, including the headers, to some generated output code, written as a
      *   simple `String` value.
      */
    def asCustom(moduleName: String, file: os.Path)(present: List[List[DataType]] => String): IndigoGenerators =
      gens.copy(
        sources = sources :+
          EmbedData.generate(
            moduleName,
            fullyQualifiedPackageName,
            file,
            delimiter,
            rowFilter,
            embedMode = EmbedData.Mode.AsCustom(present)
          )
      )

    /** Embed the data using a custom present function. In this mode we make no assumptions about what code you plan to
      * generate. The result of the present function will be placed inside a scala file named after the module name,
      * that includes the package, but nothing else.
      *
      * We provide the same mechanism and guarantees as using the other data embed methods, things like consistent row
      * lengths, and all columns having the same type of data (`StringData`, `IntData`, `DouleData`, or `BooleanData`).
      * The `DataType` provides information about the type in the Cell, and some simple/useful methods, and can be
      * exhustively pattern matched.
      *
      * @param moduleName
      *   In this mode, this is the name of the file only. If you wish to use it in your present function, you can
      *   always apply it as an argument to a curried function.
      * @param file
      *   The data file to read.
      * @param present
      *   A function from a list of all the rows, including the headers, to some generated output code, written as a
      *   simple `String` value.
      */
    def asCustom(moduleName: String, file: File)(present: List[List[DataType]] => String): IndigoGenerators =
      gens.copy(
        sources = sources :+
          EmbedData.generate(
            moduleName,
            fullyQualifiedPackageName,
            os.Path(file),
            delimiter,
            rowFilter,
            embedMode = EmbedData.Mode.AsCustom(present)
          )
      )

    /** Embed the data using a custom present function. In this mode we make no assumptions about what code you plan to
      * generate. The result of the present function will be placed inside a scala file named after the module name,
      * that includes the package, but nothing else.
      *
      * We provide the same mechanism and guarantees as using the other data embed methods, things like consistent row
      * lengths, and all columns having the same type of data (`StringData`, `IntData`, `DouleData`, or `BooleanData`).
      * The `DataType` provides information about the type in the Cell, and some simple/useful methods, and can be
      * exhustively pattern matched.
      *
      * @param moduleName
      *   In this mode, this is the name of the file only. If you wish to use it in your present function, you can
      *   always apply it as an argument to a curried function.
      * @param file
      *   The data file to read.
      * @param present
      *   A function from a list of all the rows, including the headers, to some generated output code, written as a
      *   simple `String` value.
      */
    def asCustom(moduleName: String, file: String)(present: List[List[DataType]] => String): IndigoGenerators =
      gens.copy(
        sources = sources :+
          EmbedData.generate(
            moduleName,
            fullyQualifiedPackageName,
            os.RelPath(file).resolveFrom(workspaceDir),
            delimiter,
            rowFilter,
            embedMode = EmbedData.Mode.AsCustom(present)
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
      sources = sources :+
        EmbedAseprite.generate(moduleName, fullyQualifiedPackageName, file)
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
      sources = sources :+
        EmbedAseprite.generate(moduleName, fullyQualifiedPackageName, os.Path(file))
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
      sources = sources :+
        EmbedAseprite.generate(
          moduleName,
          fullyQualifiedPackageName,
          os.RelPath(file).resolveFrom(workspaceDir)
        )
    )

}

object IndigoGenerators {

  val None: IndigoGenerators =
    IndigoGenerators("", Seq())

  def apply(fullyQualifiedPackageName: String): IndigoGenerators =
    IndigoGenerators(fullyQualifiedPackageName, Seq())

}
