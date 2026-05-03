addSbtPlugin("org.playframework.twirl" % "sbt-twirl" % "2.0.3")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.16")

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always