
# Tactix4 XML-RPC

This library provides a simple and easy to use xml-rpc implementation in scala.

### Motivation

Existing xml-rcp libraries from the java world (such as [apache's offering](http://ws.apache.org/xmlrpc/))
end up tossing around arrays of Objects, which in the typesafe world of scala, is
not ideal and quite messy to use.

Therefore we decided to implement our own version of xml-rpc, casting to type as
early as possible and making use of scala 2.10's Futures.

## Use case

```
val config:XmlRpcConfig = XmlRpcConfig("http", "localhost", 8888, "/pathToHit")

val result:Future[XmlRpcResponse] = XmlRpcClient.request(config, "someMethod", "someParameter")

result.onComplete( _ match {
   case Success(r) => r match {
      case s: XmlRpcResponseNormal => println("Got a normal response: " + s)
      case s: XmlRpcResponseFault  => println("Got a fault response: " + s)
   }
   case Failure(e) => println("Something went wrong: " + e.getMessage())
   })
}

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
