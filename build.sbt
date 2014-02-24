name := "t4xmlrpc"

organization := "com.tactix4"

version := "2.0"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.0.5",
  "org.scalaz" %% "scalaz-xml" % "7.0.5",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
  "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
  "org.scalacheck" %% "scalacheck" % "1.10.1" % "test",
  "org.scalatest" %% "scalatest" % "2.0" % "test",
  "ch.qos.logback" % "logback-classic" % "1.0.9" % "test",
  "com.github.axel22" %% "scalameter" % "0.4" % "test"
)

testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework")

initialCommands in console :=
  """import com.tactix4.t4xmlrpc._;
    |import com.tactix4.t4xmlrpc.util.XMLRPCResponseGenerator
    |import scalaz._
    |import Scalaz._
    |import scalaz.xml.Xml._
  """.stripMargin

parallelExecution in Test := false

osgiSettings

OsgiKeys.bundleSymbolicName := "Tactix4 XML-RPC Client"

OsgiKeys.importPackage ++= Seq(
    "net.databinder.dispatch.*",
    "io.netty.*",
    "com.ning.http.client.*",
    "*"
)

OsgiKeys.exportPackage ++= Seq(
    "com.tactix4.t4xmlrpc"
)

