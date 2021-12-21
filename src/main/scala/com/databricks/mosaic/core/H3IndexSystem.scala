package com.databricks.mosaic.core
import com.databricks.mosaic.types.model
import com.databricks.mosaic.types.model.MosaicChip
import com.databricks.mosaic.utils.GeoCoordsUtils
import com.uber.h3core.H3Core
import org.locationtech.jts.geom.{Coordinate, Geometry, GeometryFactory}

import java.{lang, util}
import scala.collection.JavaConverters._


/**
 * Implements the [[IndexSystem]] via [[H3Core]] java bindings.
 * @see [[https://github.com/uber/h3-java]]
 */
object H3IndexSystem extends IndexSystem {

  //An instance of H3Core to be used for IndexSystem implementation.
  val h3: H3Core = H3Core.newInstance()

  /**
   * H3 resolution can only be an Int value between 0 and 15.
   * @see [[IndexSystem.getResolution()]] docs.
   * @param res Any type input to be parsed into the Int representation of resolution.
   * @return Int value representing the resolution.
   */
  override def getResolution(res: Any): Int = {
    val resolution: Int = res.asInstanceOf[Int]
    if (resolution < 0 | resolution > 15)
      throw new IllegalStateException(s"H3 resolution has to be between 0 and 15; found $resolution")
    resolution
  }


  /**
   * A radius of minimal enclosing circle is always smaller than the largest side of
   * the skewed hexagon. Since H3 is generating hexagons that take into account
   * curvature of the spherical envelope a radius may be different at different localities
   * due to the skew. To address this problem a centroid hexagon is selected from
   * the geometry and the optimal radius is computed based on this hexagon.
   * @param geometry An instance of [[Geometry]] for which we are computing the optimal
   *                 buffer radius.
   * @param resolution  A resolution to be used to get the centroid index geometry.
   * @return An optimal radius to buffer the geometry in order to avoid blind spots
   *         when performing polyfill.
   */
  override def getBufferRadius(geometry: Geometry, resolution: Int): Double = {
    val centroid = geometry.getCentroid
    val centroidIndex = h3.geoToH3(centroid.getY, centroid.getX, resolution)
    val indexGeom = index2geometry(centroidIndex)

    val boundary = indexGeom.getCoordinates
    val (radius, _) = boundary.tail.foldLeft((0.0, boundary.head)){
      case ((max, previous), next) =>
        val current = next.distance(previous)
        if(current > max) {
          (current, next)
        } else {
          (max, next)
        }
    }
    radius
  }

  /**
   * H3 polyfill logic is based on the centroid point of the individual
   * index geometry. Blind spots do occur near the boundary of the geometry.
   * @param geometry Input geometry to be represented.
   * @param resolution A resolution of the indices.
   * @return A set of indices representing the input geometry.
   */
  override def polyfill(geometry: Geometry, resolution: Int): util.List[java.lang.Long] = {
    val boundary = GeoCoordsUtils.getBoundary(geometry)
    val holes = GeoCoordsUtils.getHoles(geometry)

    val indices = h3.polyfill(boundary, holes.asJava, resolution)
    indices
  }

  /**
   * @see [[IndexSystem.getBorderChips()]]
   * @param geometry Input geometry whose border is being represented.
   * @param borderIndices Indices corresponding to the border area of the
   *                      input geometry.
   *  @return A border area representation via [[MosaicChip]] set.
   */
  override def getBorderChips(geometry: Geometry, borderIndices: util.List[java.lang.Long]): Seq[MosaicChip] = {
    val intersections = for (index <- borderIndices.asScala) yield {
      val indexGeom = index2geometry(index)
      val chip = model.MosaicChip(isCore = false, index, indexGeom)
      chip.intersection(geometry)
    }
    intersections.filterNot(_.isEmpty)
  }

  /**
   * @see [[IndexSystem.getCoreChips()]]
   * @param coreIndices Indices corresponding to the core area of the
   *                    input geometry.
   *  @return A core area representation via [[MosaicChip]] set.
   */
  override def getCoreChips(coreIndices: util.List[lang.Long]): Seq[MosaicChip] = {
    coreIndices.asScala.map(MosaicChip(true,_,null))
  }

  /**
   * Boundary that is returned by H3 isn't valid from JTS perspective since
   * it does not form a LinearRing (ie first point == last point).
   * The first point of the boundary is appended to the end of the
   * boundary to form a LinearRing.
   * @param index Id of the index whose geometry should be returned.
   * @return An instance of [[Geometry]] corresponding to index.
   */
  override def index2geometry(index: Long): Geometry = {
    val boundary = h3.h3ToGeoBoundary(index).asScala.map(cur => new Coordinate(cur.lng, cur.lat)).toList
    val extended = boundary ++ List(boundary.head)
    val geometryFactory = new GeometryFactory
    val geom = geometryFactory.createPolygon(extended.toArray)
    geom
  }
}
