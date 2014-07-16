name := "fintry2"

version := "1.0"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "com.twitter" %% "finagle-http" % "6.2.0",
  "com.twitter" %% "finagle-core" % "6.2.0",
  "org.scalatest" %% "scalatest" % "2.2.0",
  "org.scala-lang" % "scala-reflect" % "2.10.2",
  "org.scala-lang" % "scala-compiler" % "2.10.2",
  "org.scala-lang" % "scala-library" % "2.10.2"
)

fork in run := true
