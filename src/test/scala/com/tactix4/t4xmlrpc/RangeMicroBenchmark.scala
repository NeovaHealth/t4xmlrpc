package com.tactix4.t4xmlrpc
import org.scalameter.api._

import org.scalacheck.{Gen => SGen}
import org.scalacheck.Gen.Params
import scala.collection.Iterator
import org.scalameter.Parameters
import com.tactix4.t4xmlrpc.util.XMLRPCResponseGenerator

/**
 * Created with IntelliJ IDEA.
 * User: max
 * Date: 19/02/14
 * Time: 19:38
 * To change this template use File | Settings | File Templates.
 */
object RangeMicroBenchmark extends PerformanceTest.Regression with XmlRpcResponses{

  implicit def scalacheck2scalameterGen[A](gen: SGen[A]): Gen[A] = new Gen[A] {
    def generate(params: Parameters): A = gen(params[Params](gen.label)).get
    def warmupset: Iterator[A] = Iterator.single(gen.sample.get)
    def dataset: Iterator[Parameters] = Iterator.single(Parameters(gen.label -> SGen.Params()))
  }
  val xmlRpcResponseNormal = XMLRPCResponseGenerator.randomValidResponseGen
  val sizes = Gen.range("size")(300000, 1500000, 300000)

  val ranges = for {
    size <- sizes
  } yield 0 until size
//
//  performance of "XMLRPC response parsing" in {
//    measure method "XmlRpcResponseNormal.apply" in {
//      using(xmlRpcResponseNormal) in {
//        r => XmlRpcResponseNormal()
//      }
//    }
//  }

  def persistor: Persistor = new SerializationPersistor

}
