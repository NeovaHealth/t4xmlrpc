package com.tactix4.simpleXmlRpc

import dispatch._
import Defaults._
import scala.concurrent.Promise
import scala.xml.{Source, XML, Unparsed, Elem}
import scala.util.{Try, Success, Failure}
import scala.xml.parsing.ConstructingParser
import scalaz.NonEmptyList
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.http._
import spray.can.Http
import HttpMethods._
import spray.http.HttpHeaders.RawHeader
import org.xml.sax.SAXException
import java.io.IOException
import MediaTypes._


/**
 * Created by max@tactix4.com
 * 5/21/13
 */
object XmlRpcClient {
  implicit val system = ActorSystem("xmlrpc-client")


  def request(config: XmlRpcConfig, methodName: String, params: XmlRpcDataType*): Future[XmlRpcResponse] = request(config, methodName, params.toList)

  def request(config: XmlRpcConfig, methodName: String, params: List[XmlRpcDataType]): Future[XmlRpcResponse] = {

    implicit val timeout: Timeout = 5.seconds

    val request   = new XmlRpcRequest(methodName, params)
    println(request)
//    val headers   = config.headers.map(h => RawHeader(h._1,h._2)).toList
    val headers = Nil
    val body      = HttpEntity(`text/xml`, request.toString)
    val response  = (IO(Http) ? HttpRequest(POST, Uri(config.getUrl),headers,body)).mapTo[HttpResponse]
    val result    = Promise[XmlRpcResponse]()

    response.onComplete{

      x => x match{
        case Failure(e) => result.failure(new XmlRpcClientException("Error connecting to host.\n" + e.getMessage, e))
        case Success(r) => {
          try{
            val xml = scala.xml.XML.loadString(r.message.entity.asString)
            if (!(xml \ "methodResponse" \ "fault").isEmpty) {
              result.complete(Try(XmlRpcResponseFault(xml)))
            } else {
              result.complete(Try(XmlRpcResponseNormal(xml)))
            }
          } catch {
            case e:Throwable => throw new XmlRpcXmlParseException(e.getMessage,e)
          }
        }
      }
    }
    result.future
  }
}
