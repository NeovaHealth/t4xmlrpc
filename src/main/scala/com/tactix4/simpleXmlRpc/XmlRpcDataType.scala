package com.tactix4.simpleXmlRpc

import util.XmlRpcUtils
import java.util.Date
import scala.xml.NodeSeq

/**
 * Superclass of all XML-RPC types
 */
sealed trait XmlRpcDataType {
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

case class XmlRpcString(value: String, tag : Boolean = true) extends XmlRpcDataType{
  type T = String
  def toXml : NodeSeq = if(tag) {<value><string>{value}</string></value>} else {<value>{value}</value>}
}


case class XmlRpcDouble(value: Double) extends  XmlRpcDataType{
  type T = Double
  def toXml : NodeSeq = {<value><double>{value}</double></value>}
}

case class XmlRpcDateTime(value: Date) extends  XmlRpcDataType{
  type T = Date
  def toXml : NodeSeq = {<value><date>{XmlRpcUtils.getDateAsISO8601String(value)}</date></value>}
}

case class XmlRpcBase64(value: Array[Byte]) extends XmlRpcDataType{
  type T = Array[Byte]
  def toXml : NodeSeq = {<value><base64>{new String(value)}</base64></value>}
}

case class XmlRpcStruct(value: List[(String, XmlRpcDataType)]) extends XmlRpcDataType{
  type T = List[(String, XmlRpcDataType)]

  private def outputStruct(t: ( String, XmlRpcDataType)) : NodeSeq={
    <member><name>{t._1}</name>{t._2.toXml}</member>
  }

  def add(name:String, data: XmlRpcDataType) = XmlRpcStruct((name,data)::value)

  def toXml = <value><struct>{value.map(outputStruct(_)).reduce( _ ++ _ )}</struct></value>

}



case class XmlRpcArray(value: List[XmlRpcDataType]) extends XmlRpcDataType{
  type T = List[XmlRpcDataType]
  def this(values: XmlRpcDataType*) = this(values.toList)
  def toXml = <value><array><data>{val x = value.map(_.toXml); if(!x.isEmpty) x.reduce(_ ++ _)  }</data></array></value>
}