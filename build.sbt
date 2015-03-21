name := """HomeDash"""

version := "0.9.17"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

resolvers += "apache.snapshots" at "http://repository.apache.org/snapshots/"

resolvers += "julman99-github" at "https://raw.github.com/julman99/mvn-repo/master"

    
libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
"mysql" % "mysql-connector-java" % "5.1.18",
"com.google.code.gson" % "gson" % "2.2.4",
    "org.json" % "json" % "20140107",
    "org.apache.httpcomponents" % "httpclient" % "4.3.4",
    "org.bitlet"%"weupnp"%"0.1.2",
    "com.googlecode.plist"%"dd-plist"%"1.8",
    "org.apache.commons"%"commons-imaging"%"1.0-SNAPSHOT",
    "org.apache.commons" % "commons-io" % "1.3.2",
    "org.twitter4j" % "twitter4j-core" % "4.0.2",
    "com.github.julman99" % "gson-fire" % "0.11.0"
)

