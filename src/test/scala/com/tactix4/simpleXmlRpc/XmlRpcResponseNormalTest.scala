package com.tactix4.simpleXmlRpc

import scala.xml._
import util.Diff._
import util.NoDiff._
import util.SimplePath._
import com.tactix4.simpleXmlRpc.util.{Comparison, NoDiff, XmlDiff, XMLRPCResponseGenerator}
import org.scalatest.FunSuite
import org.scalatest.prop._
import org.scalacheck.Prop._
import org.scalatest.matchers.ShouldMatchers._

/**
 * @author max@tactix4.com
 *         5/21/13
 */
class XmlRpcResponseNormalTest extends FunSuite with PropertyChecks{

//  implicit override val generatorDrivenConfig = PropertyCheckConfig(minSuccessful = 500)

  val comp = new Comparison
  test("internal representation should match input") {
    forAll(XMLRPCResponseGenerator.randomValidResponseGen) {
      (node: Elem) => {
        val input = node
        val output = XmlRpcResponseNormal(node).toElem
        assert(comp(input, output) === NoDiff)
      }
    }
  }

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
