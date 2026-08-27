/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.velocity.limbo;

import static org.junit.jupiter.api.Assertions.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.DecoderException;
import java.util.List;
import org.junit.jupiter.api.Test;

class VarIntFrameTest {

  private static ByteBuf frame(byte[] payload) {
    var out = Unpooled.buffer();
    VarIntLengthEncoder.writeVarInt(out, payload.length);
    out.writeBytes(payload);
    return out;
  }

  private static EmbeddedChannel decoderChannel() {
    var channel = new EmbeddedChannel(new VarIntFrameDecoder());
    channel.runPendingTasks();
    return channel;
  }

  @Test
  void roundTripsVarintPayloadsOfEveryLength() {
    var small = new byte[64]; // 1-byte length varint
    small[0] = 0x10;
    var medium = new byte[300]; // 2-byte length varint
    medium[0] = 0x21;
    var large = new byte[40000]; // 3-byte length varint
    large[0] = 0x42;
    var tiny = new byte[] {(byte) 0xAA};

    var channel = decoderChannel();
    channel.writeInbound(
        Unpooled.wrappedBuffer(
            frame(tiny).nioBuffer(),
            frame(small).nioBuffer(),
            frame(medium).nioBuffer(),
            frame(large).nioBuffer()));

    for (byte[] payload : List.of(tiny, small, medium, large)) {
      var decoded = (ByteBuf) requireNonNull(channel.readInbound());
      assertEquals(
          payload.length, decoded.readableBytes(), "payload of " + payload.length + " bytes");
      assertEquals(payload[0], decoded.readByte());
      decoded.release();
    }
    assertNull(channel.readInbound());
    channel.finishAndReleaseAll();
  }

  @Test
  void encoderEmitsOneFramePerPacket() {
    var channel = new EmbeddedChannel(new VarIntLengthEncoder());
    var payload = Unpooled.wrappedBuffer(new byte[300]);
    channel.writeOutbound(payload.copy());

    var framed = (ByteBuf) requireNonNull(channel.readOutbound());
    int length = 0;
    int shift = 0;
    int b;
    do {
      b = framed.readByte();
      length |= (b & 0x7F) << shift;
      shift += 7;
    } while ((b & 0x80) != 0);
    assertEquals(300, length);
    assertEquals(300, framed.readableBytes());
    framed.release();
    channel.finishAndReleaseAll();
  }

  @Test
  void encoderProducesFiveByteVarintsForHugeValues() {
    var out = Unpooled.buffer();
    VarIntLengthEncoder.writeVarInt(out, 0x7FFFFFFF);
    assertEquals(5, out.readableBytes(), "max int encodes as a 5-byte varint");
    assertEquals((byte) 0xFF, out.readByte());
    assertEquals((byte) 0xFF, out.readByte());
    assertEquals((byte) 0xFF, out.readByte());
    assertEquals((byte) 0xFF, out.readByte());
    assertEquals((byte) 0x07, out.readByte());
    out.release();
  }

  @Test
  void truncatedInputWaitsForMoreBytes() {
    var payload = new byte[300];
    var wire = frame(payload);
    var partial = wire.copy();
    partial.setIndex(partial.readerIndex(), partial.writerIndex() - 1); // drop last payload byte

    var channel = decoderChannel();
    channel.writeInbound(partial);
    assertNull(channel.readInbound(), "incomplete frame must not be emitted");

    // delivering the missing byte completes the frame
    channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {payload[299]}));
    var decoded = (ByteBuf) requireNonNull(channel.readInbound());
    assertEquals(300, decoded.readableBytes());
    decoded.release();
    wire.release();
    channel.finishAndReleaseAll();
  }

  @Test
  void partialVarintWaitsForMoreBytes() {
    var channel = decoderChannel();
    channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {(byte) 0x82})); // continuation bit set
    assertNull(channel.readInbound(), "half a length varint must not be emitted");
    channel.finishAndReleaseAll();
  }

  @Test
  void rejectsZeroLengthFrames() {
    var channel = decoderChannel();
    // 0x80 0x00: minimal encoding of length 0 that survives the leading-NUL skip
    assertThrows(
        DecoderException.class,
        () -> channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {(byte) 0x80, 0x00})),
        "length 0 must be rejected");
    channel.finishAndReleaseAll();
  }

  @Test
  void rejectsFramesOverTheSizeCap() {
    var channel = decoderChannel();
    // varint 0x80 0x80 0x80 0x08 = 2097152, one byte over the 2097151 cap
    var wire = Unpooled.wrappedBuffer(new byte[] {(byte) 0x80, (byte) 0x80, (byte) 0x80, 0x08});
    assertThrows(DecoderException.class, () -> channel.writeInbound(wire));
    channel.finishAndReleaseAll();
  }

  @Test
  void rejectsNeverEndingVarints() {
    var channel = decoderChannel();
    var wire =
        Unpooled.wrappedBuffer(
            new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x01});
    var ex = assertThrows(DecoderException.class, () -> channel.writeInbound(wire));
    assertTrue(ex.getMessage().contains("VarInt too big"), ex.getMessage());
    channel.finishAndReleaseAll();
  }

  @Test
  void skipsLeadingNulRuns() {
    var channel = decoderChannel();
    var wire = Unpooled.buffer();
    wire.writeZero(4); // proxy garbage prefix
    VarIntLengthEncoder.writeVarInt(wire, 3);
    wire.writeByte(7);
    wire.writeByte(7);
    wire.writeByte(7);

    channel.writeInbound(wire);
    var decoded = (ByteBuf) requireNonNull(channel.readInbound());
    assertEquals(3, decoded.readableBytes());
    assertEquals(7, decoded.readByte());
    decoded.release();
    channel.finishAndReleaseAll();
  }

  private static Object requireNonNull(Object o) {
    assertTrue(o != null, "expected a frame");
    return o;
  }
}
