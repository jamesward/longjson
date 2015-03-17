package models

import scala.collection.JavaConverters._

case class Cart(items: List[CartItem]) {
  def getItems = items.asJava
}