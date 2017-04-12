package com.purplekingdomgames.indigo.gameengine

sealed trait PowerOfTwo {
  val value: Int
  val halved: PowerOfTwo
  val doubled: PowerOfTwo

  def >(powerOfTwo: PowerOfTwo): Boolean = value > powerOfTwo.value
  def >=(powerOfTwo: PowerOfTwo): Boolean = value >= powerOfTwo.value
  def <(powerOfTwo: PowerOfTwo): Boolean = value < powerOfTwo.value
  def <=(powerOfTwo: PowerOfTwo): Boolean = value <= powerOfTwo.value
  def ===(powerOfTwo: PowerOfTwo): Boolean = value == powerOfTwo.value
}
object PowerOfTwo {
  case object _1 extends PowerOfTwo {
    val value: Int = 1
    val halved: PowerOfTwo = _1
    val doubled: PowerOfTwo = _2
  }
  case object _2 extends PowerOfTwo {
    val value: Int = 2
    val halved: PowerOfTwo = _1
    val doubled: PowerOfTwo = _4
  }
  case object _4 extends PowerOfTwo {
    val value: Int = 4
    val halved: PowerOfTwo = _2
    val doubled: PowerOfTwo = _8
  }
  case object _8 extends PowerOfTwo {
    val value: Int = 8
    val halved: PowerOfTwo = _4
    val doubled: PowerOfTwo = _16
  }
  case object _16 extends PowerOfTwo {
    val value: Int = 16
    val halved: PowerOfTwo = _8
    val doubled: PowerOfTwo = _32
  }
  case object _32 extends PowerOfTwo {
    val value: Int = 32
    val halved: PowerOfTwo = _16
    val doubled: PowerOfTwo = _64
  }
  case object _64 extends PowerOfTwo {
    val value: Int = 64
    val halved: PowerOfTwo = _32
    val doubled: PowerOfTwo = _128
  }
  case object _128 extends PowerOfTwo {
    val value: Int = 128
    val halved: PowerOfTwo = _64
    val doubled: PowerOfTwo = _256
  }
  case object _256 extends PowerOfTwo {
    val value: Int = 256
    val halved: PowerOfTwo = _128
    val doubled: PowerOfTwo = _512
  }
  case object _512 extends PowerOfTwo {
    val value: Int = 512
    val halved: PowerOfTwo = _256
    val doubled: PowerOfTwo = _1024
  }
  case object _1024 extends PowerOfTwo {
    val value: Int = 1024
    val halved: PowerOfTwo = _512
    val doubled: PowerOfTwo = _2048
  }
  case object _2048 extends PowerOfTwo {
    val value: Int = 2048
    val halved: PowerOfTwo = _1024
    val doubled: PowerOfTwo = _4096
  }
  case object _4096 extends PowerOfTwo {
    val value: Int = 4096
    val halved: PowerOfTwo = _2048
    val doubled: PowerOfTwo = _4096
  }

  val Max: PowerOfTwo = _4096

  val all: Set[PowerOfTwo] = Set(_1, _2, _4, _8, _16, _32, _64, _128, _256, _512, _1024, _2048, _4096)

  def isValidPowerOfTwo(i: Int): Boolean = all.exists(p => p.value == i)

  def fromInt(i: Int): Option[PowerOfTwo] = all.find(_.value == i)

  def min(a: PowerOfTwo, b: PowerOfTwo): PowerOfTwo = if(a.value <= b.value) a else b
  def max(a: PowerOfTwo, b: PowerOfTwo): PowerOfTwo = if(a.value > b.value) a else b

}
