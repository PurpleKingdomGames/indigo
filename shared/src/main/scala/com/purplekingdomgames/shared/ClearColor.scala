package com.purplekingdomgames.shared

final case class ClearColor(r: Double, g: Double, b: Double, a: Double) {
  def forceOpaque: ClearColor                = this.copy(a = 1d)
  def forceTransparent: ClearColor           = this.copy(a = 0d)
  def alphaAsPercent: Int                    = (a * 100).toInt
  def alphaPercent(percent: Int): ClearColor = this.copy(a = percent.toDouble * 0.01)
  def withR(v: Double): ClearColor           = this.copy(r = v)
  def withG(v: Double): ClearColor           = this.copy(g = v)
  def withB(v: Double): ClearColor           = this.copy(b = v)
  def withA(v: Double): ClearColor           = this.copy(a = v)
}

object ClearColor {
  def apply(r: Double, g: Double, b: Double): ClearColor = ClearColor(r, g, b, 1d)

  val Red: ClearColor   = ClearColor(1, 0, 0)
  val Green: ClearColor = ClearColor(0, 1, 0)
  val Blue: ClearColor  = ClearColor(0, 0, 1)
  val Black: ClearColor = ClearColor(0, 0, 0)
  val White: ClearColor = ClearColor(1, 1, 1)

  def fromHexString(hex: String): ClearColor =
    hex.trim match {
      case h if h.startsWith("0x") && h.length == 8 =>
        fromRGB(
          Integer.parseInt(hex.substring(2, 4), 16),
          Integer.parseInt(hex.substring(4, 6), 16),
          Integer.parseInt(hex.substring(6), 16)
        )

      case h if h.length == 6 =>
        fromRGB(
          Integer.parseInt(hex.substring(0, 2), 16),
          Integer.parseInt(hex.substring(2, 4), 16),
          Integer.parseInt(hex.substring(4), 16)
        )

      case _ =>
        ClearColor.Black
    }

  def fromRGB(r: Int, g: Int, b: Int): ClearColor =
    fromRGBA(r, g, b, 255)

  def fromRGBA(r: Int, g: Int, b: Int, a: Int): ClearColor =
    ClearColor((1.0 / 255) * r, (1.0 / 255) * g, (1.0 / 255) * b, (1.0 / 255) * a)

}
