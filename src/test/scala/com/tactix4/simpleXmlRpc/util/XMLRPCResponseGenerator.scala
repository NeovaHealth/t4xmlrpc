package com.tactix4.simpleXmlRpc.util

import _root_.util.XmlRpcUtils
import java.text.SimpleDateFormat
import java.util.{TimeZone, Date}
import org.scalacheck.Gen
import scala.xml.{Elem, Node}
import org.scalacheck._
import org.scalacheck.Arbitrary._
import org.scalacheck.Prop._/**
 * @author max@tactix4.com
 *         5/22/13
 */

//TODO: finish add array and struct types - see //http://etorreborre.blogspot.co.uk/2011/02/scalacheck-generator-for-json.html
object XMLRPCResponseGenerator extends Properties("XML-RPC Reponse generator") {



    val rDouble: Gen[Elem] = for {d <- arbDouble.arbitrary} yield
      <value>
        <double>
          {d}
        </double>
      </value>
    val rInt: Gen[Elem] = for {i <- arbInt.arbitrary} yield
      <value>
        <int>
          {i}
        </int>
      </value>
    val rBoolean: Gen[Elem] = for {b <- Gen.oneOf(0, 1)} yield <value>
      <boolean>
        {b}
      </boolean>
    </value>
    val rString: Gen[Elem] = for {s <- arbString.arbitrary} yield <value>
      <string>
        {s}
      </string>
    </value>
    val rB64: Gen[Elem] = for {b64 <- arbString.arbitrary} yield <value>
      <base64>
        {b64}
      </base64>
    </value>
    val rDate: Gen[Elem] = for {date <- arbDate.arbitrary} yield <value>
      <date>
        {XmlRpcUtils.getDateAsISO8601String(date)}
      </date>
    </value>


    lazy val types: List[Gen[Elem]] = List(rDouble, rInt, rBoolean, rString, rB64, rDate)


    val randParam: Gen[Seq[Node]] = for {
      t <- Gen.someOf(types)
    } yield {
      t map (x => <param>
        {x.sample.get}
      </param>)
    }

    val randomRequestGen: Gen[Node] = for {
      p <- Gen.listOf(randParam)
    } yield <methodResponse>
        <params>
          {p}
        </params>
      </methodResponse>


}
