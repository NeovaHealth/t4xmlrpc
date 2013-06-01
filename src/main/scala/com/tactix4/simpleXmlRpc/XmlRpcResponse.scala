package com.tactix4.simpleXmlRpc

import util.XmlRpcUtils
import scala.xml.Node
import scalaz._
import Scalaz._
import scala.util.Try

/**
 * @author max@tactix4.com
 *         5/21/13
 */

//TODO: Do away with scala.xml - scales xml instead?
//TODO: make strings per the spec i.e. not full unicode

trait XmlRpcResponse{
  def unescape(s:String) : String = {
     s.replaceAll("&quot;", "\"")
  }
  def toNode : Node
}

/**
 * Represents a fault response from the server
 * it contains the error code and error string returned by the server
 * the error code should be an Int as per http://xmlrpc.scripting.com/spec
 * however pythons xmlrpc library decided to use a string instead :/
 * http://docs.python.org/2/library/xmlrpclib.html#fault-objects
 * for this reason we allow a string too, hence why faultCode has type Either[XmlRpcString.XmlRpcInt]
 *
 */

object XmlRpcResponseFault{

  def validFaultCode(node: Option[Node]): Validation[String, FaultCodeType]=  node.getOrElse("faultCode not found") match{
    case <value><int>{contents}</int></value> =>
      if (contents.descendant.isEmpty) {
        val parse = Try(Right(XmlRpcInt(contents.text.toInt)).success[String])
        parse.getOrElse(("Could not parse " + contents.text + " to Int").fail[FaultCodeType])
      }
      else ("unexpected child nodes in faultCode: " + contents).fail[FaultCodeType]

    case <value><i4>{contents}</i4></value> =>
      if (contents.descendant.isEmpty) {
        val parse = Try(Right(XmlRpcInt(contents.text.toInt)).success[String])
        parse.getOrElse(("Could not parse " + contents.text + " to Int").fail[FaultCodeType])
      }
      else ("unexpected child nodes in faultCode: " + contents).fail[FaultCodeType]

    case <value><string>{contents}</string></value> =>
      if(contents.descendant.isEmpty) {
        Left(XmlRpcString(contents.text)).success[String]
      }
      else ("unexpected child nodes in faultCode: " + contents).fail[FaultCodeType]

    case <value>{unnamedString}</value> =>
      if(unnamedString.descendant.isEmpty) Left(XmlRpcString(unnamedString.text)).success[String]
      else ("unexpected child nodes in faultCode: " + unnamedString).fail[FaultCodeType]

    case x  => ("could not parse faultCode value element: " + x).fail[FaultCodeType]
  }

  def validFaultString(node: Option[Node]): Validation[String,FaultStringType] = node.getOrElse("faultString not found") match {

    case <value><string>{contents}</string></value> =>
      if(contents.descendant.isEmpty) XmlRpcString(contents.text).success[String]
      else ("unexpected child nodes in faultString: " + contents).fail[FaultStringType]

    case <value>{unnamedString}</value> =>
      if(unnamedString.descendant.isEmpty) XmlRpcString(unnamedString.text,false).success[String]
      else ("unexpected child nodes in faultString: " + unnamedString).fail[FaultStringType]

    case x => ("could not parse faultString: " + x).fail[FaultStringType]
  }


  def validateFault(node: Node) = {
    val members =  scala.xml.Utility.trim(node) \ "fault" \ "value" \ "struct" \ "member"
    val faultCode = (members.filter { (_ \\ "_" exists (_.text == "faultCode")) } \ "value").headOption
    val faultString = (members.filter { (_ \\ "_" exists (_.text == "faultString")) } \ "value" ).headOption
    (validFaultCode(faultCode).liftFailNel |@| validFaultString(faultString).liftFailNel) {
      XmlRpcResponseFault(_,_)
    }
  }

  def apply(node: Node) : Validation[NonEmptyList[String], XmlRpcResponseFault] = validateFault(node)
}
case class XmlRpcResponseFault(faultCode: FaultCodeType, faultString: FaultStringType) extends XmlRpcResponse {


  override def toString : String  = "Fault Code: " + faultCode + "\nFault String: " + faultString

  def toNode: Node =
    scala.xml.Utility.trim(<methodResponse>
      <fault>
        <value>
          <struct>
            <member>
              <name>faultCode</name>
                {faultCode.fold(_.toXml, _.toXml)}
            </member>
            <member>
              <name>faultString</name>
              {faultString.toXml}
            </member>
          </struct>
        </value>
      </fault>
    </methodResponse>)
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
      case <value><array>{contents @_*}</array></value>   => {
        val nodes = (node \ "array" \ "data" \ "value" )
        val childs = (node \ "array" \ "data")
        if(childs.length > 1 && nodes.length > 1) throw new XmlRpcXmlParseException("Could not parse array correctly: " + node)
        if(childs.length == 0) throw new XmlRpcXmlParseException("Could not parse array correctly: " + node)
        createXmlRpcArray(nodes.toList.reverse)
      }
      case <value><struct>{contents @_*}</struct></value> => {
        val names = (contents \ "name")
        if(names.exists(_.descendant.length > 1)) throw new XmlRpcXmlParseException("Struct name member has sub elements: " + node)
        val namesText = names.map(_.text)
        val values = (contents \ "value" )

        if(namesText.length != values.length) throw new XmlRpcXmlParseException("Could not parse struct correctly: " + node)
        createXmlRpcStruct((namesText zip values).toList.reverse)
      }
        //if no type is specified assume a string
      case <value>{contents}</value>                              => {
        if(!contents.descendant.isEmpty) throw new XmlRpcXmlParseException("string element has subnodes....")
        XmlRpcString(unescape(contents.text), false)
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