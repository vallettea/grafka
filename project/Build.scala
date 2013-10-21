import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object GrafkaBuild extends Build {

    lazy val sharedSettings = Project.defaultSettings ++ Seq(assemblySettings: _*) ++ Seq(
        
        name := "grafka",
        description := "Reactive graph library using Akka",
        organizationName := "Snips",
        organization := "net.snips",
        version := "0.1.0",
        scalaVersion := "2.10.2",

        libraryDependencies ++= Seq(
            "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
            "org.scala-tools.testing" %% "specs" % "1.6.9" % "test",
            "org.scalatest" %% "scalatest" % "1.9.2" % "test",
            "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
            "com.typesafe.akka" %% "akka-actor" % "2.2.1"
        ),

        resolvers ++= Seq(
            Opts.resolver.sonatypeSnapshots,
            Opts.resolver.sonatypeReleases,
            "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
        ),
        
        parallelExecution in Test := false, // until scalding 0.9.0 we can't do this
        
        scalacOptions ++= Seq(
            "-unchecked",
            "-deprecation",
            "-feature",
            "-language:implicitConversions",
            "-language:reflectiveCalls",
            "-language:postfixOps",
            "-Yresolve-term-conflict:package"
        ),

        publishMavenStyle := true,
        pomIncludeRepository := { x => false },
        publishArtifact in Test := false,
        exportJars := true,
        javaOptions += "-Xmx5G",

        //Assembly settings
        jarName in assembly := "grafka-example.jar",

        mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
            case "application.conf" => MergeStrategy.concat
            case x => old(x)
        }}
    )

    lazy val grafka = Project(
        id = "grafka",
        base = file("."),
        settings = sharedSettings
    ).settings(
        test := { },
        publish := { },
        publishLocal := { }
    ).aggregate(
        core,
        example
    )
    
    def module(name: String) = {
        val id = "grafka-%s".format(name)
        Project(id = id, base = file(id), settings = sharedSettings ++ Seq(Keys.name := id))
    }

    lazy val core = module("core")

    lazy val example = module("example").dependsOn(core).settings(
        mainClass in (Compile, run) := Some("net.snips.grafka.example.Main"),
        mainClass in assembly := Some("net.snips.grafka.example.Main")
    )
}

