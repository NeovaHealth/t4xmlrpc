package com.tactix4.simpleXmlRpc.util

/* Copyright (c) 2011 Jay A. Patel <jay@patel.org.in>
 * Copyright (c) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import scala.xml.{Elem, Node, MetaData, Text, Comment}

/**
 * A comparison class. The 'expected' element is compared against 'actual'. They match
 * when all elements and attributes in 'expected' are found in 'actual' and are equal 
 * to the expected ones. This means that 'actual' might have additional elements and
 * attributes that don't make the comparison fail. 
 * 
 * The order of elements does not matter. Ignore paths can be specified, in which 
 * case the elements matching those are skipped when comparing. If 'notext' is true,
 * text nodes (contents of attributes and elements) are ignored (only document structure
 * matters).
 * 
 * @author Iulian Dragos
 */
class Comparison extends ((Elem, Elem) => XmlDiff) {
  /** The XPath from the root node. */
  var path: List[Elem] = Nil
  
  /** List of XPath-like expressions to be ignored */
  var ignorePaths: List[SimplePath] = Nil
  
  /** Ignore text nodes? */
  var notext = false
  
  def apply(exp: Elem, act: Elem): XmlDiff = compare(exp, act)
  
  /**
   * Compare a list of nodes to another. Element order does not matter, but
   * other nodes are required to be in the same order.
   */
  def compareElems(exp: List[Node], act: List[Node]): XmlDiff = {
    var es = exp
    var fs = act
    
    while (es != Nil) {
      if (fs.isEmpty)
        return Diff(path, "Expected <" +es.head.label + ">.")
      es.head match {
        case e1: Elem =>
          if (ignored(e1.label)) {
            es = es.tail
          } else {
            val others = fs filter {
              case e2: Elem => sameElem(e1, e2)
              case _ => false
            }
            val results = others.map(compare(e1, _))
            val theGoodOne = results.find(_.isSimilar)
            if (theGoodOne.isEmpty) {
              if (results.isEmpty)
                return error(e1)
              else
                return Diff(path, "None of the elements found fully matches <" 
                    + e1.label + ">: \n\t" + results.mkString("", "\n\t", ""))
            } else {
              es = es.tail
              fs = fs.filterNot(_ == theGoodOne.get)
            }
          }
          
        case Text(t) if (t.trim.length == 0) => 
          es = es.tail // ignore whitespace
          
        case _ => 
          val res = compare(es.head, fs.head)
          if (!res.isSimilar) return res
          es = es.tail
          fs = fs.tail
      }
    }
    NoDiff
  }
  
  /** Give an error saying 'exp' was expected. */
  private def error(exp: Node): XmlDiff = {
    val sb = new StringBuilder
    sb.append("Expected: ")
    exp.nameToString(sb)
    Diff(path, sb.toString)
  }
  
  private def wrongAttributes(pref: String, e: Elem, exp: MetaData, actual: MetaData): XmlDiff = {
    val sb = new StringBuilder(64)
    sb.append(pref)
    e.nameToString(sb)
      .append("\n\tExpected: <").append(exp).append(">")
      .append("\n\tFound: <").append(actual).append(">")
    Diff(path, sb.toString)
  }

  /** Returns true if 'label' is an ignored element. */
  private def ignored(label: String): Boolean = {
    val ps = label :: (path map (_.label))
    ignorePaths.exists(_.matches(ps.reverse))
  }
  
  /** Returns 'true' if e1 and e2 have the same qualified name, or e1 is ignored. */
  def sameElem(e1: Elem, e2: Elem): Boolean =
    (ignored(e1.label) 
        || (e1.label == e2.label
            && e1.scope.getURI(e1.prefix) == e2.scope.getURI(e2.prefix)))

  /** Returns 'true' if the attributes in e1 are included in e2.  */
  private def includesAttributes(e1: Elem, e2: Elem): Boolean = {
    def contains(a: MetaData) = {
      val e2AttributeValue =
        if (a.isPrefixed)
          e2.attributes(a.getNamespace(e1), e2.scope, a.key)
        else
          e2.attributes(a.key)
      (e2AttributeValue != null) && (notext || e2AttributeValue == a.value)
    }
    
    e1.attributes.filter(_.value != null).forall(contains(_))
  }

  /**
   * Compare 'expected' to 'actual'. Returns 'NoDiff' if the attributes and content 
   * (including children) of 'expected' exist and are the same in 'actual'. 'actual'
   * may have additional elements/attributes. Comments are ignored, whitespace is 
   * trimmed.
   */
  def compare(expected: Node, actual: Node): XmlDiff = {
    (expected, actual) match {
      case (Comment(_), _) | (_, Comment(_)) => 
        NoDiff  // ignore comments
        
      case (Text(t1), Text(t2)) => 
        if (notext || t1.trim == t2.trim) 
          NoDiff 
        else
          Diff(path, "Expected " + t1 + " but " + t2 + " found.")
        
      case (e1: Elem, e2: Elem) =>
        path = e1 :: path

        val res = 
          if (ignored(e1.label)) 
            NoDiff 
          else if (sameElem(e1, e2)) {
            if (includesAttributes(e1, e2))
              compareElems(e1.child.toList, e2.child.toList)
            else
              wrongAttributes("Attributes are different at ", e1, e1.attributes, e2.attributes)
          } else {
            val sb = new StringBuilder(128)
            sb.append("Expected ")
            e1.nameToString(sb)
            sb.append(" but ")
            sb.append(e2.nameToString(sb))
            Diff(path, sb.toString)
          }
        path = path.tail
        res
        
      case (x: Node, y: Node) =>
        if (notext || x.text == y.text) 
          NoDiff 
        else
          Diff(path, "Expected " + x + " but " + y + " found.")
    }
  }
}
