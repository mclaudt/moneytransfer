import sbt.Keys.libraryDependencies
import sbt.inConfig

name := "money-transfer"

scalaVersion := "2.12.3"

resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.sonatypeRepo("snapshots")

val akkaVersion = "2.5.17"
val akkaHttpVersion = "10.1.3"
val slickVersion = "3.2.0"
val gatlingVersion = "3.0.0"


enablePlugins(GatlingPlugin)
enablePlugins(PackPlugin)

//explicit classpath in run script
packExpandedClasspath := true

//do not generate windows bat file
packGenerateWindowsBatFile := false


evictionWarningOptions in update := EvictionWarningOptions.default.withWarnTransitiveEvictions(false)

libraryDependencies ++= Seq(

  /* Akka dependencies */
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.megard" %% "akka-http-cors" % "0.3.1",

  /* Slick dependencies */
  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
  "com.h2database" % "h2" % "1.4.197",

  /* Testing */
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test",
  "org.scalatest" % "scalatest_2.12" % "3.0.5" % "it,test",

  /* Gatling dependencies */
  "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % Gatling,
  "io.gatling"            % "gatling-test-framework"    % gatlingVersion % Gatling,
  "org.codehaus.janino" % "janino" % "2.5.16" % Gatling,

  /* Logging */
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)

// configuration for integration tests
configs(IntegrationTest)
Defaults.itSettings

scalaSource in Gatling := sourceDirectory.value / "gatlingTest" / "scala"

resourceDirectory in Gatling := sourceDirectory.value / "gatlingTest" / "resources"

lazy val root = (project in file("."))
  .settings(
    inConfig(Gatling)(Defaults.testSettings)
)
