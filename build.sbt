lazy val root = (project in file("."))
  .settings(
    name          := "toodo",
    organization  := "me.chuwy",
    scalaVersion  := "2.12.1",

    // For http4s snapshot
    resolvers += 
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",

    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats"                 % Versions.cats,
      "com.chuusai"   %% "shapeless"            % Versions.shapeless,
      "co.fs2"        %% "fs2-core"             % Versions.fs2,
      "co.fs2"        %% "fs2-io"               % Versions.fs2,
      "co.fs2"        %% "fs2-cats"             % Versions.fs2Cats,
      "org.http4s"    %% "http4s-core"          % Versions.http4s,
      "org.http4s"    %% "http4s-dsl"           % Versions.http4s,
      "org.http4s"    %% "http4s-blaze-server"  % Versions.http4s,
      "org.http4s"    %% "http4s-circe"         % Versions.http4s,
      "org.tpolecat"  %% "doobie-core-cats"     % Versions.doobie,
      "org.tpolecat"  %% "doobie-postgres-cats" % Versions.doobie,
      "io.circe"      %% "circe-core"           % Versions.circe,
      "io.circe"      %% "circe-generic"        % Versions.circe,
      "io.circe"      %% "circe-parser"         % Versions.circe
    )
  )
