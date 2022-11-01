package indigo.macroshaders

import scala.annotation.tailrec
import scala.quoted.*

sealed trait ShaderAST derives CanEqual
object ShaderAST:

  given ToExpr[ShaderAST] with {
    def apply(x: ShaderAST)(using Quotes): Expr[ShaderAST] =
      x match
        case v: Empty        => Expr(v)
        case v: Block        => Expr(v)
        case v: NamedBlock   => Expr(v)
        case v: ShaderBlock  => Expr(v)
        case v: Function     => Expr(v)
        case v: CallFunction => Expr(v)
        case v: Infix        => Expr(v)
        case v: DataTypes    => Expr(v)
        case v: Val          => Expr(v)
  }

  final case class Empty() extends ShaderAST
  object Empty:
    given ToExpr[Empty] with {
      def apply(x: Empty)(using Quotes): Expr[Empty] =
        '{ Empty() }
    }

  final case class Block(statements: List[ShaderAST]) extends ShaderAST
  object Block:
    given ToExpr[Block] with {
      def apply(x: Block)(using Quotes): Expr[Block] =
        '{ Block(${ Expr(x.statements) }) }
    }

    def apply(statements: ShaderAST*): Block =
      Block(statements.toList)

  final case class NamedBlock(namespace: String, id: String, statements: List[ShaderAST]) extends ShaderAST
  object NamedBlock:
    given ToExpr[NamedBlock] with {
      def apply(x: NamedBlock)(using Quotes): Expr[NamedBlock] =
        '{ NamedBlock(${ Expr(x.namespace) }, ${ Expr(x.id) }, ${ Expr(x.statements) }) }
    }

    def apply(namespace: String, id: String, statements: ShaderAST*): NamedBlock =
      NamedBlock(namespace, id, statements.toList)

  // Specifically handles our 'Shader' type
  final case class ShaderBlock(statements: List[ShaderAST]) extends ShaderAST
  object ShaderBlock:
    given ToExpr[ShaderBlock] with {
      def apply(x: ShaderBlock)(using Quotes): Expr[ShaderBlock] =
        '{ ShaderBlock(${ Expr(x.statements) }) }
    }

    def apply(envVarName: String, statements: ShaderAST*): ShaderBlock =
      ShaderBlock(statements.toList)

  final case class Function(id: String, args: List[String], body: ShaderAST, returnType: Option[ShaderAST])
      extends ShaderAST
  object Function:
    given ToExpr[Function] with {
      def apply(x: Function)(using Quotes): Expr[Function] =
        '{ Function(${ Expr(x.id) }, ${ Expr(x.args) }, ${ Expr(x.body) }, ${ Expr(x.returnType) }) }
    }
  final case class CallFunction(
      id: String,
      args: List[ShaderAST],
      argNames: List[String],
      returnType: Option[ShaderAST]
  ) extends ShaderAST
  object CallFunction:
    given ToExpr[CallFunction] with {
      def apply(x: CallFunction)(using Quotes): Expr[CallFunction] =
        '{ CallFunction(${ Expr(x.id) }, ${ Expr(x.args) }, ${ Expr(x.argNames) }, ${ Expr(x.returnType) }) }
    }
  final case class Infix(
      op: String,
      left: ShaderAST,
      right: ShaderAST,
      returnType: Option[ShaderAST]
  ) extends ShaderAST
  object Infix:
    given ToExpr[Infix] with {
      def apply(x: Infix)(using Quotes): Expr[Infix] =
        '{ Infix(${ Expr(x.op) }, ${ Expr(x.left) }, ${ Expr(x.right) }, ${ Expr(x.returnType) }) }
    }

  final case class Val(id: String, value: ShaderAST) extends ShaderAST
  object Val:
    given ToExpr[Val] with {
      def apply(x: Val)(using Quotes): Expr[Val] =
        '{ Val(${ Expr(x.id) }, ${ Expr(x.value) }) }
    }

  enum DataTypes extends ShaderAST:
    case closure(body: ShaderAST, typeOf: Option[String])
    case ident(id: String)
    case float(v: Float)
    case vec2(args: List[ShaderAST])
    case vec3(args: List[ShaderAST])
    case vec4(args: List[ShaderAST])
    case swizzle(genType: ShaderAST, swizzle: String, returnType: Option[ShaderAST])

  object DataTypes:

    object vec2:
      def apply(args: Float*): DataTypes.vec2 =
        DataTypes.vec2(args.toList.map(DataTypes.float.apply))
    object vec3:
      def apply(args: Float*): DataTypes.vec3 =
        DataTypes.vec3(args.toList.map(DataTypes.float.apply))
    object vec4:
      def apply(args: Float*): DataTypes.vec4 =
        DataTypes.vec4(args.toList.map(DataTypes.float.apply))

    given ToExpr[DataTypes] with {
      def apply(x: DataTypes)(using Quotes): Expr[DataTypes] =
        x match
          case v: DataTypes.closure => Expr(v)
          case v: DataTypes.ident   => Expr(v)
          case v: DataTypes.float   => Expr(v)
          case v: DataTypes.vec2    => Expr(v)
          case v: DataTypes.vec3    => Expr(v)
          case v: DataTypes.vec4    => Expr(v)
          case v: DataTypes.swizzle => Expr(v)
    }
    given ToExpr[ident] with {
      def apply(x: ident)(using Quotes): Expr[ident] =
        '{ ident(${ Expr(x.id) }) }
    }
    given ToExpr[closure] with {
      def apply(x: closure)(using Quotes): Expr[closure] =
        '{ closure(${ Expr(x.body) }, ${ Expr(x.typeOf) }) }
    }
    given ToExpr[float] with {
      def apply(x: float)(using Quotes): Expr[float] =
        '{ float(${ Expr(x.v) }) }
    }
    given ToExpr[vec2] with {
      def apply(x: vec2)(using Quotes): Expr[vec2] =
        '{ vec2(${ Expr(x.args) }) }
    }
    given ToExpr[vec3] with {
      def apply(x: vec3)(using Quotes): Expr[vec3] =
        '{ vec3(${ Expr(x.args) }) }
    }
    given ToExpr[vec4] with {
      def apply(x: vec4)(using Quotes): Expr[vec4] =
        '{ vec4(${ Expr(x.args) }) }
    }
    given ToExpr[swizzle] with {
      def apply(x: swizzle)(using Quotes): Expr[swizzle] =
        '{ swizzle(${ Expr(x.genType) }, ${ Expr(x.swizzle) }, ${ Expr(x.returnType) }) }
    }

  extension (ast: ShaderAST)
    def isEmpty: Boolean =
      ast match
        case Empty() => true
        case _       => false

    def exists(p: ShaderAST => Boolean): Boolean =
      find(p).isDefined

    def find(p: ShaderAST => Boolean): Option[ShaderAST] =
      @tailrec
      def rec(remaining: List[ShaderAST]): Option[ShaderAST] =
        remaining match
          case Nil => None
          case x :: xs =>
            x match
              case v if p(v)                => Option(v)
              case Empty()                  => rec(xs)
              case Block(s)                 => rec(s ++ xs)
              case NamedBlock(_, _, s)      => rec(s ++ xs)
              case ShaderBlock(s)           => rec(s ++ xs)
              case Function(_, _, body, _)  => rec(body :: xs)
              case CallFunction(_, _, _, _) => rec(xs)
              case Infix(_, l, r, _)        => rec(l :: r :: xs)
              case Val(_, body)             => rec(body :: xs)
              case v: DataTypes.closure     => rec(v.body :: xs)
              case v: DataTypes.ident       => rec(xs)
              case v: DataTypes.float       => rec(xs)
              case v: DataTypes.vec2        => rec(v.args ++ xs)
              case v: DataTypes.vec3        => rec(v.args ++ xs)
              case v: DataTypes.vec4        => rec(v.args ++ xs)
              case v: DataTypes.swizzle     => rec(v.genType :: xs)

      rec(List(ast))

    def prune: ShaderAST =
      def crush(statements: ShaderAST): ShaderAST =
        statements match
          // case Block(List(s))            => crush(s)
          case b: Block => b.copy(statements = b.statements.filterNot(_.isEmpty).map(crush))
          // case NamedBlock(_, _, List(s)) => crush(s)
          case b: NamedBlock => b.copy(statements = b.statements.filterNot(_.isEmpty).map(crush))
          case other         => other

      // traverse {
      //   case b: Block      => crush(b)
      //   case b: NamedBlock => crush(b)
      //   case other         => other
      // }
      crush(ast)

    def traverse(f: ShaderAST => ShaderAST): ShaderAST =
      ast match
        case v @ Empty()                              => f(v)
        case v @ Block(s)                             => f(Block(s.map(f)))
        case v @ NamedBlock(ns, id, s)                => f(NamedBlock(ns, id, s))
        case v @ ShaderBlock(s)                       => f(ShaderBlock(s))
        case v @ Function(id, args, body, returnType) => f(Function(id, args, f(body), returnType))
        case v @ CallFunction(_, _, _, _)             => f(v)
        case v @ Infix(op, l, r, returnType)          => f(Infix(op, f(l), f(r), returnType))
        case v @ Val(id, value)                       => f(Val(id, f(value)))
        case v @ DataTypes.closure(body, typeOf)      => f(DataTypes.closure(f(body), typeOf))
        case v @ DataTypes.float(_)                   => f(v)
        case v @ DataTypes.ident(_)                   => f(v)
        case v @ DataTypes.vec2(vs)                   => f(DataTypes.vec2(vs.map(f)))
        case v @ DataTypes.vec3(vs)                   => f(DataTypes.vec3(vs.map(f)))
        case v @ DataTypes.vec4(vs)                   => f(DataTypes.vec4(vs.map(f)))
        case v @ DataTypes.swizzle(_, _, _)           => f(v)

    def typeIdent: Option[ShaderAST.DataTypes.ident] =
      ast match
        case Empty()                      => None
        case Block(_)                     => None
        case NamedBlock(_, _, _)          => None
        case ShaderBlock(_)               => None
        case Function(_, _, _, rt)        => rt.flatMap(_.typeIdent)
        case CallFunction(_, _, _, rt)    => rt.flatMap(_.typeIdent)
        case Infix(_, _, _, rt)           => rt.flatMap(_.typeIdent)
        case Val(id, value)               => value.typeIdent
        case n @ DataTypes.ident(_)       => Option(n)
        case DataTypes.closure(_, typeOf) => typeOf.map(t => ShaderAST.DataTypes.ident(t))
        case DataTypes.float(_)           => Option(ShaderAST.DataTypes.ident("float"))
        case DataTypes.vec2(_)            => Option(ShaderAST.DataTypes.ident("vec2"))
        case DataTypes.vec3(_)            => Option(ShaderAST.DataTypes.ident("vec3"))
        case DataTypes.vec4(_)            => Option(ShaderAST.DataTypes.ident("vec4"))
        case DataTypes.swizzle(v, _, _)   => v.typeIdent

    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    def render: String =
      def rf(f: Float): String =
        val s = f.toString
        if s.contains(".") then s else s + ".0"

      def decideType(a: ShaderAST): Option[String] =
        a match
          case Empty()                      => None
          case Block(_)                     => None
          case NamedBlock(_, _, _)          => None
          case ShaderBlock(_)               => None
          case Function(_, _, _, rt)        => rt.map(_.render)
          case CallFunction(_, _, _, rt)    => rt.map(_.render)
          case Infix(_, _, _, rt)           => rt.map(_.render)
          case Val(id, value)               => value.typeIdent.map(_.render)
          case n @ DataTypes.ident(_)       => None
          case DataTypes.closure(_, typeOf) => typeOf
          case DataTypes.float(v)           => Option("float")
          case DataTypes.vec2(args)         => Option("vec2")
          case DataTypes.vec3(args)         => Option("vec3")
          case DataTypes.vec4(args)         => Option("vec4")
          case DataTypes.swizzle(v, _, rt)  => rt.map(_.render)

      def processStatements(statements: List[ShaderAST]): String =
        statements
          .map(_.prune)
          .filterNot(_.isEmpty) // Empty()
          .map(_.render)
          .filterNot(_.isEmpty) // empty String
          .mkString("", ";", ";")
          .replace(";;", ";")

      def processFunctionStatements(statements: List[ShaderAST], maybeReturnType: Option[String]): (String, String) =
        val nonEmpty = statements
          .map(_.prune)
          .filterNot(_.isEmpty)

        val (init, last) =
          if nonEmpty.length > 1 then (nonEmpty.dropRight(1), nonEmpty.takeRight(1))
          else (Nil, nonEmpty)

        val returnType =
          maybeReturnType match
            case None        => last.headOption.flatMap(decideType).getOrElse("void")
            case Some(value) => value

        val body = {
          if init.isEmpty then ""
          else init.map(_.render).filterNot(_.isEmpty).mkString("", ";", ";")
        } +
          last.headOption
            .map(ss => (if returnType != "void" then "return " else "") + ss.render + ";")
            .getOrElse("")
            .replace(";;", ";") // Some Scala elements are removed completely, but still are a statement techincally.

        (
          body,
          returnType
        )

      val res =
        ast match
          case Empty() =>
            ""

          case Block(statements) =>
            processStatements(statements)

          case ShaderBlock(statements) =>
            val (body, rt) = processFunctionStatements(statements, None)
            s"""void fragment(){COLOR=$body}"""

          case NamedBlock(namespace, id, statements) =>
            s"""$namespace$id {${processStatements(statements)}}"""

          case Function(id, args, body, returnType) if id.isEmpty =>
            throw new Exception("Failed to render shader, unnamed function definition found.")

          case Function(id, args, Block(statements), returnType) =>
            val (body, rt) = processFunctionStatements(statements, returnType.map(_.render))
            s"""$rt $id(${args.mkString(",")}){$body}"""

          case Function(id, args, NamedBlock(_, _, statements), returnType) =>
            val (body, rt) = processFunctionStatements(statements, returnType.map(_.render))
            s"""$rt $id(${args.mkString(",")}){$body}"""

          case Function(id, args, statement, returnType) =>
            val (body, rt) = processFunctionStatements(List(statement), returnType.map(_.render))
            s"""$rt $id(${args.mkString(",")}){$body}"""

          case CallFunction(id, args, _, _) =>
            s"""$id(${args.map(_.render).mkString(",")})"""

          case Infix(op, left, right, returnType) =>
            s"""(${left.render})$op(${right.render})"""

          case DataTypes.closure(body, typeOf) =>
            s"[closure $body $typeOf]"

          case DataTypes.ident(id) =>
            s"$id"

          case DataTypes.float(v) =>
            s"${rf(v)}"

          case DataTypes.vec2(args) =>
            s"vec2(${args.map(_.render).mkString(",")})"

          case DataTypes.vec3(args) =>
            s"vec3(${args.map(_.render).mkString(",")})"

          case DataTypes.vec4(args) =>
            s"vec4(${args.map(_.render).mkString(",")})"

          case DataTypes.swizzle(genType, swizzle, returnType) =>
            s"${genType.render}.$swizzle"

          case Val(id, value) =>
            s"""${decideType(value).getOrElse("void")} $id=${value.render}"""

      res
