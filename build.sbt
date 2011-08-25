name := "scala-exec"

version := "1.0-SNAPSHOT"

scalaVersion := "2.9.0-1"

crossScalaVersions := Seq("2.9.0-1")

libraryDependencies += "junit" % "junit" % "4.8" % "test"

libraryDependencies += "org.specs2" %% "specs2" % "1.5"

libraryDependencies += "org.specs2" %% "specs2-scalaz-core" % "6.0.RC2" % "test"

resolvers += "Scala-Tools Snapshots" at "http://scala-tools.org/repo-snapshots"

resolvers += "Scala-Tools Releases" at "http://scala-tools.org/repo-releases"
