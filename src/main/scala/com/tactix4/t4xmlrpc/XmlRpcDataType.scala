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
import scalaz.Value
import java.nio.charset.Charset


/**
 * Hierarchy of all XML-RPC types
 */
sealed trait XmlRpcDataType{

  override def toString: String ={
    fold(
      b => b.toString,
      i => i.toString,
      d => d.toString,
      d => d.toString,
      b => new String(b,java.nio.charset.Charset.forName("UTF-8")),
      s => s.toString,
      a => a.toString(),
      s => s.toString()
    )
  }

  def fold[X](
    xmlRpcBoolean: Boolean => X,
    xmlRpcInt: Int => X,
    xmlRpcDouble: Double => X,
    xmlRpcDate:Date => X,
    xmlRpcBase64:Array[Byte] => X,
    xmlRpcString: String => X,
    xmlRpcArray: List[XmlRpcDataType] => X,
    xmlRpcStruct: Map[String,XmlRpcDataType] => X
  ): X =
    this match {
      case XmlRpcBoolean(b)   => xmlRpcBoolean(b)
      case XmlRpcInt(n)       => xmlRpcInt(n)
      case XmlRpcDouble(n)    => xmlRpcDouble(n)
      case XmlRpcDate(d)      => xmlRpcDate(d)
      case XmlRpcBase64(b)    => xmlRpcBase64(b)
      case XmlRpcString(s)    => xmlRpcString(s)
      case XmlRpcArray(a)     => xmlRpcArray(a)
      case XmlRpcStruct(o)    => xmlRpcStruct(o)
    }

  def bool : Option[Boolean] = this.fold((b: Boolean) => Some(b),_=>None,_=>None,_=>None,_=>None,_=>None,_=>None,_=>None)
  def int : Option[Int] = this.fold(_ => None,(i:Int)=> Some(i),_=>None,_=>None,_=>None,_=>None,_=>None,_=>None)
  def double : Option[Double] = this.fold(_ => None,_=> None,d=>Some(d),_=>None,_=>None,_=>None,_=>None, _=>None)
  def date : Option[Date] = this.fold(_=> None,_=> None,_=>None,d=>Some(d),_=>None,_=>None,_=>None, _=>None)
  def base64 : Option[Array[Byte]] = this.fold(_=> None,_=> None,_=>None,_=>None,d=>Some(d),_=>None,_=>None, _=>None)
  def string : Option[String] = this.fold(_ => None,_=> None,_=>None,_=>None,_=>None,s=>Some(s),_=>None,_=>None)
  def array : Option[List[XmlRpcDataType]] = this.fold(_ => None,_=> None,_=>None,_=>None,_=>None,_=> None,a => Some(a),_=>None)
  def struct : Option[Map[String,XmlRpcDataType]] = this.fold(_ => None,_=> None,_=>None,_=>None,_=>None,_=> None,_=>None,s=>Some(s))

  def isBool :Boolean   = this.fold(_ => true, _ => false,_ => false,_ => false,_ => false,_ => false,_ => false,_ => false)
  def isInt :Boolean    = this.fold(_ => false,_ => true, _ => false,_ => false,_ => false,_ => false,_ => false,_ => false)
  def isDouble :Boolean = this.fold(_ => false,_ => false,_ => true, _ => false,_ => false,_ => false,_ => false,_ => false)
  def isDate :Boolean   = this.fold(_ => false,_ => false,_ => false,_ => true, _ => false,_ => false,_ => false,_ => false)
  def isBase64 :Boolean = this.fold(_ => false,_ => false,_ => false,_ => false,_ => true, _ => false,_ => false,_ => false)
  def isString :Boolean = this.fold(_ => false,_ => false,_ => false,_ => false,_ => false,_ => true, _ => false,_ => false)
  def isArray :Boolean  = this.fold(_ => false,_ => false,_ => false,_ => false,_ => false,_ => false,_ => true, _ => false)
  def isStruct :Boolean = this.fold(_ => false,_ => false,_ => false,_ => false,_ => false,_ => false,_ => false,_ => true )

  def asBool[X](f: Boolean => X): Option[X] = bool.map(f)
  def asInt[X](f: Int => X) = int.map(f)
  def asDouble[X](f: Double => X) = double.map(f)
  def asDate[X](f: Date => X) = date.map(f)
  def asBase64[X](f: Array[Byte] => X) = base64.map(f)
  def asString[X](f: String => X) = string.map(f)
  def asArray[X](f: List[XmlRpcDataType] => X) = array.map(f)
  def asStruct[X](f: Map[String,XmlRpcDataType] => X) = struct.map(f)

  implicit case class XmlRpcInt(value: Int) extends XmlRpcDataType
  implicit case class XmlRpcBoolean(value: Boolean) extends XmlRpcDataType
  implicit case class XmlRpcString(value: String) extends XmlRpcDataType
  implicit case class XmlRpcDouble(value: Double) extends XmlRpcDataType
  implicit case class XmlRpcDate(value: Date) extends XmlRpcDataType
  implicit case class XmlRpcBase64(value: Array[Byte]) extends XmlRpcDataType
  implicit case class XmlRpcArray(value: List[XmlRpcDataType]) extends XmlRpcDataType
  implicit case class XmlRpcStruct(value: Map[String, XmlRpcDataType]) extends XmlRpcDataType

}

object XmlWriter extends XmlRpcDataType{

  def write(p:XmlRpcDataType) :String =  p match {
    case XmlRpcInt(v)         => s"<value><int>$v</int></value>"
    case XmlRpcDouble(d)      => s"<value><double>$d</double></value>"
    case XmlRpcString(s)      => s"<value><string>$s</string></value>"
    case XmlRpcBoolean(b)     => s"<value><boolean>${if(b) 1 else 0}</boolean></value>"
    case XmlRpcBase64(b)      => s"<value><base64>${new String(b)}</base64></value>"
    case XmlRpcDate(d)        => s"<value><date>${getDateAsISO8601String(d)}</date></value>"
    case XmlRpcArray(a)       => s"<value><array><data>${writeArray(a)}</data></array></value>"
    case XmlRpcStruct(s)      => s"<value><struct>${writeMap(s)}</struct></value>"
  }
  def writeArray(a:List[XmlRpcDataType]) : String = {
    def loop(a:List[XmlRpcDataType], acc:List[String]): String = a match {
      case Nil    => acc.mkString
      case x::xs  => loop(xs, write(x)::acc)
    }
    loop(a.reverse,Nil)
  }

  def writeMap(m:Map[String,XmlRpcDataType]) : String = {
    def tupleToMember(tuple: (String, XmlRpcDataType)) : String = {
      s"<member><name>${tuple._1}</name>${write(tuple._2)}</member>"
    }
    def loop(m:List[(String,XmlRpcDataType)], acc:List[String]) : String = m match {
      case Nil => acc.mkString
      case ((x,y)::xs) => loop(xs,tupleToMember((x,y))::acc)
    }
    loop(m.toList.reverse,Nil)
  }
}



