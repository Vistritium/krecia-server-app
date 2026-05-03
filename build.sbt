name := "serverapp"

version := "0.1"

scalaVersion := "3.3.1"
val JacksonVersion = "2.16.1"
val AkkaVersion = "2.9.0"

resolvers += "Akka library repository".at("https://repo.akka.io/maven")

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

libraryDependencies ++= Seq(
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % JacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-databind" % JacksonVersion,
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % JacksonVersion,
  "com.google.guava" % "guava" % "33.0.0-jre",
  "org.apache.commons" % "commons-lang3" % "3.14.0",
  "commons-io" % "commons-io" % "2.15.1",
  "com.iheart" %% "ficus" % "1.5.2",
  "net.codingwell" %% "scala-guice" % "7.0.0",
  "com.google.inject.extensions" % "guice-assistedinject" % "7.0.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "ch.qos.logback" % "logback-classic" % "1.4.14",
  "net.harawata" % "appdirs" % "1.2.2",
  "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % "10.6.0",
  "com.lightbend.akka" %% "akka-stream-alpakka-mqtt" % "7.0.1",
  "org.reflections" % "reflections" % "0.10.2",
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.github.pjfanning" %% "jackson-module-scala3-enum" % "2.16.0",
  "com.twilio.sdk" % "twilio" % "9.15.0",
  "com.vonage" % "server-sdk" % "8.1.0",
  "org.scalatest" %% "scalatest" % "3.2.17" % Test,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test
)

enablePlugins(SbtTwirl)

Compile/mainClass := Some("krecia.maciejnowicki.com.Main")

dockerBaseImage := "eclipse-temurin:21-alpine"
dockerRepository := Some("server.krecia.maciejnowicki.com:5000")
dockerExposedPorts := Seq(8080)
dockerUpdateLatest := true