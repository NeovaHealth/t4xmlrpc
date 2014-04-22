import com.typesafe.sbt.osgi.OsgiKeys
import com.typesafe.sbt.osgi.SbtOsgi.osgiSettings

name := "t4xmlrpc"

organization := "com.tactix4"

version := "2.0.1"

scalaVersion := "2.10.3"

publishMavenStyle := true

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.0.6",
  "org.scalaz" %% "scalaz-xml" % "7.0.6",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
  "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
  "org.scalacheck" %% "scalacheck" % "1.10.1" % "test",
  "org.scalatest" %% "scalatest" % "2.0" % "test",
  "ch.qos.logback" % "logback-classic" % "1.0.9" % "test",
  "com.github.axel22" %% "scalameter" % "0.4" % "test"
)



pomExtra := (
  <url>https://github.com/Tactix4/t4xmlrpc</url>
  <licenses>
    <license>
      <name>AGPL</name>
        <url>http://www.gnu.org/licenses/agpl-3.0.html</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:Tactix4/t4xmlrpc.git</url>
    <connection>scm:git:git@github.com:Tactix4/t4xmlrpc.git</connection>
  </scm>
  <developers>
    <developer>
      <name>Max Worgan</name>
      <email>max@tactix4.com</email>
      <organization>Tactix4 Ltd</organization>
      <organizationUrl>http://www.tactix4.com</organizationUrl>
    </developer>
  </developers>)

testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework")

initialCommands in console in Test :=
  """import com.tactix4.t4xmlrpc._;
    |import com.tactix4.t4xmlrpc.util.XMLRPCResponseGenerator
    |import scalaz._
    |import Scalaz._
    |import scalaz.xml.Xml._
  """.stripMargin

parallelExecution in Test := false

osgiSettings

OsgiKeys.importPackage ++= Seq(
    "scalaz.*",
    "scalaz.xml.*",
    "com.typesafe.scalalogging.*",
    "net.databinder.dispatch.*",
    "io.netty.*",
    "com.ning.http.client.*",
    "*"
)

OsgiKeys.exportPackage ++= Seq(
    "com.tactix4.t4xmlrpc"
)
