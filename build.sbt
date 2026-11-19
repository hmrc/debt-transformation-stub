val appName = "debt-transformation-stub"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.3.7"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    scalacOptions ++= Seq(
      "-source:future-migration",
      "-Yretain-trees",
      "-Wconf:src=routes/.*:s"
    ),
    // fixes the funkiness that tries to 'upgrade' to an old version
    dependencyUpdatesFilter -= moduleFilter(
      organization = "commons-io",
      name = "commons-io",
      revision = "20030203.000550"
    )
  )
  .settings(resolvers += Resolver.jcenterRepo)
  .disablePlugins(JUnitXmlReportPlugin)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice)
  .settings(libraryDependencies ++= AppDependencies.test)
