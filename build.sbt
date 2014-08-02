name := "jumpscala"

version := "1.0"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "com.twitter" % "util-core_2.10" % "6.18.0",
  "org.scala-lang" % "scala-reflect" % "2.10.2",
  "org.scala-lang" % "scala-compiler" % "2.10.2",
  "org.scala-lang" % "scala-library" % "2.10.2",
  "com.h2database" % "h2" % "1.4.180",
  "com.jsuereth" %% "scala-arm" % "1.3",
  "org.scalatest" %% "scalatest" % "2.2.0" % "test",
  "org.mockito" % "mockito-all" % "1.9.5" % "test"
)

fork := true

exportJars := true

packSettings

packMain := Map("jumpscala" -> "Jumper")

net.virtualvoid.sbt.graph.Plugin.graphSettings
