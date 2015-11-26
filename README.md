
# Neova Health XML-RPC
[![Build Status](https://travis-ci.org/NeovaHealth/t4xmlrpc.svg?branch=master)](https://travis-ci.org/NeovaHealth/t4xmlrpc)

***

This library provides a simple and easy to use xml-rpc implementation in scala.

####Changelog####

### 2.0.2 Release ###
This is a simplification and updating of the 2.0 release, removing custom data structures in favour of existing
ones and removing the error accumulating feature in favour of a fail-fast approach using scalaz's \/ datatype.
Also added the ability to provide a java.util.concurrent.Executor to the client, defaulting to a CachedThreadPool.
Now uses the latest releases from scalaz, dispatch and scalalogging.



### Motivation

Existing xml-rpc libraries from the java world (such as [apache's offering](http://ws.apache.org/xmlrpc/))
end up tossing around arrays of Objects, which in the typesafe world of scala, is
not ideal and quite messy to use.

Therefore we decided to implement our own version of xml-rpc, casting to type as
early as possible and making use of scala 2.10's Futures as well as the scalaz library.

## Use case

```scala

import scala.concurrent.ExecutionContext.Implicits.global

val config = new XmlRpcConfig("http", "localhost", 8888, "/pathToHit")

val client = new XmlRpcClient()

val result = client.request(config, "someMethod", "someParameter")

result.fold(
    error  => println(s"Got back a fault: $error"),
    result => println(s"Got back a result: $result")
    )
)

```

## Details

We use [Dispatch](https://github.com/dispatch/reboot) to handle the actual http calls, but this
could easily be extended to support other transport libraries. Dispatch was chosen due to the
relatively small number of dependencies (and the fact that it works great!)

Internally we model all the xmlrpc data types, and parameters provided to XmlRpcClient.request
must be one of these types, so package.scala contains some convenient implicit conversions.

Depending on the data you are sending, you might want to provide your own
conversions or wrap the datatypes appropriately.

## License

All code is released under the [GNU Affero General Public License Version 3](http://www.gnu.org/licenses/agpl-3.0.html)

## Contribute

Please report any bugs/issues or submit a pull request.
