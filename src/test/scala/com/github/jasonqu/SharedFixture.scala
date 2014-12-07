package com.github.jasonqu

import org.scalatest.FlatSpec
import collection.mutable.ListBuffer

class ExampleSpec1 extends FlatSpec {

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

import collection.mutable.ListBuffer
import org.scalatest.FlatSpec

class ExampleSpec2 extends FlatSpec {

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

class ExampleSpec3 extends FlatSpec {

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

import org.scalatest.fixture
import java.io._

class ExampleSpec4 extends fixture.FlatSpec {

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


import org.scalatest._
import collection.mutable.ListBuffer

class ExampleSpecBeforeAndAfter extends FlatSpec with BeforeAndAfter {

  val builder = new StringBuilder
  val buffer = new ListBuffer[String]

  before {
    throw new RuntimeException("boom")
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

class ExampleSpecWithStackableTrait extends FlatSpec with Builder with Buffer {

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

object SharingFixture {

}
