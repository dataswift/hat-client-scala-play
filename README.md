# Scala API wrappers for the HAT

This repository provides convenience wrappers around HAT HTTP APIs and contains 
the most up-to-date set of typesafe HAT Data Models and Play-JSON based
serializers and deserializers for them.

It relies on Play-WS for an asynchronous HTTP client.

## Usage

The library artifacts are hosted on AWS S3:
 
    resolvers += "HAT Library Artifacts Releases" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-releases.hubofallthings.com"
    // Or for SNAPSHOTS:
    // "HAT Library Artifacts Snapshots" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-snapshots.hubofallthings.com"
    libraryDependencies ++= "org.hatdex" %% "hat-client-scala-play" % X.Y.Z

To use the client, it is sufficient to create a new one with minimal configuration:

    new HatClient(wsClient, hatAddress, schema)

Where:

- wsClient is an instance of the WS Client, ideally dependency-injected in most cases - check Play documentation for details
- hatAddress is the fully-qualified domain name of the HAT (e.g. example.hubofallthings.net)
- schema is the schema of the address, can either be "http://" (only for development environments) or "https://" (the default if you omit the parameter)

The client is non-blocking and is built around standard Scala Futures. For example, to authenticate with a HAT and request data from it you would then run:

    val hat = new HatClient(wsClient, hatAddress, schema)
    for {
      accessToken <- hat.authenticateForToken(dataShopperUsername, dataShopperPassword)
      values <- hat.dataDebitValues(dataDebitId)
    } yield values
