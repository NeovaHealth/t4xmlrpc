package com.tactix4.simpleXmlRpc

import org.scalatest.FunSuite
import org.scalatest.prop.PropertyChecks
import org.scalatest.matchers.ShouldMatchers._
import com.tactix4.simpleXmlRpc.util.{NoDiff, XMLRPCResponseGenerator}
import scala.xml.{Elem, Node}

import org.scalatest.prop._
import scala.xml._
import util.Diff._
import util.NoDiff._
import util.SimplePath._
import com.tactix4.simpleXmlRpc.util.{Comparison, NoDiff, XmlDiff, XMLRPCResponseGenerator}

/**
 * @author max@tactix4.com
 *         5/21/13
 */
class XmlRpcResponseFaultTest extends FunSuite with PropertyChecks{

val comp = new Comparison
 // implicit override val generatorDrivenConfig = PropertyCheckConfig(minSuccessful = 1000)

  test("Parse Valid Faults Correctly") {
    forAll(XMLRPCResponseGenerator.randomValidFaultGen){
      (node: Elem) =>
        val output = XmlRpcResponseFault(node).toNode
        assert(comp(node, output) === NoDiff)
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


