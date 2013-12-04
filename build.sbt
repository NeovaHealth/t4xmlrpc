name := "t4xmlrpc"

organization := "com.tactix4"

version := "1.1-SNAPSHOT"

scalaVersion := "2.10.2"

resolvers ++= Seq(
  "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases",
  "spray repo" at "http://repo.spray.io",
  "spray nightlies" at "http://nightlies.spray.io",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
  "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
  "org.scalacheck" %% "scalacheck" % "1.10.1" % "test",
  "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
  "ch.qos.logback" % "logback-classic" % "1.0.9" % "test"
)




osgiSettings

OsgiKeys.bundleSymbolicName := "Tactix4 XML-RPC"

OsgiKeys.importPackage ++= Seq(
    "net.databinder.dispatch.*",
    "io.netty.*",
    "com.ning.http.client.*",
    "*"
)

OsgiKeys.exportPackage ++= Seq(
    "com.tactix4.t4xmlrpc",
    "com.tactix4.t4xmlrpc.Exceptions"
)

