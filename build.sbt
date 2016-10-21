name := "AkkaSample"

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8", // yes, this is 2 args
  "-unchecked",
  "-Xlint",
  "-Ywarn-dead-code"
  // "-Xfuture" // breaks => Unit implicits
)

lazy val akkaVersion = "2.4.11"
lazy val log4jVersion = "2.7"
lazy val latest = "latest.integration"

ivyScala := ivyScala.value map {
  _.copy(overrideScalaVersion = true)
}

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.0.4",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "org.apache.logging.log4j" % "log4j-core" % log4jVersion,
  "org.apache.logging.log4j" % "log4j-api" % log4jVersion,
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4jVersion,
  "org.slf4s" %% "slf4s-api" % "1.7.13",
  "com.github.blemale" %% "scaffeine" % "1.1.0",
  "com.google.code.findbugs" % "jsr305" % "3.0.1",
  "junit" % "junit" % "4.12" % "test",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test"
)



testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")
