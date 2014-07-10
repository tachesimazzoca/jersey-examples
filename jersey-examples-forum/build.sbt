name := "jersey-examples-forum"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.10.2"

seq(webSettings :_*)

libraryDependencies ++= Seq(
  "com.sun.jersey" % "jersey-core" % "1.18.1",
  "com.sun.jersey" % "jersey-server" % "1.18.1",
  "com.sun.jersey" % "jersey-servlet" % "1.18.1",
  "com.sun.jersey.contribs" % "jersey-guice" % "1.18.1",
  "com.sun.jersey" % "jersey-json" % "1.18.1",
  "com.google.inject" % "guice" % "3.0",
  "com.google.inject.extensions" % "guice-servlet" % "3.0",
  "com.google.guava" % "guava" % "17.0",
  "org.freemarker" % "freemarker" % "2.3.20",
  "javax.servlet" % "servlet-api" % "2.5" % "provided",
  "org.eclipse.jetty" % "jetty-webapp" % "9.1.0.v20131115" % "container",
  "org.eclipse.jetty" % "jetty-plus"   % "9.1.0.v20131115" % "container",
  "junit" % "junit" % "4.10" % "test"
)

host in container.Configuration := "0.0.0.0"

port in container.Configuration := 9000 

