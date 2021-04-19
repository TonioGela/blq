import sbt._
import sbt.Keys._

object Dependencies {

  lazy val mainDependencies: Seq[ModuleID] = Seq(
    "org.typelevel"          %% "cats-core"                   % "2.5.0",
    "org.typelevel"          %% "cats-kernel"                 % "2.5.0",
    "com.chuusai"            %% "shapeless"                   % "2.3.4",
    "org.scodec"             %% "scodec-core"                 % "1.11.7",
    "org.scodec"             %% "scodec-bits"                 % "1.1.25",
    "com.monovore"           %% "decline"                     % "2.0.0",
    "com.zendesk"             % "mysql-binlog-connector-java" % "0.25.0",
    "org.scala-lang.modules" %% "scala-parallel-collections"  % "1.0.2"
  )

  lazy val testDependencies: Seq[ModuleID] = Seq("org.scalameta" %% "munit" % "0.7.25")
}
