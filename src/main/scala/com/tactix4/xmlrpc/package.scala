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

package com.tactix4

import java.util.{TimeZone, Date}
import scala.language.implicitConversions
import collection._
import com.tactix4.xmlrpc.Exceptions.XmlRpcParseException
import java.text.{ParseException, SimpleDateFormat}
/**
 * @author max@tactix4.com
 *         6/1/13
 */
package object xmlrpc {
  type FaultCodeType = Either[XmlRpcString,XmlRpcInt]
  type FaultStringType = XmlRpcString
  type XmlRpcArray = XmlRpcArrayType[XmlRpcData]
  type XmlRpcStruct = XmlRpcStructType[XmlRpcData]



  /** format date in ISO 8601 format taking into account timezone
    * http://stackoverflow.com/questions/3914404/how-to-get-current-moment-in-iso-8601-format
    *
    * @param value date object to be formatted
    * @return string representation of the date
    */
  def getDateAsISO8601String(value: Date): String = {
    val df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    df.setTimeZone(TimeZone.getTimeZone("UTC"))
    df.setLenient(true)
    val t = df.format(value)
    df.format(df.parse(t))

  }

  /**
   * parse string into an ISO 8601 date object
   *
   * @param value string representation to be parsed
   * @return date object representing the date
   * @throws XmlRpcParseException if string cannot be parsed
   */
  def getDateFromISO8601String(value: String): Date = {
    val date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    date.setTimeZone(TimeZone.getTimeZone("UTC"))
    date.setLenient(false)
    try {
      date.parse(value)
    }
    catch {
      case e: ParseException => throw new XmlRpcParseException("ParseException thrown trying to parse date value: " + value, e)
    }
  }

  /**
   * unescape a string - this is needed because scala.xml seems to escape all xml text
   *
   * @param s the string to be unescaped
   * @return the unescaped string
   */
  def unescape(s:String) : String = {
     s.replaceAll("&quot;", "\"")
  }

  class MultipleMap[A,B] extends mutable.HashMap[A, mutable.Set[B]] with mutable.MultiMap[A,B]

  implicit def IntToXmlRpcInt(x: Int) = XmlRpcInt(x)
  implicit def StringToXmlRpcString(x: String) = XmlRpcString(x)
  implicit def BooleanToXmlRpcBoolean(x:Boolean) = XmlRpcBoolean(x)
  implicit def DoubleToXmlRpcDouble(x:Double) = XmlRpcDouble(x)
  implicit def Base64ToXmlRpcBase64(x:Array[Byte]) = XmlRpcBase64(x)
  implicit def DateToXmlRpcDate(x:Date) = XmlRpcDateTime(x)
  implicit def ListToXmlRpcArray(x:List[XmlRpcData]) = XmlRpcArrayType(x)
  implicit def MultiMapToXmlRpcStruct(x:collection.mutable.MultiMap[String, XmlRpcData]) = XmlRpcStructType(x)
  implicit def MapToXmlRpcStruct(x:Map[String, XmlRpcData]) = {
    XmlRpcStructType(x.foldLeft(new mutable.HashMap[String, mutable.Set[XmlRpcData]] with mutable.MultiMap[String, XmlRpcData]){
      (acc, pair) => acc.addBinding(pair._1, pair._2)
    })
  }
}
