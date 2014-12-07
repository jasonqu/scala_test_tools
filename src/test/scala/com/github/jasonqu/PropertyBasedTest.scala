package com.github.jasonqu

import org.scalatest.{Matchers, FlatSpec}
import org.scalatest.prop.PropertyChecks

class PropertyBasedTest {

}

class Fraction(n: Int, d: Int) {

  require(d != 0)
  require(d != Integer.MIN_VALUE)
  require(n != Integer.MIN_VALUE)

  val numer = if (d < 0) -1 * n else n
  val denom = d.abs

  override def toString = numer + " / " + denom
}

class PropertySpec1 extends FlatSpec with PropertyChecks with Matchers{
  forAll { (n: Int, d: Int) =>

    whenever (d != 0 && d != Integer.MIN_VALUE
      && n != Integer.MIN_VALUE) {

      val f = new Fraction(n, d)

      if (n < 0 && d < 0 || n > 0 && d > 0)
        f.numer should be > 0
      else if (n != 0)
        f.numer should be < 0
      else
        f.numer should be === 0

      f.denom should be > 0
    }
  }

  val invalidCombos =
    Table(
      ("n",               "d"),
      (Integer.MIN_VALUE, Integer.MIN_VALUE),
      (1,                 Integer.MIN_VALUE),
      (Integer.MIN_VALUE, 1),
      (Integer.MIN_VALUE, 0),
      (1,                 0)
    )

  forAll (invalidCombos) { (n: Int, d: Int) =>
    evaluating {
      new Fraction(n, d)
    } should produce [IllegalArgumentException]
  }
}
