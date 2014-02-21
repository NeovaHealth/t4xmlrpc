package com.tactix4.t4xmlrpc.util

import org.scalacheck.Gen._
import org.scalacheck._
import org.scalacheck.Arbitrary._
import com.tactix4.t4xmlrpc._
import scalaz._
import scalaz.xml.Xml._
import scalaz.xml.Content

/**
 * Generate both invalid and valid XML-RPC responses, both Faults and 'normal' responses
 * The valid responses are far more comprehensive than the invalid ones, especially the Faults
 * Any suggestions/pull requests for improvements gratefully received
 *
 * @author max@tactix4.com
 *         5/22/13
 */

object XMLRPCResponseGenerator extends Properties("XML-RPC Response generator") {

 implicit def arbitraryValidXmlRpcResponse: Arbitrary[List[Content]] =
    Arbitrary { randomValidResponseGen }

  implicit def arbitraryValidXmlRpcFault: Arbitrary[List[Content]] =
    Arbitrary { randomValidFaultGen }

  implicit def arbitraryInValidXmlRpcResponse: Arbitrary[List[Content]] =
    Arbitrary { randomInValidResponseGen}

  implicit def arbitraryInValidXmlRpcFault: Arbitrary[List[Content]] =
    Arbitrary { randomInValidFaultGen }

  def t: Gen[String] = rDate
  /**
   * Generate a random valid XML-RPC response using a random selection of the valid generators
   * @return a [[org.scalacheck.Gen[Elem] ]] that generates an [[scala.xml.Elem]] making up a valid XML-RPC response
   */
  def randomValidResponseGen: Gen[List[Content]] = Gen.sized{ size =>
    for {
    p <- Gen.listOfN(size+1,randParam)
  } yield s"<methodResponse><params>${p.mkString}</params></methodResponse>".parseXml
  }

 def randomValidResponseGen(size: Int): Gen[List[Content]] =
    for {
    p <- Gen.listOfN(size+1,randParam)
  } yield s"<methodResponse><params>${p.mkString}</params></methodResponse>".parseXml

  /**
   * Generate a random invalid XML-RPC response using a random selection of the invalid generators
   * @return a [[org.scalacheck.Gen[Elem] ]] that generates an [[scala.xml.Elem]] making up an invalid XML-RPC response
   */
   def randomInValidResponseGen: Gen[List[Content]] = Gen.sized { size => randIParam(size+1).map(p => s"<methodResponse><params>${p.mkString}</params></methodResponse>".parseXml)}

  def randomInValidResponseGen(size: Int) : Gen[List[Content]] = randIParam(size+1).map(p => s"<methodResponse><params>${p.mkString}</params></methodResponse>".parseXml)

  /**
   * Generate a random valid XML-RPC Fault using a random selection of the valid generators
   * @return a [[org.scalacheck.Gen[Elem] ]] that generates an [[scala.xml.Elem]] making up a valid XML-RPC fault
   */
  def randomValidFaultGen: Gen[List[Content]] =  rFaultStruct.map(fs => s"<methodResponse><fault>$fs</fault></methodResponse>".parseXml)

  /**
   * Generate a random invalid XML-RPC Fault using a random selection of the invalid generators
   * @return a [[org.scalacheck.Gen[Elem] ]] that generates an [[scala.xml.Elem]] making up a invalid XML-RPC fault
   */
  def randomInValidFaultGen: Gen[List[Content]] = for {
    fs <- rIFaultStruct
  }yield s"<methodResponse><fault>$fs</fault></methodResponse>".parseXml


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

  def rDouble: Gen[String]      = for {d <- arbDouble.arbitrary}    yield s"<value><double>$d</double></value>"
  def rInt: Gen[String]         = for {i <- arbInt.arbitrary}       yield s"<value><int>$i</int></value>"
  def rBoolean: Gen[String]     = for {b <- Gen.oneOf(0, 1)}        yield s"<value><boolean>$b</boolean></value>"
  def rString: Gen[String]      = oneOf(rString1,rString2)
  def rString1: Gen[String]     = for {s <- nonEmptyAlphaNumStr}    yield s"<value><string>$s</string></value>"
  def rString2: Gen[String]     = for {s <- nonEmptyAlphaNumStr}    yield s"<value>$s</value>"
  def rB64: Gen[String]         = for {b64: String <- arbString.arbitrary}  yield s"<value><base64>${new sun.misc.BASE64Encoder().encode(b64.getBytes("UTF-8"))}</base64></value>"
  def rDate: Gen[String]        = for {date <- arbDate.arbitrary}   yield s"<value><date>${getDateAsISO8601String(date)}</date></value>"

  def rStruct : Gen[String]     = for {
    n <- choose(1,5)
    m  <- pickNGen(n)
    n  <- listOfN(n,nonEmptyAlphaNumStr)
    member =  n zip m
  } yield s"<value><struct>${member.map(m => s"<member><name>${m._1}</name>${m._2}</member>").mkString}</struct></value>"

 def rArray : Gen[String]      = for {
    n <- choose(1, 5)
    m  <-  pickNGen(n)
  } yield s"<value><array><data>${m.mkString}</data></array></value>"

  def pickNGen(n:Int): Gen[Seq[String]] = listOfN(n,pickOneGen)
  def pickOneGen: Gen[String] = Gen.lzy(oneOf(rDouble, rInt, rBoolean, rString, rB64, rDate, rArray,rStruct))

  def randParam: Gen[String] =  pickOneGen.map(t => s"<param>${t.mkString}</param>")



  /**
   * The invalid data generators
   */
  def rIDouble: Gen[String]    = for {d <- oneOf(rString, rB64, rDate)}      yield s"<value><double>$d</double></value>"
  def rIInt: Gen[String]        = for {i <- oneOf(rICInt, rISInt)}           yield i
  def rICInt: Gen[String]       = for {i <- oneOf(rDate,rString,rB64)}       yield s"<value><int>${i}</int></value>"
  def rISInt: Gen[String]       = for {i <- arbString.arbitrary}                yield s"<value><double>${i}</double></value>"
  def rIBoolean: Gen[String]   = for {b <- oneOf(rDouble, rDate, rString, rB64)}       yield s"<value><boolean>${b}</boolean></value>"
  def rIDate: Gen[String]      = for {date <- arbString.arbitrary}           yield s"<value><date>${date}</date></value>"

  def rIStruct : Gen[String] = for {
    n <- choose(1,5)
    m  <- pickNIGen(n)
    badName <- listOfN(n, arbString.arbitrary)
    t = m zip badName
  } yield s"<value><struct>${t.map(z => s"<member><name>${z._2}</name>${z._1}</member>").mkString}</struct></value>"



 def rIArray : Gen[String]      = for {
    n <- choose(1, 5)
    m  <-  pickNIGen(n)
  } yield s"<value><array><data>${m.mkString}</data></array></value>"


  def pickNIGen(n: Int): Gen[Seq[String]] = for {
    x <- pickOneIGen
    y <- pickNGen(n-1)
  } yield scala.util.Random.shuffle(y++Seq(x))


  def pickOneIGen: Gen[String] = Gen.lzy(oneOf(rIDouble, rIInt, rIBoolean, rIDate, rIArray,rIStruct))

  def randIParam(i: Int): Gen[Seq[String]] =  pickNIGen(i).map((s: Seq[String]) => s.map(p =>  s"<param>$p</param>"))

  def rFaultStruct : Gen[String] = for {
    c <- rInt
    s <- rString
  } yield  s"<value><struct><member><name>faultCode</name>$c</member><member><name>faultString</name>$s</member></struct></value>"

  def rIFaultStruct : Gen[String] = for {
    c <- oneOf(rDate,rB64,rBoolean)
    s <- arbString.arbitrary
  } yield  s"<value><struct><member><name>faultCode</name>$c</member><member><name>faultString</name>$s</member></struct></value>"


}
