//use for persistence actor testing
resolvers += "dnvriend" at "http://dl.bintray.com/dnvriend/maven"

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.6")

// Quality control
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")

// Scalariform
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.1")

// Test coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")