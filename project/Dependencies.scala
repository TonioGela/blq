import sbt._
import sbt.Keys._

object Dependencies {

  lazy val mainDependencies: Seq[ModuleID] = Seq(
    "org.typelevel"          %% "cats-core"                   % "2.4.1",
    "org.typelevel"          %% "cats-kernel"                 % "2.4.1",
    "com.chuusai"            %% "shapeless"                   % "2.3.3",
    "org.scodec"             %% "scodec-core"                 % "1.11.7",
    "org.scodec"             %% "scodec-bits"                 % "1.1.24",
    "com.monovore"           %% "decline"                     % "1.3.0",
    "com.zendesk"             % "mysql-binlog-connector-java" % "0.24.0",
    "org.scala-lang.modules" %% "scala-parallel-collections"  % "1.0.0"
  )

  lazy val testDependencies: Seq[ModuleID] = Seq("org.scalameta" %% "munit" % "0.7.22")
}
