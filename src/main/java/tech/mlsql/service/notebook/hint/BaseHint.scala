package tech.mlsql.service.notebook.hint

/**
 * 18/1/2021 WilliamZhu(allwefantasy@gmail.com)
 */
trait BaseHint {
  def rewrite(query: String, options: Map[String, String]): String

  protected def _parse(query: String): SQLHeadHint = {
    val headers = query.split("\n").filter(_.stripMargin.startsWith("--%")).map{item=>
       item.stripMargin.stripPrefix("--%")
    }
    val body = query.split("\n").filterNot(_.stripMargin.startsWith("--%")).mkString("\n")
    var t: String = "mlsql"
    var input: Option[String] = None
    var output: Option[String] = None
    val headerParams = scala.collection.mutable.HashMap[String, String]()
    headers.foreach { header =>
      if (!header.contains("=")) {
        t = header
      }else {
        val Array(k, v) = header.split("=", 2)
        k match {
          case "input" =>
            input = Some(v)
          case "output" =>
            output = Some(v)
          case _ =>
            headerParams += (k -> v)
        }
      }
    }
    SQLHeadHint(t,body, input, output, headerParams.toMap)
  }
}

case class SQLHeadHint(t: String,body:String, input: Option[String], output: Option[String], params: Map[String, String])
