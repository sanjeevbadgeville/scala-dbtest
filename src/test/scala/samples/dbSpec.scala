package samples

import scalikejdbc._
import org.joda.time.DateTime
import org.scalatest.FlatSpec
import org.scalatest.BeforeAndAfter
import scalikejdbc.config._
import org.scalatest.{ConfigMap, fixture}
import org.scalatest.fixture.ConfigMapFixture

class dbSpec extends fixture.FlatSpec with fixture.ConfigMapFixture with BeforeAndAfter {

  before {
    DBs.loadGlobalSettings()
    DBs.setup('oracle)
    DBs.setup('vertica)
  }

  after {
    // wipes out ConnectionPool
    DBs.close('oracle)
    DBs.close('vertica)
  }

  behavior of "An empty Set"

  it should "have size 0" in { configMap =>
    assert(Set.empty.size === 0)
  }

  it should "create a new record" in { configMap =>
    println(s"Config: ${configMap}")


    assert(countOracle.get > 0L)
    assert(countVertica.get > 0L)
  }

  "data in oracle and vertica" should "match" in { configMap =>
    println(s"Config: ${configMap}")
  }
}
