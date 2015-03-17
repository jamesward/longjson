package models

case class CartItem(name: String, price: Double, quantity: Int) {
  def getName = name
  def getPrice = price
  def getQuantity = quantity
}