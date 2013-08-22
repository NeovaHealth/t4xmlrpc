/*
 * Copyright (C) 2013 Tactix4
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tactix4.simpleXmlRpc

import java.util.Date
import scala.xml.NodeSeq
import com.tactix4.simpleXmlRpc.XmlWriter.XmlRpcConverter
import scala.collection.mutable


/**
 * Hierarchy of all XML-RPC types
 */
sealed trait XmlRpcData
sealed abstract class XmlRpcDataType[+T] extends XmlRpcData
case class XmlRpcInt(value: Int) extends XmlRpcDataType[Int]
case class XmlRpcBoolean(value: Boolean) extends XmlRpcDataType[Boolean]
case class XmlRpcString(value: String, withTag: Boolean = true) extends XmlRpcDataType[String]
case class XmlRpcDouble(value: Double) extends XmlRpcDataType[Double]
case class XmlRpcDateTime(value: Date) extends XmlRpcDataType[Date]
case class XmlRpcBase64(value: Array[Byte]) extends XmlRpcDataType[Array[Byte]]
case class XmlRpcArrayType[A <: XmlRpcData](value: List[A]) extends XmlRpcDataType[List[A]]
case class XmlRpcStructType[A <: XmlRpcData](value: mutable.MultiMap[String, A]) extends XmlRpcDataType[mutable.MultiMap[String, A]]

/**
 * Object to aid printing out XML-RPC types as xml
 */
object XmlWriter {
  def toXml[T <: XmlRpcData](data : T)(implicit converter: XmlRpcConverter[T])= {
    converter.convertToXml(data)
  }

  trait XmlRpcConverter[T <: XmlRpcData] {
    def convertToXml(value:T) : NodeSeq
  }

}

/**
 * Contains the implicits to convert [[com.tactix4.simpleXmlRpc.XmlRpcDataType]] values into xml
 */
object XmlRpcDataHelper {

  implicit def xmlRpcDataConverter = new XmlRpcConverter[XmlRpcData] {
   def convertToXml(d: XmlRpcData) : NodeSeq =
     d match{
     case x: XmlRpcBoolean  => XmlWriter.toXml(x)(booleanConverter)
     case x: XmlRpcInt      => XmlWriter.toXml(x)(intConverter)
     case x: XmlRpcDouble   => XmlWriter.toXml(x)(doubleConverter)
     case x: XmlRpcString   => XmlWriter.toXml(x)(stringConverter)
     case x: XmlRpcDateTime => XmlWriter.toXml(x)(dateConverter)
     case x: XmlRpcBase64   => XmlWriter.toXml(x)(base64Converter)
     case x: XmlRpcStruct   => XmlWriter.toXml(x)(structConverter)
     case x: XmlRpcArray    => XmlWriter.toXml(x)(arrayConverter)
   }
  }
  implicit def intConverter = new XmlRpcConverter[XmlRpcInt]{
    def convertToXml(i:XmlRpcInt): NodeSeq = <value><int>{i.value}</int></value>
  }
  implicit def booleanConverter = new XmlRpcConverter[XmlRpcBoolean]{
    def convertToXml(b: XmlRpcBoolean) : NodeSeq  = {<value><boolean>{if(b.value) 1 else 0}</boolean></value>}
  }
  implicit def stringConverter = new XmlRpcConverter[XmlRpcString] {
    def convertToXml(s: XmlRpcString) : NodeSeq = if(s.withTag) {<value><string>{s.value}</string></value>} else {<value>{s.value}</value>}
  }
  implicit def doubleConverter = new XmlRpcConverter[XmlRpcDouble] {
    def convertToXml(d: XmlRpcDouble) : NodeSeq = <value><double>{d.value}</double></value>
  }
  implicit def dateConverter = new XmlRpcConverter[XmlRpcDateTime] {
    def convertToXml(d: XmlRpcDateTime) : NodeSeq = {<value><date>{getDateAsISO8601String(d.value)}</date></value>}
  }
  implicit def base64Converter = new XmlRpcConverter[XmlRpcBase64] {
    def convertToXml(b: XmlRpcBase64): NodeSeq = {<value><base64>{new String(b.value)}</base64></value>}
  }

  implicit def arrayConverter : XmlWriter.XmlRpcConverter[XmlRpcArray]  = new XmlRpcConverter[XmlRpcArray] {
    def convertToXml(s: XmlRpcArray) : NodeSeq = <value><array><data>{val x = s.value.map((dataType: XmlRpcData) => dataType match {
      case z: XmlRpcInt => XmlWriter.toXml(z)
      case z: XmlRpcDouble => XmlWriter.toXml(z)
      case z: XmlRpcString => XmlWriter.toXml(z)
      case z: XmlRpcDateTime => XmlWriter.toXml(z)
      case z: XmlRpcBase64 => XmlWriter.toXml(z)
      case z: XmlRpcBoolean => XmlWriter.toXml(z)
      case z: XmlRpcStruct => XmlWriter.toXml(z)(structConverter)
      case z: XmlRpcArray => XmlWriter.toXml(z)(arrayConverter)

    }); if(!x.isEmpty) x.reduce(_ ++ _)  }</data></array></value>
  }

  implicit def structConverter : XmlWriter.XmlRpcConverter[XmlRpcStruct] = new XmlRpcConverter[XmlRpcStruct] {
    private def outputStruct(t: ( String, mutable.Set[XmlRpcData])) : NodeSeq =
      t._2.map(
        d  => <member><name>{t._1}</name>{XmlWriter.toXml(d)}</member>: NodeSeq
      ).reduce(_ ++ _)

    def convertToXml(s: XmlRpcStruct): NodeSeq = <value><struct>{s.value.map(outputStruct)}</struct></value>
  }






}

