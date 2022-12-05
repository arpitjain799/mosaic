package com.databricks.labs.mosaic.expressions.raster

import com.databricks.labs.mosaic.core.raster.MosaicRaster
import com.databricks.labs.mosaic.expressions.base.{RasterExpression, WithExpressionInfo}
import org.apache.spark.sql.catalyst.analysis.FunctionRegistry.FunctionBuilder
import org.apache.spark.sql.catalyst.expressions.{Expression, Literal, NullIntolerant}
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenFallback
import org.apache.spark.sql.types._

case class ST_Subdatasets(inputRaster: Expression)
    extends RasterExpression[ST_Subdatasets](inputRaster, Literal(""), MapType(keyType = StringType, valueType = StringType))
      with NullIntolerant
      with CodegenFallback {

    override def rasterTransform(raster: MosaicRaster): Any = {
        val subdatasets = raster.subdatasets
        buildMap(subdatasets)
    }

}

object ST_Subdatasets extends WithExpressionInfo {

    override def name: String = "st_subdatasets"

    override def usage: String = "_FUNC_(expr1) - Extracts subdataset paths and descriptions from a raster dataset."

    override def example: String =
        """
          |    Examples:
          |      > SELECT _FUNC_(a);
          |        {"NETCDF:"ct5km_baa-max-7d_v3.1_20220101.nc":bleaching_alert_area":"[1x3600x7200] N/A (8-bit unsigned integer)",
          |        "NETCDF:"ct5km_baa-max-7d_v3.1_20220101.nc":mask":"[1x3600x7200] mask (8-bit unsigned integer)"}
          |  """.stripMargin

    override def builder: FunctionBuilder =
        (children: Seq[Expression]) => {
            if (children.length != 1) {
                throw new IllegalArgumentException(s"$name function requires 1 argument")
            }
            ST_Subdatasets(children(0))
        }

}
