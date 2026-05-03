name := "serverapp"

version := "0.1"

scalaVersion := "3.8.3"
val JacksonVersion = "2.21.3"
val PekkoVersion = "1.6.0"
val PekkoHttpVersion = "1.3.0"
val PekkoConnectorsVersion = "1.3.0"

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

libraryDependencies ++= Seq(
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % JacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-databind" % JacksonVersion,
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % JacksonVersion,
  "com.google.guava" % "guava" % "33.6.0-jre",
  "org.apache.commons" % "commons-lang3" % "3.20.0",
  "commons-io" % "commons-io" % "2.22.0",
  "com.iheart" %% "ficus" % "1.5.2",
  "net.codingwell" %% "scala-guice" % "7.0.0",
  "com.google.inject.extensions" % "guice-assistedinject" % "7.0.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.6",
  "ch.qos.logback" % "logback-classic" % "1.5.32",
  "net.harawata" % "appdirs" % "1.5.0",
  "org.apache.pekko" % "pekko-actor_2.13" % PekkoVersion,
  "org.apache.pekko" % "pekko-stream_2.13" % PekkoVersion,
  "org.apache.pekko" % "pekko-slf4j_2.13" % PekkoVersion,
  "org.apache.pekko" % "pekko-http_2.13" % PekkoHttpVersion,
  "org.apache.pekko" % "pekko-connectors-mqtt_2.13" % PekkoConnectorsVersion,
  "org.reflections" % "reflections" % "0.10.2",
  "org.apache.pekko" % "pekko-actor-typed_2.13" % PekkoVersion,
  "com.twilio.sdk" % "twilio" % "12.0.0",
  "com.vonage" % "server-sdk" % "9.10.2",
  "org.scalatest" %% "scalatest" % "3.2.20" % Test,
  "org.apache.pekko" % "pekko-actor-testkit-typed_2.13" % PekkoVersion % Test
)

enablePlugins(SbtTwirl)

Compile/mainClass := Some("krecia.maciejnowicki.com.Main")

dockerBaseImage := "eclipse-temurin:21-alpine"
dockerRepository := Some("server.krecia.maciejnowicki.com:5000")
dockerExposedPorts := Seq(8080)
dockerUpdateLatest := true
