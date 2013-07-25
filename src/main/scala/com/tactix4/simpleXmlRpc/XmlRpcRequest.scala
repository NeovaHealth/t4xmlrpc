package com.tactix4.simpleXmlRpc

import scala.xml.Unparsed

/**
 * @author max@tactix4.com
 *         5/22/13
 */
class XmlRpcRequest(methodName: String, params: List[XmlRpcDataType]) {


  /**
   * @param ps the list of paramters
   * @return a string representation of the parameters wrapped in an Unparsed object to avoid escaping
   */
  def outputParams(ps: List[XmlRpcDataType]): Unparsed = Unparsed(ps.map(
    (p: XmlRpcDataType) => <param>{p.toXml}</param>).mkString)

  override def toString: String =
    "<?xml version='1.0'?>" +
      scala.xml.Utility.trim(<methodCall>
        <methodName>
          {methodName}
        </methodName> <params>
          {outputParams(params)}
        </params>
      </methodCall>).toString()
}
