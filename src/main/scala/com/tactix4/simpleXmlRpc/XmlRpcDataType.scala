package com.tactix4.simpleXmlRpc

import _root_.util.XmlRpcUtils
import java.text.SimpleDateFormat
import java.util.{TimeZone, Date}
import scala.xml.{Elem, NodeSeq}

import scala.xml.NodeSeq

/**
 * Superclass of all XML-RPC types
 */
trait XmlRpcDataType {
  type T
  val value : T
  def toXml : NodeSeq
}

case class XmlRpcInt(value: Int) extends XmlRpcDataType{
  type T = Int
  def toXml: NodeSeq = {<value><int>{value}</int></value>}
}

case class XmlRpcBoolean(value: Boolean) extends XmlRpcDataType{
  type T = Boolean
  def toXml : NodeSeq  = {<value><boolean>{if(value) 1 else 0}</boolean></value>}
}

case class XmlRpcString(value: String) extends XmlRpcDataType{
  type T = String
  def toXml : NodeSeq =  {<value><string>{value}</string></value>}
}

case class XmlRpcDouble(value: Double) extends  XmlRpcDataType{
  type T = Double
  def toXml : NodeSeq = {<value><double>{value}</double></value>}
}

case class XmlRpcDateTime(value: Date) extends  XmlRpcDataType{
  type T = Date
  def toXml : NodeSeq = {<value><date>{XmlRpcUtils.getDateAsISO8601String(value)}</date></value>}
}

case class XmlRpcBase64(value: String) extends XmlRpcDataType{
  type T = String
  def toXml : NodeSeq = {<value><base64>{value}</base64></value>}
}

case class XmlRpcStruct(value: List[(String, XmlRpcDataType)]) extends XmlRpcDataType{
  type T = List[(String, XmlRpcDataType)]
  private def outputStruct(k: String, v: XmlRpcDataType) : Elem ={
    <value><struct><member><name>{k}</name>{v.toXml}</member></struct></value>
  }
  def toXml : NodeSeq = {
    value.map((elem: (String, XmlRpcDataType)) => outputStruct(elem._1, elem._2)).reduce((a: NodeSeq, b: NodeSeq) => a ++ b)
  }
}

case class XmlRpcArray(value: List[XmlRpcDataType]) extends XmlRpcDataType{
  type T = List[XmlRpcDataType]

  def this(values: XmlRpcDataType*) = this(values.toList)

  def toXml : NodeSeq = <value><array><data>{value.map((v: XmlRpcDataType) => v.toXml).reduce((a: NodeSeq, b: NodeSeq) => a ++ b)  }</data></array></value>
}