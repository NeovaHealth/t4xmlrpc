package com.tactix4.simpleXmlRpc

import java.util.Date
import scala.language.implicitConversions
/**
 * @author max@tactix4.com
 *         5/23/13
 */
object XmlRpcPreamble {

  implicit def IntToXmlRpcInt(x: Int) = XmlRpcInt(x)
  implicit def StringToXmlRpcString(x: String) = XmlRpcString(x)
  implicit def BooleanToXmlRpcBoolean(x:Boolean) = XmlRpcBoolean(x)
  implicit def DoubleToXmlRpcDouble(x:Double) = XmlRpcDouble(x)
  implicit def Base64ToXmlRpcBase64(x:Array[Byte]) = XmlRpcBase64(x)
  implicit def DateToXmlRpcDate(x:Date) = XmlRpcDateTime(x)
  implicit def ListToXmlRpcArray(x:List[XmlRpcDataType]) = XmlRpcArray(x)
  implicit def TupleListToXmlRpcStruct(x:List[(String, XmlRpcDataType)]) = XmlRpcStruct(x)
}
