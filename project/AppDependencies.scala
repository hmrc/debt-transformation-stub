import play.core.PlayVersion
import sbt._

object AppDependencies {

  private val mongoLock = "7.0.0-play-28"

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % "4.2.0",
    "uk.gov.hmrc"             %% "simple-reactivemongo"       % "8.0.0-play-28"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % "4.2.0"             % Test,

    "org.scalatest"           %% "scalatest"                  % "3.2.5"             % Test,
    "com.typesafe.play"       %% "play-test"                  % PlayVersion.current % Test,
    "uk.gov.hmrc"                %% "reactivemongo-test"      % "5.0.0-play-28"     % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.36.8"            % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0"             % "test, it"
  )
}