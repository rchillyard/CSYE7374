/*
 * Copyright (c) 2019. Phasmid Software
 */

package util

import java.io.InputStream
import org.scalatest.flatspec
import org.scalatest.matchers.should
import scala.collection.mutable
import scala.io.Source
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}
import util.FP.*

class FPSpec extends flatspec.AnyFlatSpec with should.Matchers {

    behavior of "FP"

    it should "indexFound" in {
        indexFound("junk", 0) shouldBe Success(0)
        indexFound("junk", -1) should matchPattern { case Failure(FPException("Header column 'junk' not found", None)) => }
    }

    it should "sequence" in {
        val try1 = Success(1)
        val try2 = Success(2)
        val try3 = Failure(FPException(""))
        sequence(Seq(try1, try2)) shouldBe Success(Seq(1, 2))
        val result: Try[Iterable[Int]] = sequence(Seq(try1, try3))
        result should matchPattern { case Failure(_) => }
    }

    it should "sequenceForgivingWith" in {
        val try2 = Success(1)
        val try3 = Success(2)
        val try1 = Failure(FPException(""))
        val sb = new mutable.StringBuilder()
        val handleException: PartialFunction[Throwable, Try[Option[Int]]] = {
            case NonFatal(x) => sb.append(s"forgiving: $x"); Success(None)
            case x => Failure(x)
        }
        val result: Try[Iterable[Int]] = sequenceForgivingWith(Seq(try1, try2, try3))(handleException)
        result should matchPattern { case Success(List(1, 2)) => }
        sb.toString shouldBe "forgiving: util.FPException: "
    }

    it should "sequenceForgivingTransform" in {
        val try2 = Success(1)
        val try3 = Success(2)
        val try1 = Failure(FPException(""))
        val sb = new mutable.StringBuilder()
        val handleException: PartialFunction[Throwable, Try[Option[Int]]] = {
            case NonFatal(x) => sb.append(s"forgiving: $x"); Success(None)
            case x => Failure(x)
        }
        val result: Try[Iterable[Int]] = sequenceForgivingTransform(Seq(try1, try2, try3))(x => Success(Some(x + 1)), handleException)
        result should matchPattern { case Success(List(2, 3)) => }
        sb.toString shouldBe "forgiving: util.FPException: "
    }
    it should "sequenceForgiving 0" in {
        val try2 = Success(1)
        val try3 = Success(2)
        val try1 = Failure(FPException(""))
        val result: Try[Iterable[Int]] = sequenceForgiving(Seq(try1, try2, try3))
        result should matchPattern { case Success(List(1, 2)) => }
    }

    it should "sequenceForgiving 1" in {
        val try1 = Success(1)
        val try2 = Success(2)
        val try3 = Failure(FPException(""))
        val result: Try[Iterable[Int]] = sequenceForgiving(Seq(try1, try2, try3))
        result should matchPattern { case Success(List(1, 2)) => }
    }

    it should "sequenceForgiving 2" in {
        val try1 = Success(1)
        val try2 = Success(2)
        val try3 = Failure(new OutOfMemoryError(""))
        val result: Try[Iterable[Int]] = sequenceForgiving(Seq(try1, try2, try3))
        result should matchPattern { case Failure(_) => }
    }

    it should "partition 1" in {
        val try1 = Success(1)
        val try2 = Success(2)
        val (good, bad) = partition(Seq(try1, try2).iterator)
        good.toSeq shouldBe List(try1, try2)
        bad.isEmpty shouldBe true
    }
    it should "partition 2" in {
        val try1 = Success(1)
        val try3 = Failure(FPException(""))
        val (good, bad) = partition(Seq(try1, try3).iterator)
        good.toSeq shouldBe List(try1)
        bad.toSeq shouldBe List(try3)
    }
    it should "partition 3" in {
        val try1 = Success(1)
        val try3 = Failure(FPException(""))
        val (good, bad) = partition(Seq(try1, try3))
        good shouldBe List(try1)
        bad shouldBe List(try3)
    }
    it should "tryToOption" in {
        val sb = new mutable.StringBuilder

        def logFunction(x: Throwable): Unit = {
            sb.append(x.getLocalizedMessage)
        }

        val f: Try[Int] => Option[Int] = tryToOption(logFunction)(_)
        f(Success(1)) shouldBe Some(1)
        sb.toString() shouldBe ""
        f(Failure(new Exception("failure"))) shouldBe None
        sb.toString() shouldBe "failure"
    }

    behavior of "TryUsing"

    it should "return success" in {
        lazy val i: InputStream = getClass.getResourceAsStream("/crypto/english")
        val zy: Try[String] = TryUsing(Source.fromInputStream(i))(s => Try(s.getLines().toList.head))
        zy should matchPattern { case Success("A") => }
    }

    it should "return failure(0)" in {
        val wy = TryUsing(Source.fromResource(null))(s => Try(s.getLines().toList.head))
        wy should matchPattern { case Failure(_) => }
        wy.recover {
            case _: NullPointerException => Success(())
            case e => fail(s"wrong exception: $e")
        }
    }

    it should "return failure(1)" in {
        lazy val i: InputStream = getClass.getResourceAsStream(null)
        val wy = TryUsing(Source.fromInputStream(i))(s => Try(s.getLines().toList.head))
        wy should matchPattern { case Failure(_) => }
        wy.recover {
            case _: NullPointerException => Success(())
            case e => fail(s"wrong exception: $e")
        }
    }

    it should "return failure(2)" in {
        lazy val i: InputStream = getClass.getResourceAsStream("emptyResource.txt")
        val wy = TryUsing(Source.fromInputStream(i))(s => Try(s.getLines().toList.head))
        wy should matchPattern { case Failure(_) => }
        wy.recover {
            case _: NoSuchElementException => Success(())
            case e => fail(s"wrong exception: $e")
        }
    }

}
