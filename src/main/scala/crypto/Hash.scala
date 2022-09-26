package crypto

import scala.collection.mutable

/**
 * CONSIDER defining Block as a case class. That would allow xor method to be found without import.
 */
type Block = Array[Byte]

type CompressionFunction = (Block, Block) => Block

/**
 * Trait to define the behavior of a class which can hash a BlockMessage into a Block.
 */
trait Hash {
    /**
     * A method to hash the given BlockMessage.
     *
     * @param message a BlockMessage.
     * @return a Block.
     */
    def hash(message: BlockMessage): Block
}

/**
 * Case class which implements Hash.
 *
 * @param cf a CompressionFunction.
 * @param iv the initialization vector.
 */
case class BlockHash(cf: CompressionFunction, iv: Block) extends Hash :
    /**
     * A method to hash the given BlockMessage.
     *
     * @param message a BlockMessage.
     * @return a Block returned by invoking foldLeft(iv)(cf) on the given message.
     */
    def hash(message: BlockMessage): Block = message.foldLeft(iv)(cf)

/**
 * A case class to represent a sequence of Blocks.
 *
 * @param blocks the Blocks making up this BlockMessage.
 */
case class BlockMessage(blocks: Seq[Block]) {
    /**
     * Method to apply foldLeft to the blocks of this BlockMessage.
     *
     * @param z0 the Z value returned for an empty sequence of Blocks.
     * @param f  the applicative function of type (Z, Block) => Z.
     * @tparam Z the type to be returned (and therefore the type of z0).
     * @return a value of Z.
     */
    def foldLeft[Z](z0: => Z)(f: (Z, Block) => Z): Z = blocks.foldLeft(z0)(f)

    override def toString: String = {
        val sb = new mutable.StringBuilder()
        blocks.foreach(b => sb.append(BlockMessage.render(b)))
        sb.toString
    }
}

object BlockMessage {
    /**
     * Method to construct a BlockMessage from a Block (a byte array of any length).
     *
     * @param nBytes the number of bytes to be placed in each of the resulting blocks.
     * @param block  an array of bytes of any length.
     * @return a BlockMessage where each Block is of length nBytes.
     */
    def apply(nBytes: Int, block: Block): BlockMessage =
        BlockMessage(for (b <- block.grouped(nBytes)) yield pad(nBytes, b))

    /**
     * Method to construct a BlockMessage from an iterator of Blocks.
     *
     * @param blocks an iterator of Blocks.
     * @return a BlockMessage based on a Seq[Block].
     */
    def apply(blocks: Iterator[Block]): BlockMessage = BlockMessage(blocks.toSeq)

    /**
     * Method to construct a BlockMessage from a String.
     *
     * @param nBytes  the number of bytes to be placed in each of the resulting blocks.
     * @param message a String of characters.
     * @return a BlockMessage.
     */
    def apply(nBytes: Int, message: String): BlockMessage = apply(nBytes, message.getBytes())

    // NOTE: for now, padding is fixed.
    val padding: Byte = 0.toByte

    /**
     * Method to pad a Block which has insufficient length.
     *
     * @param nBytes the number of bytes required in the resulting Block.
     * @param block  a Block which may need padding.
     * @return a Block which may be a new Block (if the input was short).
     */
    def pad(nBytes: Int, block: Block): Block = if block.length == nBytes then block else {
        val result = new Array[Byte](nBytes)
        System.arraycopy(block, 0, result, 0, block.length)
        for (x <- block.length until nBytes) result(x) = padding
        result
    }

    /**
     * Implicit class to enable the "xor" operator.
     *
     * @param block a Block.
     */
    implicit class Xor(block: Block) {
        /**
         * Method to XOR block with other.
         * NOTE: the sizes of the blocks should be the same but, in any case, the length of the output will be that of the shortest block.
         *
         * @param other a Block.
         * @return
         */
        infix def xor(other: Block): Block = for ((b1, b2) <- block zip other) yield xor(b1, b2)

        private def xor(b1: Byte, b2: Byte) = (b1.toInt ^ b2.toInt).toByte
    }

    /**
     * Method to render a Block as a String.
     *
     * @param b the Block to be rendered.
     * @return a String.
     */
    def render(b: Block): String = {
        val sb = new mutable.StringBuilder()
        b.foreach(byte => sb.append("%02x".format(byte)))
        sb.toString
    }
}