import AssemblyKeys._ // put this at the top of the file

import com.typesafe.sbt.SbtStartScript

assemblySettings

seq(SbtStartScript.startScriptForClassesSettings: _*)

seq(Revolver.settings: _*)


name := "pi-server"

organization := "up"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.3"

scalacOptions := Seq("-encoding", "utf8",
                     "-target:jvm-1.6",
                     "-feature",
                     "-language:implicitConversions",
                     "-language:postfixOps",
                     "-unchecked",
                     "-deprecation",
                     "-Xlog-reflective-calls"
                    )

//unmanagedBase := baseDirectory.value / "lib"
unmanagedBase <<= baseDirectory { base => base / "lib" }

mainClass := Some("org.up.pi.Main")

resolvers ++= Seq("Sonatype Releases"   at "http://oss.sonatype.org/content/repositories/releases",
                  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
                  "Sonatype OSS Maven Repository" at "https://oss.sonatype.org/content/groups/public",
                  "Spray Repository"    at "http://repo.spray.io/",
                  "Spray Nightlies"     at "http://nightlies.spray.io/",
                  "Base64 Repo"         at "http://dl.bintray.com/content/softprops/maven")

libraryDependencies ++= {
  val akkaVersion  = "2.2.3"
  val sprayVersion = "1.2.0"
  Seq(
    "com.typesafe.akka"       %%  "akka-actor"             % akkaVersion,
    "com.typesafe.akka"       %%  "akka-slf4j"             % akkaVersion,
    "io.spray"                %   "spray-caching"          % sprayVersion,
    "io.spray"                %   "spray-can"              % sprayVersion,
    "io.spray"                %   "spray-client"           % sprayVersion,
    "io.spray"                %   "spray-routing"          % sprayVersion,
    "io.spray"                %%  "spray-json"             % "1.2.5",
    "me.lessis"               %%  "base64"                 % "0.1.0",
    "com.github.nscala-time"  %%  "nscala-time"            % "0.4.2",
    "ch.qos.logback"          %   "logback-classic"        % "1.0.12",
    "com.pi4j"	              %   "pi4j-core"		       % "1.0-SNAPSHOT",
    "org.rxtx"	              %   "rxtx"		           % "2.1.7",
    "org.apache.commons"      %   "commons-email"          % "1.2",
    "joda-time" 			  %   "joda-time" 			   % "2.0",   
	"org.joda" 				  %   "joda-convert" 		   % "1.1",
	"dumbster" 				  %   "dumbster" 			   % "1.6",
    "junit"                   %   "junit"                  % "4.7" % "test",
    "com.typesafe.akka"       %%  "akka-testkit"           % akkaVersion    % "test",
    "io.spray"                %   "spray-testkit"          % sprayVersion   % "test",
    "org.specs2"              %%  "specs2"                 % "2.1.1"        % "test"
)
}

EclipseKeys.withSource := true

site.settings

site.sphinxSupport()
