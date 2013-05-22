package com.tactix4.simpleXmlRpc

import _root_.util.XmlRpcUtils
import scala.xml.{Node, Elem}

/**
 * @author max@tactix4.com
 *         5/21/13
 */
trait XmlRpcResponse{
  def unescape(s:String) : String = {
    s.replaceAll("&quot;", "\"")
  }
}

/**
 * Represents a fault response from the server
 * it contains the error code and error string returned by the server
 * the error code should be an Int as per http://xmlrpc.scripting.com/spec
 * however pythons xmlrpc library decided to use a string instead :/
 * http://docs.python.org/2/library/xmlrpclib.html#fault-objects
 * for this reason it's encoded as a string
 *
 * @param element the raw xml Elem from the server
 */

//TODO: check results of non parsing xml queries
case class XmlRpcResponseFault(element: Node) extends XmlRpcResponse {

    private val faultValues = element \\ "member" \\ "value"
    val faultCode : String = unescape(faultValues(0).text)
    val faultString : String = unescape(faultValues(1).text)

    override def toString : String  = "Fault Code: " + faultCode + "\nFault String: " + faultString

}

/**
 * Represents a normal (i.e. non-fault) response from the xmlrpc server
 * @param element the raw xml Elem from the server
 */
case class XmlRpcResponseNormal(element: Node) extends XmlRpcResponse{

  /**
   * a list of XmlRpcDataType object representing the response's arguments
   */
  //TODO: check results of non parsing xml queries
  val params: List[XmlRpcDataType] = {
    val query = element \ "params" \ "param" \ "value" \ "_"
    query.map((node: Node) => getParam(node)).toList
  }

  /**
   * translate an xml node into a scala type
   * @param node the node to convert
   * @return a case class based on XmlRpcDataType
   * @throws XmlRpcXmlParseException if the node doesn't match one the expected types
   */
  //TODO: make tail recursive
  private def getParam(node: Node) : XmlRpcDataType =
  try{
    scala.xml.Utility.trim(node) match {
      case <string>{contents}</string>     => XmlRpcString(unescape(contents.text))
      case <int>{contents}</int>           => XmlRpcInt(contents.text.toInt)
      case <i4>{contents}</i4>             => XmlRpcInt(contents.text.toInt)
      case <double>{contents}</double>     => XmlRpcDouble(contents.text.toDouble)
      case <base64>{contents}</base64>     => XmlRpcBase64(unescape(contents.text))
      case <date>{contents}</date>         => XmlRpcDateTime(XmlRpcUtils.getDateFromISO8601String(contents.text))
      case <boolean>{contents}</boolean>   => XmlRpcBoolean(if(contents.text.equals("0")) false else true)
      case <array>{contents @_*}</array>   => {
        val nodes = (node \ "data" \ "value" \ "_").toList
        XmlRpcArray(nodes.map((x: Node) => getParam(x)))
      }
      case <struct>{contents @_*}</struct> => {
        val nodes = (node \\ "member" \ "name") zip (node \\ "member" \ "value" \ "_")
        XmlRpcStruct(nodes.map((x: (Node, Node)) => (x._1.text, getParam(x._2))).toList)
      }
      case unknown                         => throw new XmlRpcXmlParseException("could not parse item: " + unknown)
    }
  }
  catch {
    case e: NumberFormatException => throw new XmlRpcXmlParseException("could not parse xml content",e)
  }

  override def toString : String = {
    "<methodResponse><params>" +
    params.map((d: XmlRpcDataType) => "<param>" + d.toXml + "</param>").mkString +
    "</params></methodResponse>"


  }
}