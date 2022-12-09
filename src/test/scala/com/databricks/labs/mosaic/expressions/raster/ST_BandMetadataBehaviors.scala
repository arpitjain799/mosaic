package com.databricks.labs.mosaic.expressions.raster

import com.databricks.labs.mosaic.core.geometry.api.GeometryAPI
import com.databricks.labs.mosaic.core.index.IndexSystem
import com.databricks.labs.mosaic.functions.MosaicContext
import com.databricks.labs.mosaic.test.mocks
import org.apache.spark.sql.QueryTest
import org.apache.spark.sql.functions._
import org.scalatest.matchers.should.Matchers._

trait ST_BandMetadataBehaviors extends QueryTest {

    def bandMetadataBehavior(indexSystem: IndexSystem, geometryAPI: GeometryAPI): Unit = {
        val mc = MosaicContext.build(indexSystem, geometryAPI)
        mc.register()
        val sc = spark
        import mc.functions._
        import sc.implicits._

        val rasterDfWithBandMetadata = mocks
            .getNetCDFBinaryDf(spark)
            .withColumn("subdatasets", st_subdatasets($"content"))
            .withColumn("bleachingSubdataset", element_at(map_keys($"subdatasets"), 1))
            .select(
              st_bandmetadata($"content", lit(1), $"bleachingSubdataset")
                  .alias("metadata")
            )

        mocks
            .getNetCDFBinaryDf(spark)
            .withColumn("subdatasets", st_subdatasets($"content"))
            .withColumn("bleachingSubdataset", element_at(map_keys($"subdatasets"), 1))
            .createOrReplaceTempView("source")

        noException should be thrownBy spark.sql("""
                                                   |select st_bandmetadata(content, 1, bleachingSubdataset) from source
                                                   |""".stripMargin)

        val result = rasterDfWithBandMetadata.as[Map[String, String]].collect()

        result.head.getOrElse("bleaching_alert_area_long_name", "") shouldBe "bleaching alert area 7-day maximum composite"
        result.head.getOrElse("bleaching_alert_area_valid_max", "") shouldBe "4 "
        result.head.getOrElse("bleaching_alert_area_valid_min", "") shouldBe "0 "
        result.head.getOrElse("bleaching_alert_area_units", "") shouldBe "stress_level"

    }

}
