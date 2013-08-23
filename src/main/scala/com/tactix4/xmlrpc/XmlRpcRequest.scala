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

package com.tactix4.xmlrpc

import scala.xml.Unparsed
import XmlRpcDataHelper._

/**
 * Represents a request sent to the XML-RPC server
 * @param methodName the name of the method to invoke
 * @param params the list of [[com.tactix4.xmlrpc.XmlRpcData]] parameters to pass along to the method
 * @author max@tactix4.com
 *         5/22/13
 */
class XmlRpcRequest(methodName: String, params: List[XmlRpcData]) {

  /**
   * @param ps the list of paramters
   * @return a string representation of the parameters wrapped in an Unparsed object to avoid escaping
   */
  def outputParams(ps: List[XmlRpcData]): Unparsed = Unparsed(ps.map(
    (p: XmlRpcData) => <param>{XmlWriter.toXml(p)}</param>).mkString)

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
