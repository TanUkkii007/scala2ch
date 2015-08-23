resolvers += "Flyway" at "http://flywaydb.org/repo"

addSbtPlugin("org.flywaydb" % "flyway-sbt" % "3.2.1")

libraryDependencies += "com.h2database" % "h2" % "1.4.187"

addSbtPlugin("org.scalikejdbc" %% "scalikejdbc-mapper-generator" % "2.2.7")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")