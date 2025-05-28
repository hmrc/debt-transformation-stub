import sbt._

object AppDependencies {

  val bootstrapPlay = "9.11.0"
  val hmrcMongoPlay = "2.6.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapPlay,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % hmrcMongoPlay,
    "com.beachape"      %% "enumeratum-play-json"      % "1.8.2",
    "commons-io"         % "commons-io"                % "2.19.0",
    "com.nrinaudo"      %% "kantan.csv"                % "0.8.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"  % bootstrapPlay % Test,
    "org.mockito"            %% "mockito-scala-scalatest" % "1.17.37"     % Test,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-30" % hmrcMongoPlay % Test,
    "org.scalatestplus.play" %% "scalatestplus-play"      % "7.0.1"       % Test
  )
}
