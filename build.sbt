name := "serverapp"

version := "0.1"

scalaVersion := "3.3.1"
val JacksonVersion = "2.16.1"
val AkkaVersion = "2.9.0"

resolvers += "Akka library repository".at("https://repo.akka.io/maven")

libraryDependencies ++= Seq(
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % JacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-databind" % JacksonVersion,
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
)

enablePlugins(SbtTwirl)

Compile/mainClass := Some("krecia.maciejnowicki.com.Main")