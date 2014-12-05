

http://tech.gilt.com/post/62430610230/which-scala-testing-tools-should-you-use

##ScalaTest

### Getting started

    libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"

### Selecting testing styles for your project

we recommend `FlatSpec` for unit and integration testing and `FeatureSpec` for acceptance testing

不同的Spec相当于不同的方言，只是组成测试用例的语法不一样，没有什么大的不同

#### FlatSpec

语法类似xUnit：
`["" | it] should “” in {} +`
优点：格式统一，简单

    import org.scalatest.FlatSpec

    class SetSpec extends FlatSpec {

      "An empty Set" should "have size 0" in {
        assert(Set.empty.size == 0)
      }

      it should "produce NoSuchElementException when head is invoked" in {
        intercept[NoSuchElementException] {
          Set.empty.head
        }
      }
    }

#### FunSpec

语法类似RSpec：
`(recursive) describe("") { it("") {} + }`
优点：describe可嵌套

    import org.scalatest.FunSpec

    class SetSpec extends FunSpec {

      describe("A Set") {
        describe("when empty") {
          it("should have size 0") {
            assert(Set.empty.size == 0)
          }

          it("should produce NoSuchElementException when head is invoked") {
            intercept[NoSuchElementException] {
              Set.empty.head
            }
          }
        }
      }
    }

#### WordSpec

语法类似Spec2：
`"" when { "" should { “” in {} + } + }`
优点：可读性较好[对认真编写spec的团队来说]


    import org.scalatest.WordSpec

    class SetSpec extends WordSpec {

      "A Set" when {
        "empty" should {
          "have size 0" in {
            assert(Set.empty.size == 0)
          }

          "produce NoSuchElementException when head is invoked" in {
            intercept[NoSuchElementException] {
              Set.empty.head
            }
          }
        }
      }
    }

#### PropSpec

语法类似：
`"" property("") { forAll(collection) { } + }`
优点：非常适合编写属性检查测试，并可以使用测试数据集



    import org.scalatest._
    import prop._
    import scala.collection.immutable._

    class SetSpec extends PropSpec with TableDrivenPropertyChecks with Matchers {

      val examples =
        Table(
          "set",
          BitSet.empty,
          HashSet.empty[Int],
          TreeSet.empty[Int]
        )

      property("an empty Set should have size 0") {
        forAll(examples) { set =>
          set.size should be (0)
        }
      }

      property("invoking head on an empty set should produce NoSuchElementException") {
        forAll(examples) { set =>
           a [NoSuchElementException] should be thrownBy { set.head }
        }
      }
    }

#### FeatureSpec

语法类似：
`info() + feature("") { senario("") { Given-When-Then + } + }`
优点：非常适合编写特性检查测试


    import org.scalatest._

    class TVSet {
      private var on: Boolean = false
      def isOn: Boolean = on
      def pressPowerButton() {
        on = !on
      }
    }

    class TVSetSpec extends FeatureSpec with GivenWhenThen {

      info("As a TV set owner")
      info("I want to be able to turn the TV on and off")
      info("So I can watch TV when I want")
      info("And save energy when I'm not watching TV")

      feature("TV power button") {
        scenario("User presses power button when TV is off") {

          Given("a TV set that is switched off")
          val tv = new TVSet
          assert(!tv.isOn)

          When("the power button is pressed")
          tv.pressPowerButton()

          Then("the TV should switch on")
          assert(tv.isOn)
        }

        scenario("User presses power button when TV is on") {

          Given("a TV set that is switched on")
          val tv = new TVSet
          tv.pressPowerButton()
          assert(tv.isOn)

          When("the power button is pressed")
          tv.pressPowerButton()

          Then("the TV should switch off")
          assert(!tv.isOn)
        }
      }
    }
