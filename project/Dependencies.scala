import sbt._
import sbt.Keys._

object Dependencies {

  lazy val mainDependencies: Seq[ModuleID] = Seq(
    "org.typelevel"          %% "cats-core"                   % "2.6.1",
    "org.typelevel"          %% "cats-kernel"                 % "2.6.1",
    "com.chuusai"            %% "shapeless"                   % "2.3.6",
    "org.scodec"             %% "scodec-core"                 % "1.11.7",
    "org.scodec"             %% "scodec-bits"                 % "1.1.27",
    "com.monovore"           %% "decline"                     % "2.0.0",
    "com.zendesk"             % "mysql-binlog-connector-java" % "0.25.1",
    "org.scala-lang.modules" %% "scala-parallel-collections"  % "1.0.3"
  )

  lazy val testDependencies: Seq[ModuleID] = Seq("org.scalameta" %% "munit" % "0.7.26")
}
