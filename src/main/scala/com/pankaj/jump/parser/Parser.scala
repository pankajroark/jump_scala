package com.pankaj.jump.parser

import com.pankaj.jump.Path

import scala.tools.nsc.ast.parser.Parsers
import scala.tools.nsc.{Global, Settings}
import scala.tools.nsc.reporters.ConsoleReporter
import scala.reflect.internal.util.BatchSourceFile
import scala.io.Source
import scala.annotation.tailrec

case class Pos(file: Path, row: Int, col: Int)

// @param rfqn  reverse fully qualified name e.g. "util" : "twitter" : "com"
// JSymbol stands for Jump Symbol, longer name to avoid confusion with global.Symbol
case class JSymbol(rfqn: List[String], loc: Pos, typ: String) {
  def name = rfqn.head
  def qualName = rfqn.reverse.mkString(".")
}

case class JImport(qual: List[String], name: String, rename: String)

// Parser parses a single file
class Parser {

  val settings = new Settings
  settings processArgumentString "-usejavacp"

  val global = Global(settings, new ConsoleReporter(settings))

  import global._

  def astForFile(file: Path): Tree = {
    val run = new Run
    val filename = file.toString
    val parser = new syntaxAnalyzer.UnitParser(
      new CompilationUnit(
        new BatchSourceFile(filename, Source.fromFile(filename).mkString)
      )
    )
    parser.parse()
  }

  // @return (Imports, Package)
  def trackDownSymbol(word: String, loc: Pos): (List[JImport], List[String]) = {

    def wordInside(p: Position): Boolean = {
      p.line == loc.row &&
      p.column <= loc.col &&
      p.column + word.size > loc.col
    }

    object FindWithTrace extends Traverser {
      import collection.mutable
      private val _path: mutable.Stack[Tree] = mutable.Stack()
      private var _trace: List[Tree] = Nil
      private var _found = false
      private var _imports: List[Import] = Nil

      def trace: List[Tree] = _trace
      def imports: List[Import] = _imports

      private def hasLoc(p: Position): Boolean =  {
        p match {
          case NoPosition => false
          case _ =>
            wordInside(p)
        }
      }

      override def traverse(t: Tree) = {
        if (!_found) {
          _path push t
          if(hasLoc(t.pos)) {
            _found = true
            _trace = _path.toList
          } else {
            try {
              t match {
                case i: Import =>
                  _imports = i :: _imports
                case _ =>
              }
              super.traverse(t)
            } finally _path.pop()
          }
        }
      }
    }

    val tree = astForFile(loc.file)
    FindWithTrace.traverse(tree)
    val trace = (FindWithTrace.trace)
    val packages = trace.foldLeft(List[String]()) { (acc, t) =>
      t match {
        case PackageDef(pid, stats) =>
          treeToList(pid.qualifier) ++ List(pid.name.toString) ++ acc
        case _ => acc
      }
    }
    val imports = FindWithTrace.imports flatMap { case Import(expr, selectors) =>
      val qual = treeToList(expr)
      for (ImportSelector(name, _, rename, _) <- selectors) yield {
        val ren = if (rename == null) name else rename
        JImport(qual, name.toString, ren.toString)
      }
    }
    (imports, packages)
  }

  def listSymbols(file: Path): List[JSymbol] = {
    val tree = astForFile(file)
    tx(tree)
  }
  // list of first elements in the tree
  // Meant for the case where tree really is a list
  private def treeToList(tree: Tree): List[String] = {
    @tailrec
    def go(tree: Tree, ls: List[String]): List[String] = {
      tree match {
        case n: NameTree  =>
          if (!tree.children.isEmpty)
            go(tree.children.head, n.name.toString :: ls)
          else n.name.toString :: ls
        case _ => ls
      }
    }
    go(tree, Nil).reverse
  }

  private def packagePidToNamespace(pid: RefTree): List[String] =
    pid.name.toString :: treeToList(pid.qualifier)

  private def positionToPos(pos: Position): Pos =
    Pos(pos.source.path, pos.line, pos.column)


  private def tx(tree: Tree, namespace: List[String] = Nil): List[JSymbol] = tree match {
    case p:PackageDef =>
      val ns = packagePidToNamespace(p.pid)
      p.stats.flatMap{tx(_, ns)}

    case c:ClassDef =>
      val ns = c.name.toString :: namespace
      // todo handle traits
      val sym = JSymbol(ns, positionToPos(c.pos), "class")
      sym :: c.impl.body.flatMap{tx(_, ns)}

    case m:ModuleDef =>
      val ns = m.name.toString :: namespace
      m.impl.body.flatMap{tx(_, ns)}

    case v:ValOrDefDef =>
      val ns = v.name.toString :: namespace
      List(JSymbol(ns, positionToPos(v.pos), "val"))

    case _ =>
      Nil
  }


}
