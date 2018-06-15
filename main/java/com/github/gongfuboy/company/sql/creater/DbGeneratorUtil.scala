package com.github.gongfuboy.company.sql.creater

import java.io.{File, FileInputStream, PrintWriter}
import java.sql.{Connection, DriverManager, ResultSet}
import java.text.ParseException
import java.util.Properties

import com.github.gongfuboy.company.sql.creater.pojo.DatabaseInfoBean

import scala.collection.mutable
import scala.collection.JavaConversions._


object DbGeneratorUtil {

  val driver = "com.mysql.jdbc.Driver"
  val enumRegx = """(.*\s*),(\s*\d:\s*.*\(\s*[\d|a-zA-Z|_]+\)\s*;?)+""".r
  val singleEnumRegx = """\s*([\d]+):\s*([\u4e00-\u9fa5|\w|-]+)\(([\d|a-zA-Z|_]+)\)""".r

  def generateEntityFile(fileContent: String, targetPath: String, fileName: String) = {
    val file = new File(targetPath + fileName)
    val created = if (!file.getParentFile.exists()) file.getParentFile.mkdirs() else true
    println(s"generating file: ${targetPath}${file.getName}: ${created}")
    val printWriter = new PrintWriter(file,"UTF-8")
    printWriter.println(fileContent)
    printWriter.flush()
    printWriter.close()
  }

  def toDbClassTemplate(scalaDomainName: String, scalaPackage: String, databaseInfoBeans : java.util.List[DatabaseInfoBean]) = {
    val sb = new StringBuilder(256)
    val className = scalaDomainName
    sb.append(s" package ${scalaPackage}\r\n")

    val columns = databaseInfoBeans.toList

    if (columns.exists(c => List("DATETIME", "DATE", "TIMESTAMP").contains(c.typeName))) {
      sb.append(" import java.time.LocalDateTime \r\n")
    }

    sb.append(" import wangzx.scala_commons.sql.ResultSetMapper \r\n\r\n ")

    sb.append(s" case class ${className} ( \r\n")
    columns.foreach(column => {
      sb.append(s" /** ${column.remarks} */ \r\n")
      sb.append(toCamel(keywordConvert(column.columnName))).append(": ").append(toScalaFieldType(column.typeName, column.isNullable)
      ).append(",\r\n")
    })
    if (sb.toString().contains(",")) sb.delete(sb.lastIndexOf(","), sb.lastIndexOf(",") + 1)
    sb.append(") \t\n \t\n")

    sb.append(s" object ${className} { \r\n")
    sb.append(s" \timplicit val resultSetMapper: ResultSetMapper[${className}] = ResultSetMapper.material[${className}] \r\n")
    sb.append(" }")

    sb.toString()
  }


  def keywordConvert(word: String) = {
    if (List("abstract",
      "case",
      "catch",
      "class",
      "def",
      "do",
      "else",
      "extends",
      "false",
      "final",
      "finally",
      "for",
      "forSome",
      "if",
      "implicit",
      "import",
      "lazy",
      "macro",
      "match",
      "new",
      "null",
      "object",
      "override",
      "package",
      "private",
      "protected",
      "return",
      "sealed",
      "super",
      "this",
      "throw",
      "trait",
      "try",
      "true",
      "type",
      "val",
      "var",
      "while",
      "with",
      "yield").exists(_.equals(word))) {
      s"`${word}`"
    } else word
  }

  /**
    * sss_xxx => sssXxx
    *
    * @param name
    * @return
    */
  def toCamel(name: String): String = {
    val camel = name.split("_").map(item => {
      val result = item.toLowerCase
      result.charAt(0).toUpper + result.substring(1)
    }).mkString("")
    camel.replaceFirst(s"${camel.charAt(0)}", s"${camel.charAt(0).toLower}")
  }

  /**
    * aaa_bbb => AaaBbb
    *
    * @param name
    * @return
    */
  def toFirstUpperCamel(name: String): String = {
    name.split("_").map(item => {
      val result = item.toLowerCase
      result.charAt(0).toUpper + result.substring(1)
    }).mkString("")
  }

  def toScalaFieldType(tableFieldType: String, isNullable: String): String = {
    val dataType = tableFieldType.toUpperCase() match {
      case "INT" | "SMALLINT" | "TINYINT" | "INT UNSIGNED" | "SMALLINT UNSIGNED" | "TINYINT UNSIGNED" | "BIT" => "Int"
      case "BIGINT" => "Long"
      case "CHAR" | "VARCHAR" => "String"
      case "DECIMAL" | "DOUBLE" | "FLOAT" => "BigDecimal"
      case "DATETIME" | "DATE" | "TIMESTAMP" => "LocalDateTime"
      case "ENUM" | "TEXT" => "String"
      case "LONGBLOB" | "BLOB" | "MEDIUMBLOB" => "Array[Byte]"
      case _ => throw new ParseException(s"tableFieldType = ${tableFieldType} 无法识别", 1023)
    }
    if (isNullable.equals("YES")) {
      s"Option[${dataType}]"
    } else {
      dataType
    }
  }


  def connectJdbc(): Option[Connection] = {
    val url = System.getProperty("db.url")
    val user = System.getProperty("db.user")
    val passwd = System.getProperty("db.password")
    println(s"connectTo, url: ${url}, user: ${user}, passwd: ${passwd}")

    //val url = s"jdbc:mysql://${ip}/${db}?useUnicode=true&characterEncoding=utf8"
    try {
      if (url == null || url.isEmpty) throw new Exception("please check if 'plugin.db.url' property is config in dapeng.properties ")
      if (user == null || user.isEmpty) throw new Exception("please check if 'plugin.db.user' property is config in dapeng.properties ")
      if (passwd == null || passwd.isEmpty) throw new Exception("please check if 'plugin.db.password' property is config in dapeng.properties ")

      Class.forName(driver)
      Some(DriverManager.getConnection(url, user, passwd))
    } catch {
      case e: Exception =>
        println(s" failed to instance jdbc driver: ${e.getCause} , ${e.getMessage}")
        Option.empty
    }

  }


  def getTableNamesByDb(db: String, connection: Connection) = {
    val tables = connection.getMetaData.getTables("", db, "", null)
    val tableNames = mutable.MutableList[String]()
    while (tables.next()) {
      tableNames += tables.getString("TABLE_NAME")
    }

    tableNames
  }

  def tableNameConvert(tableName: String): String = {
    if (tableName.endsWith("ies")) tableName.substring(0, tableName.length - 3) + "y"
    else if (tableName.endsWith("ses")) tableName.substring(0, tableName.length - 3) + "s"
    else if (tableName.endsWith("shes")) tableName.substring(0, tableName.length - 4) + "sh"
    else if (tableName.endsWith("ches")) tableName.substring(0, tableName.length - 4) + "ch"
    else if (tableName.endsWith("xes")) tableName.substring(0, tableName.length - 3) + "x"
    else if (tableName.endsWith("ves")) tableName.substring(0, tableName.length - 3) + "f?"
    else if (tableName.endsWith("s")) tableName.substring(0, tableName.length - 1)
    else tableName
  }


}
