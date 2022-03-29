package com.databricks.labs.mosaic.core.geometry.multilinestring

import com.databricks.labs.mosaic.core.geometry.linestring.MosaicLineStringESRI
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

import org.apache.spark.sql.catalyst.InternalRow

class TestMultiLineStringESRI extends AnyFlatSpec {

    "MosaicMultiLineStringESRI" should "return Nil for holes and hole points calls." in {
        val multiLineString = MosaicMultiLineStringESRI.fromWKT("MULTILINESTRING ((1 1, 2 2, 3 3), (2 2, 3 3, 4 4))")
        multiLineString.getHoles shouldEqual Nil
        multiLineString.getHolePoints shouldEqual Nil
    }

    "MosaicMultiLineStringESRI" should "return seq(this) for shells and flatten calls." in {
        val multiLineString = MosaicMultiLineStringESRI.fromWKT("MULTILINESTRING ((1 1, 2 2, 3 3), (2 2, 3 3, 4 4))")
        val lineString = MosaicLineStringESRI.fromWKT("LINESTRING(1 1, 2 2, 3 3)")
        multiLineString.getShells.head.equals(lineString) shouldBe true
        multiLineString.flatten.head.equals(lineString) shouldBe true
    }

    "MosaicMultiLineStringESRI" should "return number of points." in {
        val multiLineString = MosaicMultiLineStringESRI.fromWKT("MULTILINESTRING ((1 1, 2 2, 3 3), (2 2, 3 3, 4 4))")
        multiLineString.numPoints shouldEqual 6
    }

    "MosaicMultiLineStringESRI" should "read all supported formats" in {
        val multiLineString = MosaicMultiLineStringESRI.fromWKT("MULTILINESTRING ((1 1, 2 2, 3 3), (2 2, 3 3, 4 4))")
        noException should be thrownBy MosaicMultiLineStringESRI.fromWKB(multiLineString.toWKB)
        noException should be thrownBy MosaicMultiLineStringESRI.fromHEX(multiLineString.toHEX)
        noException should be thrownBy MosaicMultiLineStringESRI.fromJSON(multiLineString.toJSON)
        noException should be thrownBy MosaicMultiLineStringESRI.fromInternal(multiLineString.toInternal.serialize.asInstanceOf[InternalRow])
        multiLineString.equals(MosaicMultiLineStringESRI.fromWKB(multiLineString.toWKB)) shouldBe true
        multiLineString.equals(MosaicMultiLineStringESRI.fromHEX(multiLineString.toHEX)) shouldBe true
        multiLineString.equals(MosaicMultiLineStringESRI.fromJSON(multiLineString.toJSON)) shouldBe true
        multiLineString.equals(MosaicMultiLineStringESRI.fromInternal(multiLineString.toInternal.serialize.asInstanceOf[InternalRow])) shouldBe true
    }

}
