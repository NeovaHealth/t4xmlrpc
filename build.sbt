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
    "org.scalaz" %% "scalaz-core" % "7.0.0",
    "org.scalacheck" %% "scalacheck" % "1.10.1" % "test",
    "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.0.1" % "test",
    "net.databinder.dispatch" % "dispatch-core_2.10" % "0.10.0",
    "commons-codec" % "commons-codec" % "1.4",
    "org.clapper" % "grizzled-slf4j_2.10" % "1.0.1",
    "io.spray"            %   "spray-can"     % "1.2-M8",
    "io.spray"            %   "spray-routing" % "1.2-M8",
    "io.spray"            %   "spray-testkit" % "1.2-M8" % "test",
    "com.typesafe.akka"   %%  "akka-actor"    % "2.2.0-RC1",
    "com.typesafe.akka"   %%  "akka-testkit"  % "2.2.0-RC1" % "test"
)

