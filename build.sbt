name := "simpleXmlRpc"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.1"

resolvers ++= Seq(
  "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases",
   "spray repo" at "http://repo.spray.io",
   "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
    "org.scalacheck" %% "scalacheck" % "1.10.1" % "test",
    "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.0.1" % "test",
    "net.databinder.dispatch" % "dispatch-core_2.10" % "0.10.0",
    "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
    "ch.qos.logback" % "logback-classic" % "1.0.9" % "test"
)

