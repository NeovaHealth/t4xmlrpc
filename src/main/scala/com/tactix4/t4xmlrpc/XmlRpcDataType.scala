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

package com.tactix4.t4xmlrpc
import java.util.Date
import scala.xml.NodeSeq


/**
 * Hierarchy of all XML-RPC types
 */
sealed trait XmlRpcDataValue
case class XmlRpcInt(value: Int) extends XmlRpcDataValue
case class XmlRpcBoolean(value: Boolean) extends XmlRpcDataValue
case class XmlRpcString(value: String, withTag: Boolean = true) extends XmlRpcDataValue
case class XmlRpcDouble(value: Double) extends XmlRpcDataValue
case class XmlRpcDateTime(value: Date) extends XmlRpcDataValue
case class XmlRpcBase64(value: Array[Byte]) extends XmlRpcDataValue
case class XmlRpcArrayType[A <: XmlRpcDataValue](value: List[A]) extends XmlRpcDataValue
case class XmlRpcStructType[A <: XmlRpcDataValue](value: List[(String, A)]) extends XmlRpcDataValue

/**
 * Object to aid printing out XML-RPC types as xml
 */
object XmlWriter {
  def write(value:XmlRpcDataValue) : NodeSeq = value match{

    case XmlRpcInt(v)         => <value><int>{v}</int></value>
    case XmlRpcDouble(d)      => <value><double>{d}</double></value>
    case XmlRpcString(s,false)=> <value>{s.value}</value>
    case XmlRpcString(s,true) => <value><string>{s.value}</string></value>
    case XmlRpcBoolean(b)     => <value><boolean>{if(b) 1 else 0}</boolean></value>
    case XmlRpcBase64(b)      => <value><base64>{new String(b.value)}</base64></value>
    case XmlRpcDateTime(d)    => <value><date>{getDateAsISO8601String(d)}</date></value>
    case XmlRpcArrayType(a)   => <value><array><data>{a.map(XmlWriter.write)}</data></array></value>
    case XmlRpcStructType(s)  => <value><struct>{s.map(z => <member><name>{z._1}</name>{XmlWriter.write(z._2)}</member>)}</struct></value>
  }

}

