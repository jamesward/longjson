package models

case class UserProfile(name: String, email: String, role: String) {
  def getName = name
  def getEmail = email
  def getRole = role
}