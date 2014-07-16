package com.pankaj.jump

import java.util.concurrent.ArrayBlockingQueue
import scala.tools.nsc.ast.parser.Parsers
import scala.tools.nsc.Global
import scala.tools.nsc.Settings
import scala.tools.nsc.reporters.ConsoleReporter
import scala.reflect.internal.util.BatchSourceFile
import scala.io.Source
import scala.annotation.tailrec

case class Pos(file: Path, row: Int, col: Int)

// @param rfqn  reverse fully qualified name e.g. "util" : "twitter" : "com"
// JSymbol stands for Jump Symbol, longer name to avoid confusion with global.Symbol
case class JSymbol(rfqn: List[String], loc: Pos, typ: String) {
  def name = rfqn.head
}

// ParserWorker reads dirty files from the dirt queue, parses them and inserts
// qualified symbols in the Symbol table
class ParseWorker(queue: ArrayBlockingQueue[Path]) extends Runnable {

  def run() = {
  }
}

// Parser parses a single file
object Parser {

  val settings = new Settings
  settings processArgumentString "-usejavacp"

  val global = Global(settings, new ConsoleReporter(settings))

  import global._

  // list of first elements in the tree
  // Meant for the case where tree really is a list
  def treeToList(tree: Tree): List[String] = {
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

  def packagePidToNamespace(pid: RefTree): List[String] =
    pid.name.toString :: treeToList(pid.qualifier)


  def tx(tree: Tree, namespace: List[String] = Nil): Unit = tree match {
    case p:PackageDef => 
      //println(s"package ${p.pid.name} :: ${p.pid.qualifier}")
      val ns = packagePidToNamespace(p.pid)
      p.stats.foreach{tx(_, ns)}

    case c:ClassDef =>
      val ns = c.name.toString :: namespace
      println(s"class ${ns.reverse.mkString(".")}")
      c.impl.body.foreach{tx(_, ns)}

    case m:ModuleDef =>
      val ns = m.name.toString :: namespace
      println(s"object ${ns.reverse.mkString(".")}")
      m.impl.body.foreach{tx(_, ns)}

    case v:ValOrDefDef =>
      val ns = v.name.toString :: namespace
      println(s"identifier ${ns.reverse.mkString(".")}")
      println(s"position ${v.pos}")

    case _ => println("unknown")
  }

  def parse(file: Path): List[JSymbol] = {
    val run = new Run
    val filename = file.toString
    val parser = new syntaxAnalyzer.UnitParser(
      new CompilationUnit(
        new BatchSourceFile(filename, Source.fromFile(filename).mkString)
      )
    )
    val tree = parser.parse()
    tx(tree)
    Nil
  }
}
