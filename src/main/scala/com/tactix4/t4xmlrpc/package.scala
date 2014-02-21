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
import java.text.{ParseException, SimpleDateFormat}
import scalaz._
import Scalaz._
import com.typesafe.scalalogging.slf4j.Logging

/**
 * @author max@tactix4.com
 *         6/1/13
 */
package object t4xmlrpc extends Logging{


  type ErrorMessage = String
  type FaultCode = XmlRpcDataType

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
  def getDateFromISO8601String(value: String): Validation[ErrorMessage,Date] = {
    val date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    date.setTimeZone(TimeZone.getTimeZone("UTC"))
    date.setLenient(false)
    try {
      date.parse(value).success
    } catch {
      case e: ParseException => e.getMessage.fail
    }
  }

  implicit def IntToXmlRpcInt(x: Int) = XmlRpcInt(x)
  implicit def StringToXmlRpcString(x: String) = XmlRpcString(x)
  implicit def BooleanToXmlRpcBoolean(x:Boolean) = XmlRpcBoolean(x)
  implicit def DoubleToXmlRpcDouble(x:Double) = XmlRpcDouble(x)
  implicit def Base64ToXmlRpcBase64(x:Array[Byte]) = XmlRpcBase64(x)
  implicit def DateToXmlRpcDate(x:Date) = XmlRpcDate(x)
  implicit def ListToXmlRpcArray(x:List[XmlRpcDataType]) = XmlRpcArray(x)
  implicit def MapToXmlRpcStruct(x:immutable.Map[String, XmlRpcDataType]) = XmlRpcStruct(x)
}
