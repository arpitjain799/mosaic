package com.databricks.labs.mosaic.expressions.index

import com.databricks.labs.mosaic.core.geometry.api.GeometryAPI.{ESRI, JTS}
import com.databricks.labs.mosaic.core.raster.api.RasterAPI.GDAL
import com.databricks.labs.mosaic.core.index.{BNGIndexSystem, H3IndexSystem}
import com.databricks.labs.mosaic.functions.MosaicContext
import com.databricks.labs.mosaic.test.SparkSuite
import org.scalatest.flatspec.AnyFlatSpec

class TestMosaicFill extends AnyFlatSpec with MosaicFillBehaviors with SparkSuite {

    "MosaicFill" should "fill wkt geometries for any index system and any geometry API" in {
        it should behave like wktMosaicFill(MosaicContext.build(H3IndexSystem, ESRI, GDAL), spark, 11)
        it should behave like wktMosaicFill(MosaicContext.build(H3IndexSystem, JTS, GDAL), spark, 11)
        it should behave like wktMosaicFill(MosaicContext.build(BNGIndexSystem, ESRI, GDAL), spark, 4)
        it should behave like wktMosaicFill(MosaicContext.build(BNGIndexSystem, JTS, GDAL), spark, 4)
    }

    "MosaicFill" should "fill wkb geometries for any index system and any geometry API" in {
        it should behave like wkbMosaicFill(MosaicContext.build(H3IndexSystem, ESRI, GDAL), spark, 11)
        it should behave like wkbMosaicFill(MosaicContext.build(H3IndexSystem, JTS, GDAL), spark, 11)
        it should behave like wkbMosaicFill(MosaicContext.build(BNGIndexSystem, ESRI, GDAL), spark, 4)
        it should behave like wkbMosaicFill(MosaicContext.build(BNGIndexSystem, JTS, GDAL), spark, 4)
    }

    "MosaicFill" should "fill hex geometries for any index system and any geometry API" in {
        it should behave like hexMosaicFill(MosaicContext.build(H3IndexSystem, ESRI, GDAL), spark, 11)
        it should behave like hexMosaicFill(MosaicContext.build(H3IndexSystem, JTS, GDAL), spark, 11)
        it should behave like hexMosaicFill(MosaicContext.build(BNGIndexSystem, ESRI, GDAL), spark, 4)
        it should behave like hexMosaicFill(MosaicContext.build(BNGIndexSystem, JTS, GDAL), spark, 4)
    }

    "MosaicFill" should "fill coords geometries for any index system and any geometry API" in {
        it should behave like coordsMosaicFill(MosaicContext.build(H3IndexSystem, ESRI, GDAL), spark, 11)
        it should behave like coordsMosaicFill(MosaicContext.build(H3IndexSystem, JTS, GDAL), spark, 11)
        it should behave like coordsMosaicFill(MosaicContext.build(BNGIndexSystem, ESRI, GDAL), spark, 4)
        it should behave like coordsMosaicFill(MosaicContext.build(BNGIndexSystem, JTS, GDAL), spark, 4)
    }

    "MosaicFill" should "fill wkt geometries with keepCoreGeom parameter" in {
        it should behave like wktMosaicFillKeepCoreGeom(MosaicContext.build(H3IndexSystem, ESRI, GDAL), spark, 11)
        it should behave like wktMosaicFillKeepCoreGeom(MosaicContext.build(H3IndexSystem, JTS, GDAL), spark, 11)
        it should behave like wktMosaicFillKeepCoreGeom(MosaicContext.build(BNGIndexSystem, ESRI, GDAL), spark, 4)
        it should behave like wktMosaicFillKeepCoreGeom(MosaicContext.build(BNGIndexSystem, JTS, GDAL), spark, 4)
    }

}
