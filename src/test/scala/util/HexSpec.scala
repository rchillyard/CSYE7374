package util

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class HexSpec extends AnyFlatSpec with should.Matchers {

    behavior of "hexStringToBytes and bytesToHexString"

    it should "hexStringToBytes/bytesToHexString" in {
        val hex = "391F322E2DE3EAF7C9029DB2BB873C3B1E60FD1F657B97DB17031B8774A21EE45E2740DC65246C0FA712290AE8255406BDA708D166029E80F4B31236AC33A6D43A09370196D43191715E9817A9846D66DF7E159BDC641344AE7196AEAD9CC44FF7F8D2A33A3D153D7ADC8DBD3312381896BEAC462EF4DEB4C05F502DE312994EA9D679E3825593291C4CFEBFC653F3121DC3FDA2FCDB80E7C072D7EC95942BFAD9EFD7ACCF51BA38D96A4E3A325C860FFA47C94093751B58C4A9A257931876F2ADEEEFF4C3E4662339D5F3066CB625B9EB0E508AB6C4FD950CA259BCC6EC4283AB9521758B5E7D1CAE4BFE852B76BDD3F390C2C6CF65BB15FDB5B91CEB5F6D6AF4FCC8318AF911BDA27E5419225D3B5274D4AF5C75D0E1089F94"
        val bytes = Hex.hexStringToBytes(hex)
        val result: String = Hex.bytesToHexString(bytes)
        result shouldBe hex
    }

}