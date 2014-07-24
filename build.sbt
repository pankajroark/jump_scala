name := "jumpscala"

version := "1.0"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "com.twitter" %% "finagle-http" % "6.2.0",
  "com.twitter" %% "finagle-core" % "6.2.0",
  "org.scalatest" %% "scalatest" % "2.2.0",
  "org.scala-lang" % "scala-reflect" % "2.10.2",
  "org.scala-lang" % "scala-compiler" % "2.10.2",
  "org.scala-lang" % "scala-library" % "2.10.2",
  "com.h2database" % "h2" % "1.4.180",
  "com.jsuereth" %% "scala-arm" % "1.3",
  "org.mockito" % "mockito-all" % "1.9.5" % "test"
)

fork := true

exportJars := true

packSettings

packMain := Map("jumpscala" -> "Hi")
