import sbt.Keys.resolvers

name := "slick-integrator"
organization in ThisBuild := "com.slick.init"
version in ThisBuild := "1.0.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.8"
lazy val `lms` = (project in file(".")).aggregate(`lms-api`, `lms-impl`)

//Define the external service’s host and port name.
lagomUnmanagedServices in ThisBuild := Map("lms-service" -> "http://13.126.58.168/api")
lagomCassandraEnabled in ThisBuild := false
lagomKafkaEnabled in ThisBuild := false
//lagomServiceLocatorPort in ThisBuild := 8181
//lagomServiceGatewayPort in ThisBuild := 9100


lazy val `lms-api` = (project in file("lms-api")).settings(libraryDependencies ++= Seq(lagomScaladslApi))

lazy val `lms-impl` = (project in file("lms-impl"))
  .enablePlugins(LagomScala)
  .enablePlugins(JavaAppPackaging)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslTestKit,
      lagomJavadslClient,
      lagomScaladslPersistenceJdbc,
      "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided",
      "org.scalatest" %% "scalatest" % "3.0.5" % Test,
      "com.amazonaws" % "aws-java-sdk-s3" % "1.11.117",
      "com.amazonaws" % "aws-java-sdk-sqs" % "1.11.456",
      "com.google.code.gson" % "gson" % "2.8.1",
      "com.typesafe.slick" %% "slick" % "3.2.1"
      , "com.loanframe" % "lf-data-models_2.12" % "1.0.0-SNAPSHOT"
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .settings(
    resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
    , resolvers += Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns)
    , resolvers += "Nexus" at s"$nexus_url/repository/lf-repo/"
    , credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
  )
  .dependsOn(`lms-api`)
val nexus_url = "https://staging-repo.loanframe.com/"
