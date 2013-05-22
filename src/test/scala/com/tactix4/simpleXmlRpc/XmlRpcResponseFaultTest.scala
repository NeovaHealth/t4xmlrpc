package com.tactix4.simpleXmlRpc

import org.scalatest.FunSuite

/**
 * @author max@tactix4.com
 *         5/21/13
 */
class XmlRpcResponseFaultTest extends FunSuite {

  val faultCode = 4
  val faultString = "Too many parameters"
  val fault = <methodResponse>
   <fault>
      <value>
         <struct>
            <member>
               <name>faultCode</name>
               <value><int>{faultCode}</int></value>
               </member>
            <member>
               <name>faultString</name>
               <value><string>{faultString}</string></value>
               </member>
            </struct>
         </value>
      </fault>
   </methodResponse>



  test("should be able to pull out fault code and fault string from fault") {
    val f = XmlRpcResponseFault(fault)
    assert(f.faultCode === faultCode)
    assert(f.faultString === faultString)
  }
}


