name := "utils"
organization := "com.daemon"
version := "1.0.0"
scalaVersion := "2.11.8"
scalacOptions += "-deprecation"
scalacOptions += "-feature"


resolvers ++= Seq("RoundEights" at "http://maven.spikemark.net/roundeights")

libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.12.0"
libraryDependencies += "com.roundeights" %% "hasher" % "1.2.0"
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.39"
