ThisBuild / version := "0.1.0"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .enablePlugins(DockerPlugin)
  .enablePlugins(JavaServerAppPackaging)
  .enablePlugins(JlinkPlugin)
  .enablePlugins(GraalVMNativeImagePlugin)
  .settings(
    name := "Users",
    idePackagePrefix := Some("me.alstepan"),
    assembly / mainClass := Some("me.alstepan.users.Server"),
    Docker / packageName := "Users",
    graalVMNativeImageGraalVersion := Some("21.3.0"),
    containerBuildImage := Some("ghcr.io/graalvm/graalvm-ce:java17-21.3.0"),
    graalVMNativeImageOptions ++= Seq(
      "--no-fallback",
      "--initialize-at-build-time",
      "--allow-incomplete-classpath",
      "--enable-http",
      "--enable-https",
      "--enable-all-security-services",
      "--static",
    )
  )

ThisBuild / assemblyMergeStrategy := {
  case "application.conf" => MergeStrategy.concat
  case PathList("META-INF", "versions", "9", "module-info.class") => MergeStrategy.discard
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}

libraryDependencies ++= Seq(
  // cats
  "org.typelevel" %% "cats-core"            % "2.7.0",
  "org.typelevel" %% "cats-effect"          % "3.3.4",
  // config
  "com.github.pureconfig" %% "pureconfig"   % "0.17.1",
  // circe
  "com.monovore"  %% "decline"              % "2.2.0",
  "io.circe"      %% "circe-core"           % "0.14.1",
  "io.circe"      %% "circe-parser"         % "0.14.1",
  "io.circe"      %% "circe-generic"        % "0.14.1",
  "io.circe"      %% "circe-literal"        % "0.14.1",
  // http4s
  "org.http4s"    %% "http4s-dsl"           % "0.23.7",
  "org.http4s"    %% "http4s-circe"         % "0.23.7",
  "org.http4s"    %% "http4s-blaze-server"  % "0.23.7",
  //postgres
  "org.postgresql" % "postgresql"           % "42.3.1",
  //doobie
  "org.tpolecat"  %% "doobie-core"          % "1.0.0-RC1",
  "org.tpolecat"  %% "doobie-hikari"        % "1.0.0-RC1",          // HikariCP transactor.
  "org.tpolecat"  %% "doobie-postgres"      % "1.0.0-RC1",
  "org.tpolecat"  %% "doobie-quill"         % "1.0.0-RC1",     // Postgres driver 42.3.1 + type mappings.
  // logging
  "ch.qos.logback" % "logback-classic"      % "1.3.0-alpha13",
  "org.typelevel" %% "log4cats-core"        % "2.2.0",
  "org.typelevel" %% "log4cats-slf4j"       % "2.2.0",
  //graalvm
  "org.scalameta" %% "svm-subs" % "20.2.0" % "compile-internal",

  //tests
  "org.scalactic" %% "scalactic"            % "3.2.10",
  "org.scalatest" %% "scalatest"            % "3.2.10" % Test,
  "org.typelevel" %% "cats-effect-testing-scalatest" % "1.4.0" % Test,
)
