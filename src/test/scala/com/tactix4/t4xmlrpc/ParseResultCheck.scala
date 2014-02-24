package com.tactix4.t4xmlrpc

/**
 * @author max@tactix4.com
 *         24/02/2014
 */

import org.scalacheck._
import org.scalacheck.Arbitrary._
import org.scalatest.FunSuite
import org.scalatest.prop.PropertyChecks

class ParseResultCheck extends FunSuite with PropertyChecks with XmlRpcResponses {

  val prGen: Gen[ParseResult[String, String]] = for {
    e <- Gen.listOf(arbString.arbitrary)
    r <- arbOption(arbString).arbitrary
  } yield ParseResult(e, r)


  test("testing flatmap accumulates errors regardless of results") {
    forAll(prGen, prGen)((pr1: ParseResult[String, String], pr2: ParseResult[String, String]) => {
      val fm = pr1.flatMap((s: String) => pr2)
      fm.errors == pr1.errors ++ pr2.errors && {
        (pr1.result, pr2.result) match {
          case (r: Some[String], r2: Some[String]) => fm.result == r2
          case (r: Some[String], None) => fm.result == None
          case (None, r: Some[String]) => fm.result == None
          case (None, None) => fm.result == None
        }
      }
    }
    )

  }


}

