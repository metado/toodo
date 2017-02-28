name := "toodo-main"
organization := "me.chuwy"
scalaVersion in ThisBuild := "2.12.1"

lazy val root = (project in file("."))
  .aggregate(toodoJVM, toodoJS)
  .settings(
    publish := {},
    publishLocal := {}
  )

lazy val cross = (crossProject in file("."))
  .settings(
    libraryDependencies ++= Seq(
      "com.chuusai"   %%% "shapeless"           % Versions.shapeless,
      "org.typelevel" %%% "cats"                % Versions.cats,
      "io.circe"      %%% "circe-core"          % Versions.circe,
      "io.circe"      %%% "circe-generic"       % Versions.circe,
      "io.circe"      %%% "circe-parser"        % Versions.circe
    )
  )
  .jvmSettings(
    mainClass in run := Some("me.chuwy.toodo.App"),

    // For http4s snapshot
    resolvers +=
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",

    libraryDependencies ++= Seq(
      "co.fs2"        %% "fs2-core"             % Versions.fs2,
      "co.fs2"        %% "fs2-io"               % Versions.fs2,
      "co.fs2"        %% "fs2-cats"             % Versions.fs2Cats,
      "org.http4s"    %% "http4s-core"          % Versions.http4s,
      "org.http4s"    %% "http4s-dsl"           % Versions.http4s,
      "org.http4s"    %% "http4s-blaze-server"  % Versions.http4s,
      "org.http4s"    %% "http4s-circe"         % Versions.http4s,
      "org.tpolecat"  %% "doobie-core-cats"     % Versions.doobie,
      "org.tpolecat"  %% "doobie-postgres-cats" % Versions.doobie,
      "org.slf4j"     % "slf4j-simple"          % Versions.slf4j
    )
  )
  .jsSettings(
    mainClass in run := Some("me.chuwy.toodo.Spa"),


    libraryDependencies ++= Seq(
      "org.scala-js"      %%% "scalajs-dom"         % Versions.dom,
      "in.nvilla"         %%% "monadic-html"        % Versions.monadicHtml,
      "in.nvilla"         %%% "monadic-rx-cats"     % Versions.monadicHtml,
      "io.github.cquiroz" %%% "scala-java-time"     % Versions.scalaTime
    )
  )

lazy val toodoJVM = cross.jvm
  .settings(sharedSettings: _*)
  .settings(unmanagedResourceDirectories in Compile += baseDirectory.value / ".." / "static")
  // Remove scala.js target from file watch to prevent compilation loop
  .settings(watchSources := watchSources.value.filterNot(_.getPath.contains("target")))
  // Add web-client sources to file watch
  .settings(watchSources <++= (watchSources in toodoJS))
  // Make compile depend on Scala.js fast compilation
  .settings(compile <<= (compile in Compile) dependsOn (fastOptJS in Compile in toodoJS))
  // Make re-start depend on Scala.js fast compilation
  .settings(reStart <<= reStart dependsOn (fastOptJS in Compile in toodoJS))


lazy val toodoJS = cross.js
  .settings(sharedSettings: _*)
  .settings(Seq(fullOptJS, fastOptJS, packageJSDependencies, packageScalaJSLauncher, packageMinifiedJSDependencies)
    .map(task => crossTarget in (Compile, task) := file("static/content/target")))


lazy val sharedSettings = Seq(
  // File changes in `/static` should never trigger new compilation
  watchSources := watchSources.value.filterNot(_.getPath.contains("static")),
  scalacOptions := Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture")
)
