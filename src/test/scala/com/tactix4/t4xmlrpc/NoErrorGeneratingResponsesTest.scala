package com.tactix4.t4xmlrpc

import org.scalatest.prop._
import org.scalatest.FunSuite
import com.tactix4.t4xmlrpc.util.XMLRPCResponseGenerator
import scalaz.xml.Content

/**
 * Tests the resilience of the library to represent responses as well as to throw exceptions on invalid input
 *
 * @author max@tactix4.com
 *         5/21/13
 */
class NoErrorGeneratingResponsesTest extends FunSuite with PropertyChecks with XmlRpcResponses{

  implicit override val generatorDrivenConfig = PropertyCheckConfig(minSuccessful = 1000)

  test("All valid xmlrpc responses should be parsed with no errors") {
    forAll(XMLRPCResponseGenerator.arbitraryValidXmlRpcResponse.arbitrary) {
      (node: List[Content]) => createXmlRpcResponse(node).fold(
        _ => fail("Normal Responses recognised as a fault") ,
      (normal: XmlRpcResponseNormal) => if(normal.errors.isDefined) fail("Errors were generated: " + normal.errors.mkString(" ")))
      }
    }

  test("All valid xmlrpc faults should be parsed with no errors") {
    forAll(XMLRPCResponseGenerator.arbitraryValidXmlRpcFault.arbitrary){
      (node: List[Content]) => createXmlRpcResponse(node).fold(
       (fault:XmlRpcResponseFault) => assert(fault.errors.isEmpty),
        _ => fail("Fault recognised as a Normal response")
      )
        }
    }
}
