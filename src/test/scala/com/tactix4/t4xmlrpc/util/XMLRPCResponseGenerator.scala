package com.tactix4.t4xmlrpc.util

import org.scalacheck.Gen._
import scala.xml.{Elem, Node}
import org.scalacheck._
import org.scalacheck.Arbitrary._
import com.tactix4.t4xmlrpc._
/**
 * Generate both invalid and valid XML-RPC responses, both Faults and 'normal' responses
 * The valid responses are far more comprehensive than the invalid ones, especially the Faults
 * Any suggestions/pull requests for improvements gratefully received
 *
 * @author max@tactix4.com
 *         5/22/13
 */

object XMLRPCResponseGenerator extends Properties("XML-RPC Reponse generator") {

  /**
   * Generate a random valid XML-RPC response using a random selection of the valid generators
   * @return a [[org.scalacheck.Gen[Elem] ]] that generates an [[scala.xml.Elem]] making up a valid XML-RPC response
   */
  def randomValidResponseGen: Gen[Elem] = for {
    p <- Gen.listOf(randParam)
  } yield <methodResponse><params>{p}</params></methodResponse>

  /**
   * Generate a random invalid XML-RPC response using a random selection of the invalid generators
   * @return a [[org.scalacheck.Gen[Elem] ]] that generates an [[scala.xml.Elem]] making up an invalid XML-RPC response
   */
   def randomInValidResponseGen: Gen[Node] = for {
    p <- Gen.listOf(randIParam)
  } yield <methodResponse><params>{p}</params></methodResponse>

  /**
   * Generate a random valid XML-RPC Fault using a random selection of the valid generators
   * @return a [[org.scalacheck.Gen[Elem] ]] that generates an [[scala.xml.Elem]] making up a valid XML-RPC fault
   */
  def randomValidFaultGen: Gen[Elem] = for {
    fs <- rFaultStruct
  }yield <methodResponse><fault>{fs}</fault></methodResponse>

  /**
   * Generate a random invalid XML-RPC Fault using a random selection of the invalid generators
   * @return a [[org.scalacheck.Gen[Elem] ]] that generates an [[scala.xml.Elem]] making up a invalid XML-RPC fault
   */
  def randomInValidFaultGen: Gen[Elem] = for {
    ifs <- rIFaultStruct
  }yield <methodResponse><fault>{ifs}</fault></methodResponse>


  /**
   * The valid data generators
   */

  def nonEmptyAlphaNumStr: Gen[String] = for {
    an <- choose(1,10)
    bn <- choose(0,9)
    cn <- choose(0,3)
    c <- listOfN(an,alphaChar)
    n <- listOfN(bn,numChar)
    u <- Gen.pick(cn,"_",":",";","/",".")
  } yield scala.util.Random.shuffle(c ++ n ++ u).mkString

  def rDouble: Gen[Elem]      = for {d <- arbDouble.arbitrary}    yield <value><double>{d}</double></value>
  def rInt: Gen[Elem]         = for {i <- arbInt.arbitrary}       yield <value><int>{i}</int></value>
  def rBoolean: Gen[Elem]     = for {b <- Gen.oneOf(0, 1)}        yield <value><boolean>{b}</boolean></value>
  def rString: Gen[Elem]      = oneOf(rString1,rString2)
  def rString1: Gen[Elem]     = for {s <- nonEmptyAlphaNumStr}    yield <value><string>{s}</string></value>
  def rString2: Gen[Elem]     = for {s <- nonEmptyAlphaNumStr}     yield <value>{s}</value>
  def rB64: Gen[Elem]         = for {b64 <- arbString.arbitrary}  yield <value><base64>{new sun.misc.BASE64Encoder().encode(b64.getBytes)}</base64></value>
  def rDate: Gen[Elem]        = for {date <- arbDate.arbitrary}   yield <value><date>{getDateAsISO8601String(date)}</date></value>
  def rStruct : Gen[Elem]     = for {
    n <- choose(1,3)
    m :Seq[Gen[Elem]] <- pickNGen(n)
  } yield <value><struct>{m.map((gen: Gen[Elem]) => <member><name>{nonEmptyAlphaNumStr.sample.get}</name>{gen.sample.get}</member>)}</struct></value>
  def rArray : Gen[Elem]      = for {
    n <- choose(1, 3)
    m :Seq[Gen[Elem]] <-  pickNGen(n)
  } yield <value><array><data>{m.map(_.sample.get)}</data></array></value>

  def pickNGen(n: Int)        = pick(n, List(rDouble, rInt, rBoolean, rString, rB64, rDate, rArray,rStruct))

  def randParam: Gen[Seq[Node]] = for {
    n <- choose(1,5)
    t <- pickNGen(n)
  } yield  t.map(x => <param>{x.sample.get}</param>)


  /**
   * The invalid data generators
   */
  def rIDouble : Gen[Elem]    = for {d <- oneOf(rICDouble, rISDouble,rIS2Double)}  yield d
  def rICDouble: Gen[Elem]    = for {d <- oneOf(rString, rB64, rDate)}             yield <value><double>{d}</double></value>
  def rISDouble: Gen[Elem]    = for {d <- arbDouble.arbitrary}                     yield <valua><double>{d}</double></valua>
  def rIS2Double: Gen[Elem]   = for {d <- arbDouble.arbitrary}                     yield <value><doouble>{d}</doouble></value>
  def rIInt: Gen[Elem]        = for {i <- oneOf(rICInt, rISInt, rIS2Int)}  yield i
  def rICInt: Gen[Elem]       = for {i <- oneOf(rDate,rString,rB64)}       yield <value><int>{i}</int></value>
  def rISInt: Gen[Elem]       = for {i <- arbInt.arbitrary}                yield <value><intt>{i}</intt></value>
  def rIS2Int : Gen[Elem]     = for {i <- arbInt.arbitrary}                yield <velue><int>{i}</int></velue>
  def rIBoolean: Gen[Elem]    = for {b <- oneOf(rICBoolean, rISBoolean, rIS2Boolean)} yield b
  def rICBoolean: Gen[Elem]   = for {b <- oneOf(rDouble, rDate, rString, rB64)}       yield <value><boolean>{b}</boolean></value>
  def rISBoolean: Gen[Elem]   = for {b <- Gen.oneOf(0, 1)}                            yield <v-alue><boolean>{b}</boolean></v-alue>
  def rIS2Boolean: Gen[Elem]  = for {b <- Gen.oneOf(0, 1)}                            yield <value><bolean>{b}</bolean></value>
  def rIString: Gen[Elem]     = for {s <- oneOf(rICString, rISString, rIS2String)}  yield s
  def rICString: Gen[Elem]    = for {s <- oneOf(rArray,rStruct)}                    yield <value><string>{s}</string></value>
  def rISString: Gen[Elem]    = for {s <- arbString.arbitrary}                      yield <value><string><string></string>{s}</string></value>
  def rIS2String: Gen[Elem]   = for {s <- arbString.arbitrary}                      yield <value><int><string>{s}</string></int></value>
  def rIB64: Gen[Elem]        = for {b64 <- oneOf(rCIB64,rSIB64,rS2IB64)}   yield b64
  def rCIB64: Gen[Elem]       = for {b64 <- oneOf(rArray,rStruct)}          yield <value><base64>{b64}</base64></value>
  def rSIB64: Gen[Elem]       = for {b64 <- arbString.arbitrary}            yield <value><base>{b64}</base></value>
  def rS2IB64: Gen[Elem]      = for {b64 <- arbString.arbitrary}            yield <value><base64><int>{b64}</int></base64></value>
  def rIDate: Gen[Elem]       = for {date <- oneOf(rCIDate,rSIDate,rS2IDate)} yield date
  def rCIDate: Gen[Elem]      = for {date <- arbDate.arbitrary}               yield <value><date>{date}</date></value>
  def rSIDate: Gen[Elem]      = for {date <- arbDate.arbitrary}               yield <value><date><int></int>{getDateAsISO8601String(date)}</date></value>
  def rS2IDate: Gen[Elem]     = <value><date>2011-02-29T03:44Z</date></value>

  def rIStruct : Gen[Elem] = oneOf(rCIStruct, rSIStruct, rS2IStruct)

  def rCIStruct : Gen[Elem] = for {
    n <- choose(1,3)
    m :Seq[Gen[Elem]] <- pickNGen(n)
  } yield <value><struct>{m.map((gen: Gen[Elem]) => <member><name>{oneOf(rArray, rStruct).sample.get}</name>{gen.sample.get}</member>)}</struct></value>

  def rSIStruct : Gen[Elem] = for {
    n <- choose(1,3)
    m :Seq[Gen[Elem]] <- pickNGen(n)
} yield <value><struct>{m.map((gen: Gen[Elem]) => <member><uwotmate><name>{arbString.arbitrary.sample.get}</name></uwotmate>{gen.sample.get}</member>)}</struct></value>

  def rS2IStruct : Gen[Elem] = for {
    n <- choose(1,3)
    m :Seq[Gen[Elem]] <- pickNGen(n)
  } yield <value><struct>{m.map((gen: Gen[Elem]) => <member>{arbString.arbitrary.sample.get}{gen.sample.get}</member>)}</struct></value>

  def rIArray : Gen[Elem] = oneOf(rSIArray, rS2IArray)

  def rSIArray : Gen[Elem] = for {
    n <- choose(1, 3)
    m :Seq[Gen[Elem]] <-  pickNGen(n)
  } yield <value><array>{m.map(_.sample.get)}</array></value>

  def rS2IArray : Gen[Elem] = for {
    n <- choose(1, 3)
    m :Seq[Gen[Elem]] <-  pickNGen(n)
  } yield <array><data>{m.map(_.sample.get)}</data></array>

  def pickNIGen(n: Int) = for {
    x <- pick(1,rIString,rIInt, rIBoolean, rIDouble, rIB64,rIDate,rIStruct,rIArray)
    y <- pick(n-1,  rDouble, rInt, rBoolean, rString, rB64, rDate, rArray,rStruct)
  } yield scala.util.Random.shuffle(x++y)

  def randIParam: Gen[Seq[Node]] = for {
    n <- choose(1,5)
    t <- pickNIGen(n)
  } yield  t.map(x => <param>{x}</param>)


  def rFaultStruct : Gen[Elem] = for {
    c <- rInt
    s <- rString
  } yield  <value><struct>
      <member>
        <name>faultCode</name>
        {c.sample.get}
      </member>
      <member>
        <name>faultString</name>
        {s.sample.get}
      </member>
    </struct>
    </value>

  def rIFaultStruct : Gen[Elem] = for { f <- oneOf(rIFault1,rIFault2,rIFault3,rIFault4,rIFault5,rIFault6,rIFault7,rIFault8,rIFault9,rIFault10,rIFault11)} yield f.sample.get


  def rIFault1 = <value><struct>
    <member>
      <name>faultCode</name>
      {rDate.sample.get}
    </member>
    <member>
      <name>faultString</name>
      {rString.sample.get}
    </member>
  </struct>
  </value>

  def rIFault2 = <value><struct>
    <member>
      <name>faultCode</name>
      {rInt.sample.get}
    </member>
    <member>
      {rString.sample.get}
    </member>
  </struct>
  </value>

  def rIFault3 = <value><struct>
    <member>
      {rInt.sample.get}
    </member>
    <member>
      <name>faultString</name>
      {rString.sample.get}
    </member>
  </struct>
  </value>

  def rIFault4 =
    <value><struct>
    <member>
      <name>faultCode</name>
      {rInt.sample.get}
      <name>faultCode</name>
      {rString.sample.get}
    </member>
  </struct>
  </value>

  def rIFault5 =
    <value>
      <member>
        <name>faultCode</name>
        {rInt.sample.get}
        </member>
      <member>
        <name>faultString</name>
        {rString.sample.get}
      </member>
      <member><name>faultFoo</name>{rString.sample.get}</member>
    </value>
  def rIFault6 =
    <struct>
      <member>
        <name>faultCode</name>
        {rInt.sample.get}
      </member>
      <member>
        <name>faultString</name>
        {rString.sample.get}
      </member>
      </struct>
  def rIFault7 =
    <value>
      <member>
        <name>faultCode</name>
        {rInt.sample.get}
      </member>
    </value>

  def rIFault8 =
    <value><struct>
      <member>
        <name>faultString</name>
        {rString.sample.get}
      </member>
      </struct>
    </value>

  def rIFault9 =
    <value><struct>
        <name>faultCode</name>
        {rInt.sample.get}
      <member>
        <name>fault string</name>
        {rString.sample.get}
      </member>
    </struct>
    </value>

  def rIFault10 =
    <value><struct>
      <member>
        <name>{rString.sample.get}</name>
        {rInt.sample.get}
      </member>
      <member>
        <name>faultString</name>
        {rString.sample.get}
      </member>
    </struct>
    </value>


  def rIFault11 =
    <value><array>
      <data>
        <name>{rString.sample.get}</name>
        {rInt.sample.get}
        <name>faultString</name>
        {rString.sample.get}
        </data>
      </array>
    </value>


}
