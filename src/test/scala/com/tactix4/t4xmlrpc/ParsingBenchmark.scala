package com.tactix4.t4xmlrpc

import org.scalameter.api._

import org.scalacheck.{Gen => SGen}
import scala.collection.Iterator
import org.scalameter.Parameters
import com.tactix4.t4xmlrpc.util.XMLRPCResponseGenerator
import org.scalacheck.Gen.Params
import scalaz.xml.Content


object ParsingBenchmark extends PerformanceTest.Regression with XmlRpcResponses {

  def persistor: Persistor = new SerializationPersistor("/tmp/test")

  override def reporter: Reporter = Reporter.Composite(
    new RegressionReporter(
      RegressionReporter.Tester.OverlapIntervals(),
      RegressionReporter.Historian.ExponentialBackoff()),
    HtmlReporter(true)
  )

  implicit def scalacheck2scalameterGen[A](gen: SGen[A]): Gen[A] = new Gen[A] {
    def generate(params: Parameters): A = gen(params[Params](gen.label)).get
    def warmupset: Iterator[A] = Iterator.single(gen.sample.get)
    def dataset: Iterator[Parameters] = Iterator.single(Parameters(gen.label -> SGen.Params()))
  }

  def validNormalResponses: Gen[List[Content]] = for {
    size <- sizes
    array: List[Content] <- XMLRPCResponseGenerator.randomValidResponseGen(size) :| "Valid Normal Response"
  } yield array

  def inValidNormalResponses: Gen[List[Content]] = for {
    size <- sizes
    array: List[Content] <- XMLRPCResponseGenerator.randomInValidResponseGen(size) :| "Invalid Normal Response"
  } yield array

  def validFaultResponses: Gen[List[Content]] = for {
    size <- sizes
    f <- XMLRPCResponseGenerator.randomValidFaultGen :| "Valid Fault Response"
  } yield f

  def inValidFaultResponses: Gen[List[Content]] = for {
    size <- sizes
    f <- XMLRPCResponseGenerator.randomInValidFaultGen :| "InValid Fault Response"
  } yield f

  val sizes = Gen.range("size")(10, 1010, 50)

  performance of "XML-RPC response parsing" in {
    performance of "createXmlRpcResponse" in {
      measure method "XmlRpcResponseNormal" in {
        using(validNormalResponses) in {
          r => createXmlRpcResponse(r)
        }
      }
      measure method "XmlRpcResponseNormal with Errors" in {
        using(inValidNormalResponses) in {
          r => createXmlRpcResponse(r)
        }
      }
      measure method "XmlRpcResponseFault" in {
        using(validFaultResponses) in {
          r => createXmlRpcResponse(r)
        }
      }
      measure method "XmlRpcResponseFault with Errors" in {
        using(inValidFaultResponses) in {
          r => createXmlRpcResponse(r)
        }
      }
    }
  }


}
