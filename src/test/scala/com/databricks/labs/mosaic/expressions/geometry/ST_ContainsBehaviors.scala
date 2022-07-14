package com.databricks.labs.mosaic.expressions.geometry

import com.databricks.labs.mosaic.core.geometry.api.GeometryAPI
import com.databricks.labs.mosaic.core.index._
import com.databricks.labs.mosaic.functions.MosaicContext
import org.apache.spark.sql.QueryTest
import org.apache.spark.sql.catalyst.expressions.codegen.{CodegenContext, CodeGenerator}
import org.apache.spark.sql.execution.WholeStageCodegenExec
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.types._
import org.scalatest.matchers.must.Matchers.noException
import org.scalatest.matchers.should.Matchers.{an, be, convertToAnyShouldWrapper}

trait ST_ContainsBehaviors extends QueryTest {

    def containsBehavior(indexSystem: IndexSystem, geometryAPI: GeometryAPI): Unit = {
        spark.sparkContext.setLogLevel("FATAL")
        val mc = MosaicContext.build(indexSystem, geometryAPI)
        import mc.functions._
        val sc = spark
        import sc.implicits._
        mc.register(spark)

        val poly = """POLYGON ((10 10, 110 10, 110 110, 10 110, 10 10),
                     | (20 20, 20 30, 30 30, 30 20, 20 20),
                     | (40 20, 40 30, 50 30, 50 20, 40 20))""".stripMargin.filter(_ >= ' ')

        val rows = List(
          (poly, "POINT (35 25)", true),
          (poly, "POINT (25 25)", false)
        )

        val results = rows
            .toDF("leftGeom", "rightGeom", "expected")
            .withColumn("result", st_contains($"leftGeom", $"rightGeom"))
            .where($"expected" === $"result")

        results.count shouldBe 2
    }

    def containsCodegen(indexSystem: IndexSystem, geometryAPI: GeometryAPI): Unit = {
        spark.sparkContext.setLogLevel("FATAL")
        val mc = MosaicContext.build(indexSystem, geometryAPI)
        val sc = spark
        import mc.functions._
        import sc.implicits._
        mc.register(spark)

        val poly = """POLYGON ((10 10, 110 10, 110 110, 10 110, 10 10),
                     | (20 20, 20 30, 30 30, 30 20, 20 20),
                     | (40 20, 40 30, 50 30, 50 20, 40 20))""".stripMargin.filter(_ >= ' ')

        val rows = List(
          ("POINT (35 25)", true),
          ("POINT (25 25)", false)
        )

        val polygons = List(poly).toDF("leftGeom")
        val points = rows.toDF("rightGeom", "expected")

        val result = polygons
            .crossJoin(points)
            .withColumn("result", st_contains($"leftGeom", $"rightGeom"))
            .where($"expected" === $"result")

        val queryExecution = result.queryExecution
        val plan = queryExecution.executedPlan

        val wholeStageCodegenExec = plan.find(_.isInstanceOf[WholeStageCodegenExec])

        wholeStageCodegenExec.isDefined shouldBe true

        val codeGenStage = wholeStageCodegenExec.get.asInstanceOf[WholeStageCodegenExec]
        val (_, code) = codeGenStage.doCodeGen()

        noException should be thrownBy CodeGenerator.compile(code)

        val stContains = ST_Contains(lit(1).expr, lit(rows.head._1).expr, "JTS")
        val ctx = new CodegenContext
        an[IllegalArgumentException] should be thrownBy stContains.genCode(ctx)
    }

    def auxiliaryMethods(indexSystem: IndexSystem, geometryAPI: GeometryAPI): Unit = {
        spark.sparkContext.setLogLevel("FATAL")
        val mc = MosaicContext.build(indexSystem, geometryAPI)
        mc.register(spark)

        val poly = """POLYGON ((10 10, 110 10, 110 110, 10 110, 10 10),
                     | (20 20, 20 30, 30 30, 30 20, 20 20),
                     | (40 20, 40 30, 50 30, 50 20, 40 20))""".stripMargin.filter(_ >= ' ')

        val rows = List(
          ("POINT (35 25)", true),
          ("POINT (25 25)", false)
        )

        val stContains = ST_Contains(lit(poly).expr, lit(rows.head._1).expr, "illegalAPI")

        stContains.left shouldEqual lit(poly).expr
        stContains.right shouldEqual lit(rows.head._1).expr
        stContains.dataType shouldEqual BooleanType
        noException should be thrownBy stContains.makeCopy(Array(stContains.left, stContains.right))

    }

}
