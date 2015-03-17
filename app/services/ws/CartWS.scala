package services.ws

import com.sandinh.soap.DefaultImplicits._
import com.sandinh.soap.SoapWS11
import com.sandinh.xml.{Xml, XmlReader, XmlWriter}
import models.{CartItem, Cart}

import scala.concurrent.Future
import scala.xml.NodeSeq

object CartWS {

  case class Param()

  implicit object ParamXmlW extends XmlWriter[Param] {
    def write(t: Param, base: NodeSeq): NodeSeq = {
      <get xmlns="http://cart.services/"/>
    }
  }

  implicit object CartItemXmlW extends XmlWriter[CartItem] {
    def write(t: CartItem, base: NodeSeq): NodeSeq = {
      <tns:add xmlns:tns="http://cart.services/">
        <arg0>
          <name>{t.name}</name>
          <price>{t.price}</price>
          <quantity>{t.quantity}</quantity>
        </arg0>
      </tns:add>
    }
  }

  implicit object CartItemXmlR extends XmlReader[CartItem] {
    def read(x: NodeSeq): Option[CartItem] = {
      for {
        name <- Xml.fromXml[String](x \ "name").headOption
        price <- Xml.fromXml[Double](x \ "price").headOption
        quantity <- Xml.fromXml[Int](x \ "quantity").headOption
      } yield CartItem(name, price, quantity)
    }
  }

  implicit object CartXmlR extends XmlReader[Cart] {
    def read(x: NodeSeq): Option[Cart] = {
      for {
        r <- (x \ "getResponse" \ "return").headOption
        items <- Xml.fromXml[List[CartItem]](r \ "items")
      } yield Cart(items)
    }
  }

  implicit object UnitXmlR extends XmlReader[Unit] {
    def read(x: NodeSeq): Option[Unit] = {
      Some(Unit)
    }
  }

  object GetCart extends SoapWS11[Param, Cart]("https://longsoap.herokuapp.com/services/cart", "")
  object AddToCart extends SoapWS11[CartItem, Unit]("https://longsoap.herokuapp.com/services/cart", "")

  def get(): Future[Cart] = {
    GetCart.call(Param())
  }

  def addItem(cartItem: CartItem): Future[Unit] = {
    AddToCart.call(cartItem)
  }

}
