package com.tactix4

/**
 * @author max@tactix4.com
 *         6/1/13
 */
package object simpleXmlRpc {
  type FaultCodeType = Either[XmlRpcString,XmlRpcInt]
  type FaultStringType = XmlRpcString
}
