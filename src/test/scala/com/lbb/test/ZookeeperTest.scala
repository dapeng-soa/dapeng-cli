package com.lbb.test

import com.github.dapeng.utils.ZookeeperUtils
import collection.JavaConverters._

object ZookeeperTest {

  val result = Set[String]()

  def main(args: Array[String]): Unit = {
    ZookeeperUtils.connect()

    val paths = getAllNodes("/", result)
    println(" paths size: " + paths.size)
    val r = paths.toList.sortWith((a, b) => a.length < b.length)

    r.foreach(println(_))
  }

  private def getAllNodes(path: String, result: Set[String]): Set[String] = {
    val childs = ZookeeperUtils.getChildren(path)
    if (childs == null || childs.size() <= 0) {
      result
    } else {
      childs.asScala.flatMap(i => {
        val p = if (path != "/") s"$path/$i" else s"$path$i"
        getAllNodes(p, result ++ Set(p))
      }).toSet
    }
  }

}
