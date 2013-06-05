name := "simpleXmlRpc"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.1"

resolvers ++= Seq(
  "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases"
)


libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.0.0"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.10.1" % "test"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"

libraryDependencies += "org.scalamock" %% "scalamock-scalatest-support" % "3.0.1" % "test"

libraryDependencies +=  "net.databinder.dispatch" % "dispatch-core_2.10" % "0.10.0"

libraryDependencies +=  "commons-codec" % "commons-codec" % "1.4"

libraryDependencies += "org.clapper" % "grizzled-slf4j_2.10" % "1.0.1"
