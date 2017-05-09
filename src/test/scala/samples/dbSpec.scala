package samples

import org.scalatest.BeforeAndAfter
import org.scalatest.fixture
import org.scalatest.Tag
import org.scalatest.tagobjects.Slow
import org.json4s._
import org.json4s.jackson.JsonMethods._
import com.typesafe.config.ConfigFactory
import scalikejdbc._
import scalikejdbc.config._

object DbTest extends Tag("samples.DbTest")
object DbTestIgnore extends Tag("samples.DbTestIgnore")

case class dbQuery(name: String, source_query: String, sink_query: String, assertion: String)

class dbSpec extends fixture.FlatSpec with fixture.ConfigMapFixture with BeforeAndAfter {

  before {
    DBs.loadGlobalSettings()
    DBs.setup('source)
    DBs.setup('sink)
  }

  after {
    DBs.close('source)
    DBs.close('sink)
  }

  behavior of "Analytics ETL data verification"

  it should "have a tenant" taggedAs (DbTest) in { configMap =>
    val tenantid, tenant_name = configMap.get("tenantid")
    assert(tenant_name != None)
  }

  "data in source and sink for ABC table" should "match" taggedAs(Slow, DbTest) in { configMap =>

    val tenantid, tenant_name = configMap.get("tenantid").get
    val queryString =
      """select count(*) cnt from (
            select *
            rom CS_SalesTransaction txn
            where txn.tenantid = ${tenant_name}
          ) t"""

    val querySourceDB = s"select count(*) cnt from CS_SalesTransaction where tenantid = '${tenant_name}' "
    val countSourceCnt: Option[Long] = NamedDB('source) readOnly { implicit session =>
      SQL(s"${querySourceDB}").map(rs => rs.long("cnt")).single.apply() // SQL is the other version of the sql interpolation
    }
    val countSinkDB: Option[Long] = NamedDB('sink) readOnly { implicit session =>
      sql"select count(*) cnt from tntx_dm.FactSalesTransactionCube where tenantid = ${tenantid} ".map(rs => rs.long("cnt")).single.apply()
    }

    assert(countSourceCnt.get > 0L)
    assert(countSinkDB.get > 0L)
    assert(countSourceCnt.get > countSinkDB.get)
  }

  behavior of "Analytics ETL data verification based on an external config file for queries"

  it should "match on source and sink queries" taggedAs (DbTestIgnore) in { configMap =>

    val tenantid, tenant_name = configMap.get("tenantid").get
    val processingunitseq = configMap.get("processingunitseq").get
    val periodseq = configMap.get("periodseq").get

    assert(tenant_name != None)
    assert(processingunitseq != None)
    assert(periodseq != None)
    // get the queries json file from config
    val value = ConfigFactory.load().getString("queries.location")
    // parse the json file for file with utf-8 encoding
    val lines = scala.io.Source.fromFile(value, "utf-8").getLines.mkString
    val jObj = parse(lines).asInstanceOf[JObject]
    implicit val fmts = DefaultFormats

    val queries = (jObj \ "analytics_etl").extract[List[dbQuery]]
    queries foreach { query =>
      val sourceQuery = s"${query.source_query}".replaceAll("\\$\\{tenant_name\\}", tenant_name.toString).replaceAll("\\$\\{processingunitseq\\}", processingunitseq.toString).replaceAll("\\$\\{periodseq\\}", periodseq.toString)
      val sourceQueryCnt = s"SELECT count(*) cnt FROM ( ${sourceQuery} ) t"
      val sinkQuery = s"${query.sink_query}".replaceAll("\\$\\{tenantid\\}", tenant_name.toString).replaceAll("\\$\\{processingunitseq\\}", processingunitseq.toString).replaceAll("\\$\\{periodseq\\}", periodseq.toString)
      val sinkQueryCnt = s"SELECT count(*) cnt FROM ( ${sinkQuery} ) t"

      val countSource: Option[Long] = NamedDB('source) readOnly { implicit session =>
        SQL(sourceQueryCnt).map(rs => rs.long("cnt")).single.apply()
      }
      val countSink: Option[Long] = NamedDB('sink) readOnly { implicit session =>
        SQL(sinkQueryCnt).map(rs => rs.long("cnt")).single.apply()
      }
      val assertOp = s"${query.assertion}"
      assertOp match {
        case "=" => assert(countSource.getOrElse(0L) == countSink.getOrElse(0L), "Count of source and sink should match")
        case ">=" => assert(countSource.getOrElse(0L) >= countSink.getOrElse(0L), "Count of source should be equal or greater than sink")
        case "<=" => assert(countSource.getOrElse(0L) <= countSink.getOrElse(0L), "Count of source should be equal or less than sink")
        case _ => fail("No Assertion Defined!")
      }

    }
  }


}
