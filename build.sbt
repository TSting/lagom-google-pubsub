import sbt.Keys.developers
import sbt.url
import sbtrelease.ReleaseStateTransformations._
import xerial.sbt.Sonatype._
import xerial.sbt.Sonatype.autoImport.sonatypeProjectHosting

organization in ThisBuild := "com.elegantmonkeys"
scalaVersion in ThisBuild := "2.12.4"

useGpg in ThisBuild := true

lazy val settings: SettingsDefinition = Seq(
  // Add sonatype repository settings
  publishTo := sonatypePublishTo.value,
  // To sync with Maven central, you need to supply the following information:
  publishMavenStyle := true,
  // License of your choice
  licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  // Where is the source code hosted
  sonatypeProjectHosting := Some(GitHubHosting("elegantmonkeys", "lagom-google-pubsub", "contact@elegantmonkeys")),
  developers := List(Developer(id = "lagom-google-pubsub",
    name = "Lagom Google Pubsub Contributors",
    email = null,
    url = null))
)

val lagomVersion = "1.4.2"

val slf4j = "org.slf4j" % "log4j-over-slf4j" % "1.7.25"
val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
val scalatest = "org.scalatest" %% "scalatest" % "3.0.4"
val lagomApi = "com.lightbend.lagom" %% "lagom-api" % lagomVersion
val lagomApiJavaDsl = "com.lightbend.lagom" %% "lagom-javadsl-api" % lagomVersion
val lagomApiScalaDsl = "com.lightbend.lagom" %% "lagom-scaladsl-api" % lagomVersion
val lagomPersistenceCore = "com.lightbend.lagom" %% "lagom-persistence-core" % lagomVersion
val lagomJavadslBroker = "com.lightbend.lagom" %% "lagom-javadsl-broker" % lagomVersion
val lagomJavadslServer = "com.lightbend.lagom" %% "lagom-javadsl-server" % lagomVersion
val lagomScaladslBroker = "com.lightbend.lagom" %% "lagom-scaladsl-broker" % lagomVersion
val lagomScaladslServer = "com.lightbend.lagom" %% "lagom-scaladsl-server" % lagomVersion
val pubsubSdk = "com.google.cloud" % "google-cloud-pubsub" % "0.43.0-beta"

val pubsubProjects = Seq[Project](
  `client`, `client-scaladsl`, `client-javadsl`,
  `server`, `server-scaladsl`, `server-javadsl`
)

lazy val root = (project in file("."))
  .settings(
    name := "lagom-google-pubsub",
    publishLocal := {},
    publishArtifact in Compile := false,
    publish := {},
    settings
  )
  .aggregate(pubsubProjects.map(Project.projectToRef): _*)

lazy val `client` = (project in file("service/core/pubsub/client"))
  .settings(
    name := "lagom-google-pubsub-client",
    libraryDependencies ++= Seq(
      slf4j,
      lagomApi,
      scalatest % Test,
      pubsubSdk
    ),
    settings
  )

lazy val `client-scaladsl` = (project in file("service/scaladsl/pubsub/client"))
  .settings(
    name := "lagom-scaladsl-google-pubsub-client",
    libraryDependencies ++= Seq(
      slf4j,
      lagomApiScalaDsl,
      scalatest % Test,
      logback % Test
    ),
    settings
  )
  .dependsOn(`client`)

lazy val `client-javadsl` = (project in file("service/javadsl/pubsub/client"))
  .settings(
    name := "lagom-javadsl-google-pubsub-client",
    libraryDependencies ++= Seq(
      slf4j,
      lagomJavadslServer,
      scalatest % Test,
      logback % Test
    ),
    settings
  )
  .dependsOn(`client`)

lazy val `server` = (project in file("service/core/pubsub/server"))
  .settings(
    name := "lagom-google-pubsub-broker",
    libraryDependencies ++= Seq(
      slf4j,
      pubsubSdk,
      lagomApi,
      lagomPersistenceCore
    ),
    settings
  )
  .dependsOn(`client`)

lazy val `server-scaladsl` = (project in file("service/scaladsl/pubsub/server"))
  .settings(
    name := "lagom-scaladsl-google-pubsub-broker",
    libraryDependencies ++= Seq(
      slf4j,
      lagomScaladslBroker,
      lagomScaladslServer,
      scalatest % Test,
      logback % Test
    ),
    settings
  )
  .dependsOn(`server`, `client-scaladsl`)

lazy val `server-javadsl` = (project in file("service/javadsl/pubsub/server"))
  .settings(
    name := "lagom-javadsl-google-pubsub-broker",
    libraryDependencies ++= Seq(
      slf4j,
      lagomJavadslBroker,
      lagomJavadslServer,
      scalatest % Test,
      logback % Test
    ),
    settings
  )
  .dependsOn(`server`, `client-javadsl`)

releaseCommitMessage := s"chore: set version to ${(version in ThisBuild).value}"

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommand("publishSigned"),
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeReleaseAll"),
  pushChanges
)