package krecia.maciejnowicki.com.gcode

import com.fasterxml.jackson.databind.ObjectMapper

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

object GCode {

  val objectMapper = new ObjectMapper()

  def decode(gcode: String): String = {
    val lines = gcode.replace("\r", "").split("\n")
    val stringToTrim = ";SETTING_3"
    val res = lines
      .filter(_.startsWith(stringToTrim))
      .map(_.substring(stringToTrim.length + 1))
      .reduceLeft(_ + _)

    val node = objectMapper.readTree(res)

    def parseIni(name: String, string: String): String = {
      val replaced = string.replace("""\n""", "\n")
      s"### $name ###\n$replaced"
    }

    val result = node.fields().asScala.flatMap(node => {
        if (node.getValue.isArray) {
          node.getValue.elements().asScala.zipWithIndex.map {
            case (elem, index) => s"${node.getKey}_$index" -> elem.textValue()
          }
        } else {
          List(node.getKey -> node.getValue.textValue())
        }
      })
      .map(Function.tupled(parseIni))
      .mkString("\n")


    result
  }

  def main(args: Array[String]): Unit = {
    
  }

}
