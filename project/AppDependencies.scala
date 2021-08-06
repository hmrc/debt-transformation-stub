import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % "5.3.0",
    "com.beachape"            %% "enumeratum-play-json"       % "1.6.1"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % "5.3.0"             % Test,
    
    "org.scalatest"           %% "scalatest"                  % "3.2.5"             % Test,
    "com.typesafe.play"       %% "play-test"                  % PlayVersion.current % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.36.8"            % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0"             % "test, it",
    "com.beachape"            %% "enumeratum-play-json"       % "1.6.1"             % "test, it"
  )
}
