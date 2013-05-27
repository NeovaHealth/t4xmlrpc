package com.tactix4.simpleXmlRpc.util

import java.util.{TimeZone, Date}
import java.text.{ParseException, SimpleDateFormat}
import com.tactix4.simpleXmlRpc.XmlRpcDateParseException

/**
 * @author max@tactix4.com
 *         5/22/13
 */
object XmlRpcUtils {

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
   * @throws XmlRpcDateParseException if string cannot be parsed
   */
  def getDateFromISO8601String(value: String): Date = {
    val date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    date.setTimeZone(TimeZone.getTimeZone("UTC"))
    date.setLenient(false)
    try {
      date.parse(value)
    }
    catch {
      case e: ParseException => throw new XmlRpcDateParseException("ParseException thrown trying to parse date value: " + value, e)
    }
  }

}
