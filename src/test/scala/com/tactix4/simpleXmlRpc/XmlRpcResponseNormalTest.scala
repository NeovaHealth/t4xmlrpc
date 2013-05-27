package com.tactix4.simpleXmlRpc

import scala.xml.Node
import org.scalatest.FunSuite
import org.scalatest.prop._
import org.scalacheck.Prop._
import org.scalatest.matchers.ShouldMatchers._
import com.tactix4.simpleXmlRpc.util.XMLRPCResponseGenerator

/**
 * @author max@tactix4.com
 *         5/21/13
 */
class XmlRpcResponseNormalTest extends FunSuite with PropertyChecks{

  test("internal representation should match input") {
    forAll(XMLRPCResponseGenerator.randomValidRequestGen) {
      (node: Node) => {
        val input = node
        val output = XmlRpcResponseNormal(node).toNode
        input should equal(output)
      }
    }
  }

  test("should throw exceptions on invalid input") {
    forAll(XMLRPCResponseGenerator.randomInValidRequestGen){
      (node: Node) =>
        if(node.descendant.length < 2) true // dont test the empty messages - we'll accept them
        else {
          println(node)
          val e = intercept[java.lang.Exception]{
            XmlRpcResponseNormal(node)
          }
          println(e.getMessage)
        }
    }
  }
}
