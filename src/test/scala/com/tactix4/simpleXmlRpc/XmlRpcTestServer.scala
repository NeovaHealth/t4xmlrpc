package com.tactix4.simpleXmlRpc
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import akka.actor._
import spray.can.Http
import spray.can.server.Stats
import spray.util._
import spray.http._
import HttpMethods._
import MediaTypes._
import akka.io

/**
 * Created with IntelliJ IDEA.
 * User: max
 * Date: 05/07/2013
 * Time: 11:57
 * To change this template use File | Settings | File Templates.
 */
class XmlRpcTestServer extends Actor with SprayActorLogging {

  implicit val timeout: Timeout = 1.second // for the actor 'asks'
  import context.dispatcher // ExecutionContext for the futures and scheduler

  def receive = {
    // when a new connection comes in we register ourselves as the connection handler
    case _: Http.Connected => sender ! Http.Register(self)
    case HttpRequest(POST,Uri.Path("/xmlrpc/common"),_, e, _) => {
      if(e.asString.contains("search")) sender ! searchPartners
      else if(e.asString.contains("read")) sender ! readPartners
      else if(e.asString.contains("login")) sender ! login
    }
  }

  lazy val login = HttpResponse(
    entity = HttpEntity(`text/xml`,
      <methodResponse><params><param><value><int>1</int></value></param></params></methodResponse>.toString()
    )
  )

  lazy val searchPartners = HttpResponse(
    entity = HttpEntity(`text/xml`,
      <?xml version='1.0'?>
        <methodResponse>
          <params>
            <param><value><array><data>
              <value><int>1</int></value>
              <value><int>2</int></value>
              <value><int>3</int></value>
            </data></array></value></param>
          </params>
        </methodResponse>.toString()
    )
  )

  lazy val readPartners = HttpResponse(
    entity = HttpEntity(`text/xml`,
      <methodResponse>
        <params>
          <param><value><array><data>
            <value><struct>
              <member>
                <name>id</name>
                <value><int>1</int></value>
              </member>
              <member>
                <name>name</name>
                <value><string>Duck, Donald J</string></value>
               </member>
              <member>
                <name>id</name>
                <value><int>2</int></value>
              </member>
              <member>
                <name>name</name>
                <value><string>Duck, James T</string></value>
              </member>
              <member>
                <name>id</name>
                <value><int>3</int></value>
              </member>
              <member>
                <name>name</name>
                <value><string>Duck, Daniel P</string></value>
              </member>
            </struct></value>
          </data></array></value></param>
        </params>
      </methodResponse>.toString()
    )

  )


}
