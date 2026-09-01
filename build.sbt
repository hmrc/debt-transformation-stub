val appName = "debt-transformation-stub"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.18"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    scalacOptions ++= Seq(
      "-Xlint:-byname-implicit",
      "-Wconf:src=routes/.*:s",
      "-Wconf:cat=unused-imports&src=html/.*:s"
    ),
    // fixes the funkiness that tries to 'upgrade' to an old version
    dependencyUpdatesFilter -= moduleFilter(organization = "commons-io", name = "commons-io", revision = "20030203.000550")
  )
  .settings(resolvers += Resolver.jcenterRepo)
  .disablePlugins(JUnitXmlReportPlugin)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice)
  .settings(libraryDependencies ++= AppDependencies.test)
