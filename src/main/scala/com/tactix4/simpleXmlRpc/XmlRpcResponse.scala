package com.tactix4.simpleXmlRpc

import util.XmlRpcUtils
import scala.xml.{Node, Elem}
import org.apache.commons.codec.binary.Base64

/**
 * @author max@tactix4.com
 *         5/21/13
 */

//TODO: make strings per the spec i.e. not full unicode
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
    val query = element \ "params" \ "param" \ "_"
    //each param should have a value
    if((element \\ "param").length != query.length)throw new XmlRpcXmlParseException("Could not parse all params")
    query.map((node: Node) => getParam(node)).toList
  }

  /**
   * translate an xml node into a scala type
   * @param node the node to convert
   * @return a case class based on XmlRpcDataType
   * @throws XmlRpcXmlParseException if the node doesn't match one the expected types
   */
  private def getParam(node: Node) : XmlRpcDataType =
  try{
    scala.xml.Utility.trim(node) match {
      case <value><string>{contents}</string></value>     => {
        if(!contents.descendant.isEmpty) throw new XmlRpcXmlParseException("string element has subnodes....")
        XmlRpcString(unescape(contents.text))
      }
      case <value><int>{contents}</int></value>           => {
        if(!contents.descendant.isEmpty) throw new XmlRpcXmlParseException("int element has subnodes....")
        XmlRpcInt(contents.text.toInt)
      }
      case <value><i4>{contents}</i4></value>             => {
        if(!contents.descendant.isEmpty) throw new XmlRpcXmlParseException("int element has subnodes....")
        XmlRpcInt(contents.text.toInt)
      }
      case <value><double>{contents}</double></value>     => {
        if(!contents.descendant.isEmpty) throw new XmlRpcXmlParseException("double element has subnodes....")
        XmlRpcDouble(contents.text.toDouble)
      }
      case <value><base64>{contents}</base64></value>     => {
        if(!contents.descendant.isEmpty) throw new XmlRpcXmlParseException("base64 element has subnodes....")
        XmlRpcBase64(contents.text.getBytes)
      }
      case <value><date>{contents}</date></value>        => {
        if(!contents.descendant.isEmpty) throw new XmlRpcXmlParseException("date element has subnodes....")
        XmlRpcDateTime(XmlRpcUtils.getDateFromISO8601String(contents.text))
      }
      case <value><boolean>{contents}</boolean></value>    => {
        if(!contents.descendant.isEmpty) throw new XmlRpcXmlParseException("boolean element has subnodes....")
        XmlRpcBoolean(if(contents.text.equals("0")) false else true)
      }
      case <value><array><data>{contents @_*}</data></array></value>   => {
        val nodes = (node \ "array" \ "data" \ "value" )
        val childs = (node \ "array" \ "data")
        if(childs.length > 1 && nodes.length > 1) throw new XmlRpcXmlParseException("Could not parse array correctly: " + node)
        createXmlRpcArray(nodes.toList.reverse)
      }
      case <value><struct>{contents @_*}</struct></value> => {
        val names = (node \ "struct" \ "member" \ "name").map(_.text)
        val values = (node \  "struct" \ "member" \ "value" )
        if(names.length != values.length) throw new XmlRpcXmlParseException("Could not parse struct correctly: " + node)
        createXmlRpcStruct((names zip values).toList.reverse)
      }
        //if no type is specified assume a string
      case <value>{contents}</value>                              => {
        if(!contents.descendant.isEmpty) throw new XmlRpcXmlParseException("string element has subnodes....")
        println("assuming a string type: " + node)
        XmlRpcString(unescape(contents.text))
      }

      case x => throw new XmlRpcXmlParseException("Parse failed.  Unexpected Node: " + x)

    }
  }
  catch {
    case e: NumberFormatException => throw new XmlRpcXmlParseException("could not parse xml content",e)
  }

  def createXmlRpcArray(nodes : List[Node]) : XmlRpcArray = {
    def loop(nodes:List[Node], acc:XmlRpcArray) : XmlRpcArray = nodes match {
      case Nil => acc
      case x::xs => loop(xs, acc.add(getParam(x)))
     }
    loop(nodes,XmlRpcArray(Nil))
  }

  def createXmlRpcStruct(nodes : List[(String,Node)]) : XmlRpcStruct =  {
    def loop(nodes: List[(String,Node)], acc:XmlRpcStruct) : XmlRpcStruct = nodes match {
      case Nil => acc
      case (x1,x2)::xs => loop(xs, acc.add(x1,getParam(x2)))
    }
    loop(nodes,XmlRpcStruct(Nil))

  }

  def toNode : Node =  <methodResponse><params>{params.map((d: XmlRpcDataType) => <param>{d.toXml}</param>)}</params></methodResponse>

  override def toString : String = {
    "<methodResponse><params>" +
    params.map((d: XmlRpcDataType) => "<param>" + d.toXml + "</param>").mkString +
    "</params></methodResponse>"


  }
}