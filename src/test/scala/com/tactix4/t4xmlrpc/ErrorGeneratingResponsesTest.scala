package com.tactix4.t4xmlrpc

import org.scalatest.FunSuite

import scalaz.xml.Xml._
import org.scalatest.prop._
import com.tactix4.t4xmlrpc.util.XMLRPCResponseGenerator
import scalaz.xml.Content

/**
 * Tests the libraries ability to represent faults and to throw exceptions for invalid faults
 *
 * @author max@tactix4.com
 *         5/21/13
 */
class ErrorGeneratingResponsesTest extends FunSuite with PropertyChecks with XmlRpcResponses {

  implicit override val generatorDrivenConfig = PropertyCheckConfig(minSuccessful = 1000)

  test("All invalid xmlrpc responses should be parsed with errors") {
    forAll(XMLRPCResponseGenerator.arbitraryInValidXmlRpcResponse.arbitrary) {
      (node: List[Content]) => createXmlRpcResponse(node).fold(
        _ => fail("Normal Responses recognised as a fault"),
        (normal: XmlRpcResponseNormal) => if (normal.errors.isEmpty && !node.isEmpty) fail("Errors were not generated for input: " + (node.head sxprints pretty)))
    }
  }
  test("All invalid xmlrpc faults should be parsed with errors") {
    forAll(XMLRPCResponseGenerator.arbitraryInValidXmlRpcFault.arbitrary) {
      (node: List[Content]) => createXmlRpcResponse(node).fold(
        (fault: XmlRpcResponseFault) =>  if (fault.errors.isEmpty && !node.isEmpty) fail("Errors were not generated for input: " + (node.head sxprints pretty)),
        _ => fail("Fault recognised as a Normal response")
      )
    }
  }

}


