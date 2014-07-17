name := "jersey-examples-forum"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.10.2"

seq(webSettings :_*)

libraryDependencies ++= Seq(
  "com.sun.jersey" % "jersey-core" % "1.18.1",
  "com.sun.jersey" % "jersey-server" % "1.18.1",
  "com.sun.jersey" % "jersey-servlet" % "1.18.1",
  "com.google.guava" % "guava" % "17.0",
  "commons-lang" % "commons-lang" % "2.6",
  "commons-io" % "commons-io" % "2.4",
  "commons-codec" % "commons-codec" % "1.9",
  "org.apache.commons" % "commons-email" % "1.3.3",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.1",
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % "2.4.1",
  "com.typesafe" % "config" % "1.2.1",
  "org.freemarker" % "freemarker" % "2.3.20",
  "javax.el" % "javax.el-api" % "2.2.4",
  "org.glassfish.web" % "javax.el" % "2.2.4",
  "org.hibernate" % "hibernate-validator" % "5.1.1.Final",
  "org.hibernate" % "hibernate-core" % "4.3.5.Final",
  "org.hibernate" % "hibernate-entitymanager" % "4.3.5.Final",
  "com.h2database" % "h2" % "1.4.179",
  "javax.servlet" % "servlet-api" % "2.5" % "provided",
  "org.eclipse.jetty" % "jetty-webapp" % "9.1.0.v20131115" % "container",
  "org.eclipse.jetty" % "jetty-plus"   % "9.1.0.v20131115" % "container",
  "junit" % "junit" % "4.10" % "test"
)

host in container.Configuration := "0.0.0.0"

port in container.Configuration := 9000 

