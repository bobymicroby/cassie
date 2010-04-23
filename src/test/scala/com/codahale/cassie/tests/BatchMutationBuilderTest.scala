package com.codahale.cassie.tests

import scalaj.collection.Imports._
import org.scalatest.Spec
import org.scalatest.matchers.MustMatchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito.when
import com.codahale.cassie.codecs.Utf8Codec
import com.codahale.cassie.{Mutations, Column, BatchMutationBuilder, ColumnFamily}
import com.codahale.cassie.clocks.Clock

class BatchMutationBuilderTest extends Spec with MustMatchers with MockitoSugar {
  val cf = mock[ColumnFamily[String, String]]
  when(cf.name).thenReturn("People")
  when(cf.columnCodec).thenReturn(Utf8Codec)
  when(cf.valueCodec).thenReturn(Utf8Codec)

  implicit val clock = new Clock {
    def timestamp = 445
  }

  describe("inserting a column") {
    val builder = new BatchMutationBuilder(cf)
    builder.insert("key", Column("name", "value", 234))
    val mutations = Mutations(builder)

    it("adds an insertion mutation") {
      val mutation = mutations.get("key").get("People").get(0)
      val col = mutation.getColumn_or_supercolumn.getColumn
      new String(col.getName) must equal("name")
      new String(col.getValue) must equal("value")
      col.getTimestamp must equal(234)
    }
  }

  describe("removing a column with an implicit timestamp") {
    val builder = new BatchMutationBuilder(cf)
    builder.remove("key", "column")
    val mutations = Mutations(builder)

    it("adds a deletion mutation") {
      val mutation = mutations.get("key").get("People").get(0)
      val deletion = mutation.getDeletion

      deletion.getTimestamp must equal(445)
      deletion.getPredicate.getColumn_names.asScala.map { new String(_) } must equal(List("column"))
    }
  }

  describe("removing a column with an explicit timestamp") {
    val builder = new BatchMutationBuilder(cf)
    builder.remove("key", "column", 22)
    val mutations = Mutations(builder)

    it("adds a deletion mutation") {
      val mutation = mutations.get("key").get("People").get(0)
      val deletion = mutation.getDeletion

      deletion.getTimestamp must equal(22)
      deletion.getPredicate.getColumn_names.asScala.map { new String(_) } must equal(List("column"))
    }
  }

  describe("removing a set of columns with an implicit timestamp") {
    val builder = new BatchMutationBuilder(cf)
    builder.remove("key", Set("one", "two"))
    val mutations = Mutations(builder)

    it("adds a deletion mutation") {
      val mutation = mutations.get("key").get("People").get(0)
      val deletion = mutation.getDeletion

      deletion.getTimestamp must equal(445)
      deletion.getPredicate.getColumn_names.asScala.map { new String(_) }.sortWith { _ < _ } must equal(List("one", "two"))
    }
  }

  describe("removing a set of columns with an explicit timestamp") {
    val builder = new BatchMutationBuilder(cf)
    builder.remove("key", Set("one", "two"), 22)
    val mutations = Mutations(builder)

    it("adds a deletion mutation") {
      val mutation = mutations.get("key").get("People").get(0)
      val deletion = mutation.getDeletion

      deletion.getTimestamp must equal(22)
      deletion.getPredicate.getColumn_names.asScala.map { new String(_) }.sortWith { _ < _ } must equal(List("one", "two"))
    }
  }
}
