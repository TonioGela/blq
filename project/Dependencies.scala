import sbt._
import sbt.Keys._

object Dependencies {

  lazy val mainDependencies: Seq[ModuleID] =
    Seq("com.monovore" %% "decline" % "1.3.0", "com.zendesk" % "mysql-binlog-connector-java" % "0.23.3")

  lazy val testDependencies: Seq[ModuleID] = Seq.empty[ModuleID]
}
