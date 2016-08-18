package utils

import scala.language.implicitConversions

class NumbericString(val string: String) {
    private val MAX_INTEGER_STR = "2147483647"
    private val MIN_INTEGER_STR = "2147483648"
    private val MAX_LONG_STR = "9223372036854775807"
    private val MIN_LONG_STR = "9223372036854775808"

    def get = string

    def isNumberic = string.matches("""(?i)[+-]?((\d+l?)|(((\d+(\.\d*)?)|(\.\d+))(e[+-]?\d+)?f?))""")

    def isInteger = {
        if(string.matches("""[+-]?(\d+)""")){
            var str = string
            var length = string.length
            val positive = !str.startsWith("-")
            if(str.startsWith("-") || str.startsWith("-")){
                str = string.slice(1, length)
                length -= 1
            }
            if(length > 10){
                false;
            }else if(length < 10){
                true
            }else{
                if(positive){
                    str.compare(MAX_INTEGER_STR) <= 0
                }else{
                    str.compare(MIN_INTEGER_STR) <= 0
                }
            }
        }else{
            false
        }
    }

    def isLong = {
        if(string.matches("""(?i)[+-]?(\d+l?)""")){
            var str = string
            var length = string.length
            if(string.endsWith("l") || string.endsWith("L")){
                length -= 1
                str = string.slice(0, length)
            }
            val positive = !str.startsWith("-")
            if(str.startsWith("-") || str.startsWith("-")){
                str = string.slice(1, length)
                length -= 1
            }
            if(length > 19){
                false;
            }else if(length < 19){
                true
            }else{
                if(positive){
                    str.compare(MAX_LONG_STR) <= 0
                }else{
                    str.compare(MIN_LONG_STR) <= 0
                }
            }
        }else{
            false
        }
    }

    def isDouble = isNumberic
}

object NumbericString {
    def apply(string: String) = new NumbericString(string)

    implicit def str2Numberic(string: String) = NumbericString(string)
}
