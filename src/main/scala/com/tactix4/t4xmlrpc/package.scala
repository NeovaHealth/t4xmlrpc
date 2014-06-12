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
import java.text.SimpleDateFormat
import scalaz.syntax.std.either._
import scala.util.control.Exception._
import scalaz.\/

/**
 * @author max@tactix4.com
 *         6/1/13
 */
package object t4xmlrpc{

  val date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
  date.setTimeZone(TimeZone.getTimeZone("UTC"))
  date.setLenient(true)


  type XmlRpcResponse = XmlRpcResponseFault \/ XmlRpcResponseNormal

  final case class XmlRpcResponseFault(faultCode: ResultType[FaultCode], faultString: ResultType[String]) {
    override def toString: String =
      s"${faultCode.fold(_.toString,_.toString)} ${faultString.fold(_.toString, _.toString)}"
  }

  final case class XmlRpcResponseNormal(params: ErrorMessage \/ List[XmlRpcDataType]) {
    override def toString: String = params.fold(_.toString,_.toString())
  }

  type ErrorMessage = String
  type FaultCode = XmlRpcDataType
  type ResultType[+A] = ErrorMessage \/ A

  /** format date in ISO 8601 format taking into account timezone
    * http://stackoverflow.com/questions/3914404/how-to-get-current-moment-in-iso-8601-format
    *
    * @param value date object to be formatted
    * @return string representation of the date
    */
  def getDateAsISO8601String(value: Date): String = {
    val t = date.format(value)
    date.format(date.parse(t))
  }



  /**
   * parse string into an ISO 8601 date object
   *
   * @param value string representation to be parsed
   * @return date object representing the date
   * @throws XmlRpcParseException if string cannot be parsed
   */
  def getDateFromISO8601String(value: String): ErrorMessage \/ Date = {
      (allCatch either date.parse(value)).disjunction.bimap(_.getMessage, d => d)
  }

}
