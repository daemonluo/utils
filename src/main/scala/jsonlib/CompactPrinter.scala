package com.daemon.utils.jsonlib

import java.lang.StringBuilder

trait CompactPrinter extends JsonPrinter {
    def print(x: JsValue, sb: StringBuilder){
        x match {
            case JsObject(x) => printObject(x, sb)
            case JsArray(x) => printArray(x, sb)
            case _ => printLeaf(x, sb)
        }
    }

    protected def printObject(members: Map[String, JsValue], sb: StringBuilder){
        sb.append('{')
        printSeq(members, sb.append(',')){ m =>
            printString(m._1, sb)
            sb.append(':')
            print(m._2, sb)
        }
        sb.append('}')
    }

    protected def printArray(elements: Seq[JsValue], sb: StringBuilder){
        sb.append('[')
        printSeq(elements, sb.append(','))(print(_, sb))
        sb.append(']')
    }
}

object CompactPrinter extends CompactPrinter
