package services.ws

import com.sandinh.soap.SoapWS11
import com.sandinh.xml.{Xml, XmlReader, XmlWriter}
import com.sandinh.soap.DefaultImplicits._
import models.UserProfile

import scala.concurrent.Future
import scala.xml.NodeSeq

object UserProfileWS {

  case class Param()

  implicit object ParamXmlW extends XmlWriter[Param] {
    def write(t: Param, base: NodeSeq): NodeSeq = {
      <get xmlns="http://userprofile.services/"/>
    }
  }

  implicit object ResultXmlR extends XmlReader[UserProfile] {
    def read(x: NodeSeq): Option[UserProfile] = {
      for {
        r <- (x \ "getResponse" \ "return").headOption
        name <- Xml.fromXml[String](r \ "name")
        email <- Xml.fromXml[String](r \ "email")
        role <- Xml.fromXml[String](r \ "role")
      } yield UserProfile(name, email, role)
    }

  }

  object WS11 extends SoapWS11[Param, UserProfile]("https://longsoap.herokuapp.com/services/userprofile", "")

  def get(): Future[UserProfile] = {
    WS11.call(Param())
  }

}
