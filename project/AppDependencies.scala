import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28" % "7.12.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"        % "0.68.0",
    "com.beachape"      %% "enumeratum-play-json"      % "1.7.2"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"  % "7.12.0"  % Test,
    "org.mockito"            %% "mockito-scala-scalatest" % "1.17.5" % Test,
    "com.typesafe.play"      %% "play-test"               % "2.8.8"  % Test,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28" % "0.68.0" % Test,
    "com.vladsch.flexmark"    % "flexmark-all"            % "0.36.8" % "test, it",
    "org.scalatestplus.play" %% "scalatestplus-play"      % "5.1.0"  % "test, it"
  )
}
