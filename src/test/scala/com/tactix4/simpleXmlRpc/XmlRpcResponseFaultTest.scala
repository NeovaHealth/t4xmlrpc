package com.tactix4.simpleXmlRpc

import org.scalatest.FunSuite
import org.scalatest.prop.PropertyChecks
import org.scalatest.matchers.ShouldMatchers._
import com.tactix4.simpleXmlRpc.util.XMLRPCResponseGenerator
import scala.xml.Node
import scalaz.NonEmptyList

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
      println(node)
        val output = XmlRpcResponseFault(node)
        output.fold(
          (list: NonEmptyList[String]) => fail(list.toString()),
          (fault: XmlRpcResponseFault) => input should equal(fault.toNode))
    }
  }

  test("Fail on Invalid Faults") {
    forAll(XMLRPCResponseGenerator.randomInValidFaultGen){
      (node: Node) =>
        println(node)
          val failure = XmlRpcResponseFault(node)
        println(failure)
        failure.isFailure
    }
  }
}


