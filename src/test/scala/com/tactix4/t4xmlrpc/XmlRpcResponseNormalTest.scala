package com.tactix4.t4xmlrpc

import scala.xml._
import org.scalatest.prop._
import org.scalatest.FunSuite
import com.google.xmldiff.{Comparison, NoDiff}
import com.tactix4.t4xmlrpc.util.XMLRPCResponseGenerator

/**
 * Tests the resilience of the library to represent responses as well as to throw exceptions on invalid input
 *
 * @author max@tactix4.com
 *         5/21/13
 */
class XmlRpcResponseNormalTest extends FunSuite with PropertyChecks{

  implicit override val generatorDrivenConfig = PropertyCheckConfig(minSuccessful = 1000)

  val comp = new Comparison

  /**
   * Generate some valid responses - feed them into [[com.tactix4.t4xmlrpc.XmlRpcResponseNormal]]
   * then compare the internal output to the original xml
   * Uses [[com.google.xmldiff.Comparison]] to do sensible XML comparison
   */
  test("internal representation should match input") {
    forAll(XMLRPCResponseGenerator.randomValidResponseGen) {
      (node: Elem) => {
        val output = XmlRpcResponseNormal(node).toElem
        assert(comp(node, output) === NoDiff)
      }
    }
  }

  /**
   * Generate some invalid responses - expect exceptions thrown when we try to
   * feed them into [[com.tactix4.t4xmlrpc.XmlRpcResponseNormal]]
   */
  test("should throw exceptions on invalid input") {
    forAll(XMLRPCResponseGenerator.randomInValidResponseGen){
      (node: Node) =>
        if(node.descendant.length >= 2) { // dont test the empty messages - we'll accept them
          intercept[java.lang.Exception]{
            XmlRpcResponseNormal(node)
          }
        }
    }
  }
}
