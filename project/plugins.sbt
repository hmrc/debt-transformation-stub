resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
resolvers += Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(
  Resolver.ivyStylePatterns
)
resolvers += Resolver.typesafeRepo("releases")

addSbtPlugin("uk.gov.hmrc"       % "sbt-auto-build"     % "3.15.0")
addSbtPlugin("uk.gov.hmrc"       % "sbt-distributables" % "2.4.0")
addSbtPlugin("com.typesafe.play" % "sbt-plugin"         % "2.8.21")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt"       % "2.4.3")

/* Suppress the following clashes (04/12/2023):
 *
 * org.scala-lang.modules:scala-xml_2.12:2.1.0 (early-semver) is selected over {1.2.0, 1.1.1}
 *   +- org.scala-lang:scala-compiler:2.12.17                                          (depends on 2.1.0)
 *   +- com.github.sbt:sbt-native-packager:1.9.16  (scalaVersion=2.12, sbtVersion=1.0) (depends on 2.1.0)
 *   +- com.typesafe.sbt:sbt-native-packager:1.5.2 (scalaVersion=2.12, sbtVersion=1.0) (depends on 1.1.1)
 *   +- com.typesafe.play:twirl-api_2.12:1.5.1                                         (depends on 1.2.0)
 */
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
