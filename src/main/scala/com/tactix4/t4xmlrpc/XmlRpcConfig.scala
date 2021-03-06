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

/**
 * Class to hold config information for the current XML-RPC target host
 *
 * @param protocol the protocol to use
 * @param host the host to connect to
 * @param port the port to connect to
 * @param path the path to connect to
 * @param headers a [[scala.collection.immutable.Map]] containing headers to be sent to host
 * @author max@tactix4.com
 * 5/21/13
 */
case class XmlRpcConfig (protocol :String, host: String, port: Int, path: String, headers: Map[String, String] = Map()){
  override val toString:String = protocol + "://" + host + ":" + port + path

}

