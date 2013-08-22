name := "t4XmlRpc"

version := "1.0"

scalaVersion := "2.10.2"

resolvers ++= Seq(
  "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases",
  "spray repo" at "http://repo.spray.io",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
    "com.stackmob" %% "newman" % "0.22.0",
    "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
    "org.scalacheck" %% "scalacheck" % "1.10.1" % "test",
    "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.0.1" % "test",
    "ch.qos.logback" % "logback-classic" % "1.0.9" % "test"
)

