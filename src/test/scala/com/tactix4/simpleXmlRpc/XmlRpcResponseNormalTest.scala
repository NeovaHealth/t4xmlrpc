package com.tactix4.simpleXmlRpc

import org.scalatest.FunSuite
import org.scalatest.prop.PropertyChecks
import scala.xml.Node
import com.tactix4.simpleXmlRpc.util.XMLRPCResponseGenerator


/**
 * @author max@tactix4.com
 *         5/21/13
 */
class XmlRpcResponseNormalTest extends FunSuite with PropertyChecks {



  //TODO: make tail recursive
  def countTotalDataTypes(list: List[XmlRpcDataType]): Int = list match {
    case Nil => 0
    case x :: xs => (x match {
      case y: XmlRpcArray => countTotalDataTypes(y.value)
      case y: XmlRpcStruct => countTotalDataTypes(y.value.map((tuple: (String, XmlRpcDataType)) => tuple._2))
      case _ => 1
    }) + countTotalDataTypes(xs)
  }

  val simpleTestXml =
    <methodCall>
      <methodName>examples.getStateName</methodName>
      <params>
        <param>
          <value>
            <i4>41</i4>
          </value>
        </param>
      </params>
    </methodCall>

  val lessSimpleTestXml =
    <methodCall>
      <methodName>examples.getStateName</methodName>
      <params>
        <param>
          <value>
            <i4>41</i4>
          </value>
        </param>
        <param>
          <value>
            <double>41.0289</double>
          </value>
        </param>
        <param>
          <value>
            <string>yo!</string>
          </value>
        </param>
      </params>
    </methodCall>


  val notSimpleTestXml =
    <methodCall>
      <methodName>examples.getStateName</methodName>
      <params>
        <param>
          <value>
            <array>
              <data>
                <value>
                  <i4>12</i4>
                </value>
                <value>
                  <string>Egypt</string>
                </value>
                <value>
                  <boolean>0</boolean>
                </value>
                <value>
                  <i4>-31</i4>
                </value>
              </data>
            </array>
          </value>
        </param>
        <param>
          <value>
            <struct>
              <member>
                <name>lowerBound</name>
                <value>
                  <i4>18</i4>
                </value>
              </member>
              <member>
                <name>upperBound</name>
                <value>
                  <i4>139</i4>
                </value>
              </member>
            </struct>
          </value>
        </param>
        <param>
          <value>
            <string>another string yeah!</string>
          </value>
        </param>
      </params>
    </methodCall>


  test("input should match internal representation") {

    forAll(XMLRPCResponseGenerator.randomRequestGen) {
      (node: Node) => {
        val input = node.mkString
        val output = XmlRpcResponseNormal(node).toString
        input.equals(output)
      }
    }

  }
  test("get Param should be able to extract an Int") {
    val result = XmlRpcResponseNormal(simpleTestXml)
    assert(result.params.length === 1)

  }

  test("getParam should be able to extract multiple params") {
    val result = XmlRpcResponseNormal(lessSimpleTestXml)
    assert(result.params.length === 3)
  }

  test("getParam should be able to extract multiple complex params") {

    val result = XmlRpcResponseNormal(notSimpleTestXml)
    assert(result.params.length === 3)
    assert(countTotalDataTypes(result.params) === 7)
  }


}
