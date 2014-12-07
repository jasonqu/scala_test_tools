

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

### Defining base classes for your project

好的实践方式，为每一种类型的测试[比如DbSpec、ActorSysSpec甚至DbActorSysSpec等]提供一个基类，便于代码的简洁清晰：

    package com.mycompany.myproject

    import org.scalatest._

    abstract class UnitSpec extends FlatSpec with Matchers with
      OptionValues with Inside with Inspectors

    // 这里是一个使用UnitSpec的例子
    package com.mycompany.myproject

    import org.scalatest._

    class MySpec extends UnitSpec {
      // Your tests here
    }

### Writing your first test

入门示例，注意`it`指代的是前面用过的`"A Stack"`：

    import collection.mutable.Stack
    import org.scalatest._

    class StackSpec extends FlatSpec {

      "A Stack" should "pop values in last-in-first-out order" in {
        val stack = new Stack[Int]
        stack.push(1)
        stack.push(2)
        assert(stack.pop() === 2)
        assert(stack.pop() === 1)
      }

      it should "throw NoSuchElementException if an empty stack is popped" in {
        val emptyStack = new Stack[String]
        intercept[NoSuchElementException] {
          emptyStack.pop()
        }
      }
    }

### Using assertions

This Assertions trait also mixes in the TripleEquals, which gives you a === operator that allows you to customize Equality and perform equality checks with a numeric Tolerance. You can also use === to enforce type constraints at compile time with sibling traits TypeCheckedTripleEquals and ConversionCheckedTripleEquals.

    // 简单assert
    assert(left == right)

    // 表达式
    assertResult(2) {
      a - b
    }

    // 异常
    val s = "hi"
    intercept[IndexOutOfBoundsException] {
      s.charAt(-1)
    }

    // 假设 抛出TestCanceledException
    assume(database.isAvailable, "The database was down again")
    assume(database.getAllUsers.count == 9)

    // 提示
    assert(1 + 1 === 3, "this is a clue")
    assertResult(3, "this is a clue") { 1 + 1 }
    withClue("this is a clue") {
      intercept[IndexOutOfBoundsException] {
        "hi".charAt(-1)
      }
    }

注：intercept会返回抓到的异常，如果需要查看其具体信息的话可以利用：
intercept returns the caught exception so that you can inspect it further if you wish, for example, to ensure that data contained inside the exception has the expected values.

### Tagging your tests

#### ignore

      ignore should "throw NoSuchElementException if an empty stack is popped" in {
        ...
      }

#### Defining and using your own tags

    import org.scalatest.Tag

    object SlowTest extends Tag("com.mycompany.tags.SlowTest")
    object DbTest extends Tag("com.mycompany.tags.DbTest")

    import org.scalatest.FlatSpec

    class ExampleSpec extends FlatSpec {

      "The Scala language" must "add correctly" taggedAs(SlowTest) in {
          val sum = 1 + 1
          assert(sum === 2)
        }

      it must "subtract correctly" taggedAs(SlowTest, DbTest) in {
        val diff = 4 - 1
        assert(diff === 3)
      }
    }

然后可以指定tag进行测试[不过我们需要测试都通过的，不是吗？]

### Sharing fixtures

共享夹具是为了较少重复代码，消除坏味道

推荐方式：

* Refactor using Scala
* Override withFixture
* Mix in a before-and-after trait

下面分别介绍：
#### Calling get-fixture methods

在多个测试共用相同的可变夹具时可以用这种方式，对不需要关注清理工作的夹具，可以使用这种方式：

    import org.scalatest.FlatSpec
    import collection.mutable.ListBuffer

    class ExampleSpec extends FlatSpec {

      def fixture =
        new {
          val builder = new StringBuilder("ScalaTest is ")
          val buffer = new ListBuffer[String]
        }

      "Testing" should "be easy" in {
        val f = fixture
        f.builder.append("easy!")
        assert(f.builder.toString === "ScalaTest is easy!")
        assert(f.buffer.isEmpty)
        f.buffer += "sweet"
      }

      it should "be fun" in {
        val f = fixture
        f.builder.append("fun!")
        assert(f.builder.toString === "ScalaTest is fun!")
        assert(f.buffer.isEmpty)
      }
    }

#### Instantiating fixture-context objects

对更加复杂的带有上下文信息的夹具，可以使用trait的特性来组合；和上面的方式类似，适用于不需要关注清理工作的夹具：

    import collection.mutable.ListBuffer
    import org.scalatest.FlatSpec

    class ExampleSpec extends FlatSpec {

      trait Builder {
        val builder = new StringBuilder("ScalaTest is ")
      }

      trait Buffer {
        val buffer = ListBuffer("ScalaTest", "is")
      }

      // This test needs the StringBuilder fixture
      "Testing" should "be productive" in new Builder {
        builder.append("productive!")
        assert(builder.toString === "ScalaTest is productive!")
      }

      // This test needs the ListBuffer[String] fixture
      "Test code" should "be readable" in new Buffer {
        buffer += ("readable!")
        assert(buffer === List("ScalaTest", "is", "readable!"))
      }

      // This test needs both the StringBuilder and ListBuffer
      it should "be clear and concise" in new Builder with Buffer {
        builder.append("clear!")
        buffer += ("concise!")
        assert(builder.toString === "ScalaTest is clear!")
        assert(buffer === List("ScalaTest", "is", "concise!"))
      }
    }

#### Overriding withFixture(NoArgTest)

可以我需要进行清理工作呢？可以重载Suite trait中的`withFixture(NoArgTest)`方法。在Suite中，方法定义很简单：

    // Default implementation in trait Suite
    protected def withFixture(test: NoArgTest) = {
      test()
    }

可以这样重写

    // Your implementation
    override def withFixture(test: NoArgTest) = {
      // Perform setup
      try super.withFixture(test) // Invoke the test function
      finally {
        // Perform cleanup
      }
    }

如果有异常，withFixture返回的是Failed对象，所以不用try-finally也行，不过这个算是一个比较好的实践。

这里有一个例子：

    import java.io.File
    import org.scalatest._

    class ExampleSpec extends FlatSpec {

      override def withFixture(test: NoArgTest) = {
        super.withFixture(test) match {
          case failed: Failed =>
            val currDir = new File(".")
            val fileNames = currDir.list()
            info("Dir snapshot: " + fileNames.mkString(", "))
            failed
          case other => other
        }
      }

      "This test" should "succeed" in {
        assert(1 + 1 === 2)
      }

      it should "fail" in {
        assert(1 + 1 === 3)
      }
    }

#### Calling loan-fixture methods

将借贷模式引入测试：

    import java.util.concurrent.ConcurrentHashMap

    object DbServer { // Simulating a database server
      type Db = StringBuffer
      private val databases = new ConcurrentHashMap[String, Db]
      def createDb(name: String): Db = {
        val db = new StringBuffer
        databases.put(name, db)
        db
      }
      def removeDb(name: String) {
        databases.remove(name)
      }
    }

    import org.scalatest.FlatSpec
    import DbServer._
    import java.util.UUID.randomUUID
    import java.io._

    class ExampleSpec extends FlatSpec {

      def withDatabase(testCode: Db => Any) {
        val dbName = randomUUID.toString
        val db = createDb(dbName) // create the fixture
        try {
          db.append("ScalaTest is ") // perform setup
          testCode(db) // "loan" the fixture to the test
        }
        finally removeDb(dbName) // clean up the fixture
      }

      def withFile(testCode: (File, FileWriter) => Any) {
        val file = File.createTempFile("hello", "world") // create the fixture
        val writer = new FileWriter(file)
        try {
          writer.write("ScalaTest is ") // set up the fixture
          testCode(file, writer) // "loan" the fixture to the test
        }
        finally writer.close() // clean up the fixture
      }

      // This test needs the file fixture
      "Testing" should "be productive" in withFile { (file, writer) =>
        writer.write("productive!")
        writer.flush()
        assert(file.length === 24)
      }

      // This test needs the database fixture
      "Test code" should "be readable" in withDatabase { db =>
        db.append("readable!")
        assert(db.toString === "ScalaTest is readable!")
      }

      // This test needs both the file and the database
      it should "be clear and concise" in withDatabase { db =>
        withFile { (file, writer) => // loan-fixture methods compose
          db.append("clear!")
          writer.write("concise!")
          writer.flush()
          assert(db.toString === "ScalaTest is clear!")
          assert(file.length === 21)
        }
      }
    }

#### Overriding withFixture(OneArgTest)

如果大部分[所有的]测试都共用一个夹具，则可以利用函数式特性，使用重载`withFixture(test: OneArgTest)`方法的方式来减少重复代码：

    import org.scalatest.fixture
    import java.io._

    class ExampleSpec extends fixture.FlatSpec {

      case class FixtureParam(file: File, writer: FileWriter)

      def withFixture(test: OneArgTest) = {
        val file = File.createTempFile("hello", "world") // create the fixture
        val writer = new FileWriter(file)
        val theFixture = FixtureParam(file, writer)

        try {
          writer.write("ScalaTest is ") // set up the fixture
          withFixture(test.toNoArgTest(theFixture)) // "loan" the fixture to the test
        }
        finally writer.close() // clean up the fixture
      }

      "Testing" should "be easy" in { f =>
        f.writer.write("easy!")
        f.writer.flush()
        assert(f.file.length === 18)
      }

      it should "be fun" in { f =>
        f.writer.write("fun!")
        f.writer.flush()
        assert(f.file.length === 17)
      }
    }

#### Mixing in BeforeAndAfter

前面的方式都是在每一次测试开始的时候执行的，即使初始化有异常，测试仍然会进行。如果希望发生异常的时候，就停止执行测试，则需要使用另外的方法：

    package org.scalatest.examples.flatspec.beforeandafter

    import org.scalatest._
    import collection.mutable.ListBuffer

    class ExampleSpec extends FlatSpec with BeforeAndAfter {

      val builder = new StringBuilder
      val buffer = new ListBuffer[String]

      before {
        builder.append("ScalaTest is ")
      }

      after {
        builder.clear()
        buffer.clear()
      }

      "Testing" should "be easy" in {
        builder.append("easy!")
        assert(builder.toString === "ScalaTest is easy!")
        assert(buffer.isEmpty)
        buffer += "sweet"
      }

      it should "be fun" in {
        builder.append("fun!")
        assert(builder.toString === "ScalaTest is fun!")
        assert(buffer.isEmpty)
      }
    }

不过由于上面的测试使用了相同的可变量，所以这样的测试是不能并行的[即This is why ScalaTest's `ParallelTestExecution` trait extends `OneInstancePerTest`. ]


#### Composing fixtures by stacking traits

通过stackable trait pattern来简化大型工程的测试夹具代码

    import org.scalatest._
    import collection.mutable.ListBuffer

    trait Builder extends SuiteMixin { this: Suite =>

      val builder = new StringBuilder

      abstract override def withFixture(test: NoArgTest) = {
        builder.append("ScalaTest is ")
        try super.withFixture(test) // To be stackable, must call super.withFixture
        finally builder.clear()
      }
    }

    trait Buffer extends SuiteMixin { this: Suite =>

      val buffer = new ListBuffer[String]

      abstract override def withFixture(test: NoArgTest) = {
        try super.withFixture(test) // To be stackable, must call super.withFixture
        finally buffer.clear()
      }
    }

    class ExampleSpec extends FlatSpec with Builder with Buffer {

      "Testing" should "be easy" in {
        builder.append("easy!")
        assert(builder.toString === "ScalaTest is easy!")
        assert(buffer.isEmpty)
        buffer += "sweet"
      }

      it should "be fun" in {
        builder.append("fun!")
        assert(builder.toString === "ScalaTest is fun!")
        assert(buffer.isEmpty)
        buffer += "clear"
      }
    }

也可以通过继承 BeforeAndAfterEach and/or BeforeAndAfterAll 来达到相同的效果，其中beforeEach和afterEach相当于JUnit中的setup和teardown

    import org.scalatest._
    import collection.mutable.ListBuffer

    trait Builder extends BeforeAndAfterEach { this: Suite =>

      val builder = new StringBuilder

      override def beforeEach() {
        builder.append("ScalaTest is ")
        super.beforeEach() // To be stackable, must call super.beforeEach
      }

      override def afterEach() {
        try super.afterEach() // To be stackable, must call super.afterEach
        finally builder.clear()
      }
    }

    trait Buffer extends BeforeAndAfterEach { this: Suite =>

      val buffer = new ListBuffer[String]

      override def afterEach() {
        try super.afterEach() // To be stackable, must call super.afterEach
        finally buffer.clear()
      }
    }

    class ExampleSpec extends FlatSpec with Builder with Buffer {

      "Testing" should "be easy" in {
        builder.append("easy!")
        assert(builder.toString === "ScalaTest is easy!")
        assert(buffer.isEmpty)
        buffer += "sweet"
      }

      it should "be fun" in {
        builder.append("fun!")
        assert(builder.toString === "ScalaTest is fun!")
        assert(buffer.isEmpty)
        buffer += "clear"
      }
    }

为了是测试夹具的trait具有相同的顺序，从而达到可预见的stackable，应该遵循：将super.beforeEach()放在beforeEach的最后，将super.afterEach()放在afterEach()的最前面。

The difference between stacking traits that extend BeforeAndAfterEach versus traits that implement withFixture is that setup and cleanup code happens before and after the test in BeforeAndAfterEach, but at the beginning and end of the test in withFixture. Thus if a withFixture method completes abruptly with an exception, it is considered a failed test. By contrast, if any of the beforeEach or afterEach methods of BeforeAndAfterEach complete abruptly, it is considered an aborted suite, which will result in a SuiteAborted event.


### Sharing tests

在不同的夹具中共享相同的测试，还是很不错的，可以见[代码](http://www.scalatest.org/user_guide/sharing_tests)

有点类似于spec，不知道spec2会不会做的好一些


### Using matchers

ScalaTest的should测试DSL，比如之前的assert可以改写为：`result should equal (3)`

* Checking equality with matchers
* Checking size and length
* Checking strings
* Greater and less than
* Checking Boolean properties with be
* Using custom BeMatchers
* Checking object identity
* Checking an object's class
* Checking numbers against a range
* Checking for emptiness
* Working with "containers"
* Working with "aggregations"
* Working with "sequences"
* Working with iterators
* Inspector shorthands
* Single-element collections
* Java collections and maps
* Be as an equality comparison
* Being negative
* Logical expressions with and and or
* Working with Options
* Checking arbitrary properties with have
* Using length and size with HavePropertyMatchers
* Using custom matchers
* Creating dynamic matchers
* Creating matchers using logical operators
* Composing matchers
* Checking for expected exceptions
* Those pesky parens

具体的可以参阅文档

### Testing with mock objects

#### Using ScalaMock

ScalaMock supports three different mocking styles:

* Function mocks
* Proxy (dynamic) mocks
* Generated (type-safe) mocks

#### Using Mockito

ScalaTest's MockitoSugar trait provides some basic syntax sugar for Mockito.

### Property-based testing

todo

### Using Selenium

todo

### Other goodies

todo

### Philosophy and design

todo



# Spec2

http://etorreborre.github.io/specs2/
http://etorreborre.github.io/specs2/guide/org.specs2.guide.QuickStart.html

# ScalaCheck

http://www.scalacheck.org/












