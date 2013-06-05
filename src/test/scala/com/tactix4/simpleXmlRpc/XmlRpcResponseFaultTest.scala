package com.tactix4.simpleXmlRpc

import org.scalatest.FunSuite
import org.scalatest.prop.PropertyChecks
import org.scalatest.matchers.ShouldMatchers._
import com.tactix4.simpleXmlRpc.util.XMLRPCResponseGenerator
import scala.xml.Node

/**
 * @author max@tactix4.com
 *         5/21/13
 */
class XmlRpcResponseFaultTest extends FunSuite with PropertyChecks{


 // implicit override val generatorDrivenConfig = PropertyCheckConfig(minSuccessful = 1000)

  test("Parse Valid Faults Correctly") {
    forAll(XMLRPCResponseGenerator.randomValidFaultGen){
      (node: Node) =>
      val input = scala.xml.Utility.trim(node)
        val output = XmlRpcResponseFault(node).toNode
        input should equal(output)
    }
  }

  test("Throw exceptions on Invalid Faults") {
    forAll(XMLRPCResponseGenerator.randomInValidFaultGen){
      (node: Node) =>
        intercept[Exception]{
          XmlRpcResponseFault(node)
        }

    }
  }
}


