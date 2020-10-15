import Dependencies._
import Settings._

Global / onChangedBuildSource := ReloadOnSourceChanges
Global / showSuccess := false
Global / excludeLintKeys += showSuccess

inThisBuild(Seq(
  scalaVersion := "2.13.3",
  organization := "dev.toniogela.blq",
  organizationName := "toniogela.dev",
  version := "0.1.0-SNAPSHOT"
))

inThisBuild(compilerPlugins.map(addCompilerPlugin) ++ scalaFixSettings)

lazy val blq = (project in file(".")).settings(
  commonSettings,
  name := "blq",
  Compile / mainClass := Some("dev.toniogela.blq.Main"),
  headerLicense := Some(HeaderLicense.MIT("2020", "Antonio Gelameris", HeaderLicenseStyle.SpdxSyntax)),
  libraryDependencies ++= (mainDependencies ++ testDependencies.map(_ % Test))
).enablePlugins(NativeImagePlugin)
