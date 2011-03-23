package com.twitter.cassie.types.tests

import org.scalatest.Spec
import org.scalatest.matchers.MustMatchers
import com.twitter.cassie.types.VarInt
import com.twitter.cassie.codecs.VarIntCodec

class VarIntTest extends Spec with MustMatchers {
  describe("a VarInt") {
    it("can be implicitly converted to an Int") {
      val i: Int = VarInt(12311)

      i must equal(12311)
    }
  }

  describe("an Int") {
    it("can be implicitly converted to a VarInt") {
      val i: VarInt = 12311

      i.value must equal(12311)
    }
  }

  describe("encoding a large int") {
    it("should not throw an exception") {
      val b = VarIntCodec.encode(VarInt(java.lang.Integer.MAX_VALUE))
      VarIntCodec.decode(b).value must equal(java.lang.Integer.MAX_VALUE)
    }
  }

  describe("encoding a min int") {
    it("should not throw an exception") {
      val b = VarIntCodec.encode(VarInt(java.lang.Integer.MIN_VALUE))
      VarIntCodec.decode(b).value must equal(java.lang.Integer.MIN_VALUE)
    }
  }

  describe("encoding 0") {
    it("should work") {
      val b = VarIntCodec.encode(VarInt(0))
      VarIntCodec.decode(b).value must equal(0)
    }
  }
}
