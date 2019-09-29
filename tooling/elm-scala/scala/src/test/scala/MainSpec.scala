import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{Matchers, WordSpec}

class MainSpec extends WordSpec with Matchers with TypeCheckedTripleEquals {
	"tests" should {
		"work" in {
			55 should === (55)
		}
	}
}