name := "scala2ch"

scalaVersion := "2.11.7"

libraryDependencies ++= {
  val akkaVersion       = "2.3.12"
  val sprayVersion      = "1.3.3"
  val akkaStreamVersion = "1.0"
  Seq(
    "com.typesafe.akka" %% "akka-actor"      % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j"      % akkaVersion,
    "com.typesafe.akka" %% "akka-agent"      % akkaVersion,
    "com.typesafe.akka" %%  "akka-testkit"   % akkaVersion   % "test",
    "com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamVersion,
    "com.typesafe.akka" %% "akka-http-core-experimental" % akkaStreamVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaStreamVersion,
    "com.typesafe.akka" %% "akka-http-xml-experimental" % akkaStreamVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStreamVersion,
    "io.spray"          %% "spray-json"      % "1.3.2",
    "org.scalatest"     %% "scalatest"       % "2.2.4"       % "test",
    "ch.qos.logback"  %  "logback-classic"   % "1.1.3",
    "org.scalikejdbc" %% "scalikejdbc"       % "2.2.7",
    "org.scalikejdbc" %% "scalikejdbc-test"  % "2.2.7"   % "test",
    "com.h2database"  %  "h2"                % "1.4.187"
  )
}

scalacOptions in ThisBuild ++= Seq("-feature", "-deprecation", "-unchecked", "-encoding", "UTF-8", "-language:postfixOps", "-nobootcp")

Revolver.settings

lazy val genH2 = taskKey[Seq[java.io.File]]("generate migration scripts for H2")

genH2 := {
  val targetDir: File = (resourceManaged in Test).value / "migration/h2"
  val resourceFile = ((resourceDirectory in Compile).value ** "*.sql").get
  resourceFile.map { file =>
    val replaced = IO.read(file).replaceAll("""COMMENT[\s=]+'.*'""", "").replaceAll("""FLOAT\(.*\)""", "FLOAT")
    val after = targetDir / file.name
    IO.write(after, replaced)
    after
  }
}

lazy val testMigrate = taskKey[Unit]("migrate schema for H2 database")

testMigrate := {
  genH2.value
  flywayMigrate.value
}

seq(flywaySettings: _*)

flywayUrl := System.getProperty("flyway.url", "jdbc:h2:file:./target/h2db;MODE=MySQL;INIT=RUNSCRIPT FROM './src/main/resources/migration/init.sql';")

flywayUrl in ITest := "jdbc:h2:file:./target/h2db"  //#891 flywayがまだ未対応

flywayUser := "root"

flywayPassword := "passwd"

flywaySchemas := Seq("scala2ch")

flywayLocations := Seq(s"filesystem:${(resourceManaged in Test).value}/migration/h2")

lazy val ITest = config("it") extend(Test)

inConfig(ITest)(Defaults.testSettings)

def itFilter(name: String): Boolean = name endsWith "ITest"

def unitFilter(name: String): Boolean = !itFilter(name)

testOptions in Test += Tests.Filter(unitFilter)

testOptions in ITest ++= Seq(
  Tests.Filter(itFilter),
  Tests.Setup{() =>
    testMigrate.value
  },
  Tests.Cleanup(() => {})
)

parallelExecution in ITest := false

fork in (ITest, test) := true

fork in (ITest, testOnly) := true

fork in (ITest, testQuick) := true

scalikejdbcSettings