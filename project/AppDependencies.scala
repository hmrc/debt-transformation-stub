import sbt._

object AppDependencies {

  val bootstrapPlay = "9.11.0"
  val hmrcMongoPlay = "2.6.0"

  val compile: Seq[ModuleID] = Seq(
    // This is necessary until the HMRC/Play dependencies bring in the version of Jackson that is not insecure.
    "com.fasterxml.jackson.core" % "jackson-core"              % "2.19.2",
    "uk.gov.hmrc"               %% "bootstrap-backend-play-30" % bootstrapPlay,
    "uk.gov.hmrc.mongo"         %% "hmrc-mongo-play-30"        % hmrcMongoPlay,
    "com.beachape"              %% "enumeratum-play-json"      % "1.9.0",
    "commons-io"                 % "commons-io"                % "2.21.0",
    "com.nrinaudo"              %% "kantan.csv"                % "0.8.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"  % bootstrapPlay % Test,
    "org.mockito"            %% "mockito-scala-scalatest" % "2.0.0"     % Test,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-30" % hmrcMongoPlay % Test,
    "org.scalatestplus.play" %% "scalatestplus-play"      % "7.0.2"       % Test
  )
}
