name := "scala_test_tools"

organization := "com.github.jasonqu"

version := "0.0.1"

scalaVersion := "2.11.4"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.12.0" % "test"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2-core" % "2.4.14" % "test"
)

scalacOptions in Test ++= Seq("-Yrangepos")

// Read here for optional jars and dependencies:
// http://etorreborre.github.io/specs2/guide/org.specs2.guide.Runners.html#Dependencies

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)