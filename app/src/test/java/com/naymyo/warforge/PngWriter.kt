package com.naymyo.warforge

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.util.zip.CRC32
import java.util.zip.Deflater

/**
 * Minimal 8-bit RGB PNG encoder.
 *
 * Android unit tests compile against a stubbed `android.jar` with no `java.awt`, so
 * `BufferedImage` and `ImageIO` are not available here. PNG's baseline format is small
 * enough to write by hand: a header, zlib-deflated scanlines each prefixed with a
 * zero filter byte, and a terminator.
 */
object PngWriter {

    fun write(file: File, width: Int, height: Int, rgb: ByteArray) {
        require(rgb.size == width * height * 3) { "pixel buffer is the wrong size" }
        val out = ByteArrayOutputStream()
        val data = DataOutputStream(out)
        data.write(byteArrayOf(0x89.toByte(), 'P'.code.toByte(), 'N'.code.toByte(), 'G'.code.toByte(), 13, 10, 26, 10))

        val header = ByteArrayOutputStream()
        DataOutputStream(header).apply {
            writeInt(width)
            writeInt(height)
            writeByte(8)      // bit depth
            writeByte(2)      // colour type: truecolour
            writeByte(0)      // deflate
            writeByte(0)      // adaptive filtering
            writeByte(0)      // no interlace
        }
        chunk(data, "IHDR", header.toByteArray())

        val raw = ByteArray(height * (1 + width * 3))
        var src = 0
        var dst = 0
        for (y in 0 until height) {
            raw[dst++] = 0                             // filter: none
            System.arraycopy(rgb, src, raw, dst, width * 3)
            src += width * 3
            dst += width * 3
        }
        chunk(data, "IDAT", deflate(raw))
        chunk(data, "IEND", ByteArray(0))
        file.writeBytes(out.toByteArray())
    }

    private fun deflate(input: ByteArray): ByteArray {
        val deflater = Deflater(Deflater.BEST_SPEED)
        deflater.setInput(input)
        deflater.finish()
        val out = ByteArrayOutputStream(input.size / 2)
        val buffer = ByteArray(64 * 1024)
        while (!deflater.finished()) {
            out.write(buffer, 0, deflater.deflate(buffer))
        }
        deflater.end()
        return out.toByteArray()
    }

    private fun chunk(out: DataOutputStream, type: String, body: ByteArray) {
        out.writeInt(body.size)
        val typeBytes = type.toByteArray(Charsets.US_ASCII)
        out.write(typeBytes)
        out.write(body)
        val crc = CRC32()
        crc.update(typeBytes)
        crc.update(body)
        out.writeInt(crc.value.toInt())
    }
}
