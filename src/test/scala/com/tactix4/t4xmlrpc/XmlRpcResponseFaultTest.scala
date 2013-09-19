package com.tactix4.t4xmlrpc

import org.scalatest.FunSuite

import scala.xml._
import org.scalatest.prop._
import com.google.xmldiff.{Comparison, NoDiff}
import com.tactix4.t4xmlrpc.util.XMLRPCResponseGenerator

/**
 * Tests the libraries ability to represent faults and to throw exceptions for invalid faults
 *
 * @author max@tactix4.com
 *         5/21/13
 */
class XmlRpcResponseFaultTest extends FunSuite with PropertyChecks{

  implicit override val generatorDrivenConfig = PropertyCheckConfig(minSuccessful = 1000)

  val comp = new Comparison


  /**
   * Generate some valid faults - feed them into [[com.tactix4.t4xmlrpc.XmlRpcResponseFault]]
   * then compare the internal output to the original xml
   * Uses [[com.google.xmldiff.Comparison]] to do sensible XML comparison
   */
  test("Parse Valid Faults Correctly") {
    forAll(XMLRPCResponseGenerator.randomValidFaultGen){
      (node: Elem) =>
        val output = XmlRpcResponseFault(node).toElem
        assert(comp(node, output) === NoDiff)
    }
  }

  /**
   * Generate some invalid faults - expect exceptions thrown when we try to
   * feed them into [[com.tactix4.t4xmlrpc.XmlRpcResponseFault]]
   */
  test("Throw exceptions on Invalid Faults") {
    forAll(XMLRPCResponseGenerator.randomInValidFaultGen){
      (node: Node) =>
        intercept[Exception]{
          XmlRpcResponseFault(node)
        }

    }
  }
}


