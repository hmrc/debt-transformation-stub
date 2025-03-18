import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % "8.2.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % "1.6.0",
    "com.beachape"      %% "enumeratum-play-json"      % "1.8.2",
    "commons-io"         % "commons-io"                % "2.18.0",
    "com.nrinaudo"      %% "kantan.csv"                % "0.8.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"  % "8.2.0"   % Test,
    "org.mockito"            %% "mockito-scala-scalatest" % "1.17.37" % Test,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-30" % "1.6.0"   % Test,
    "org.scalatestplus.play" %% "scalatestplus-play"      % "5.1.0"   % Test
  )
}
