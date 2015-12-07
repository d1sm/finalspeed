/*_##########################################################################
  _##
  _##  Copyright (C) 2014  Pcap4J.org
  _##
  _##########################################################################
*/

package net.fs.cap;

import static org.pcap4j.util.ByteArrays.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.packet.LengthBuilder;
import org.pcap4j.packet.TcpPacket.TcpOption;
import org.pcap4j.packet.namednumber.TcpOptionKind;
import org.pcap4j.util.ByteArrays;

/**
 * @author Kaito Yamada
 * @since pcap4j 1.2.0
 */
public final class CustomTcpSackOption implements TcpOption {

  /*
   * http://tools.ietf.org/html/rfc2018
   *
   *                     +--------+--------+
   *                     | Kind=5 | Length |
   *   +--------+--------+--------+--------+
   *   |      Left Edge of 1st Block       |
   *   +--------+--------+--------+--------+
   *   |      Right Edge of 1st Block      |
   *   +--------+--------+--------+--------+
   *   |                                   |
   *   /            . . .                  /
   *   |                                   |
   *   +--------+--------+--------+--------+
   *   |      Left Edge of nth Block       |
   *   +--------+--------+--------+--------+
   *   |      Right Edge of nth Block      |
   *   +--------+--------+--------+--------+
   */

  /**
   *
   */
  private static final long serialVersionUID = -3308738405807657257L;

  private final TcpOptionKind kind = TcpOptionKind.SACK;
  private final byte length;
  private final List<Sack> sacks = new ArrayList<Sack>();

  /**
   * A static factory method.
   * This method validates the arguments by {@link ByteArrays#validateBounds(byte[], int, int)},
   * which may throw exceptions undocumented here.
   *
   * @param rawData rawData
   * @param offset offset
   * @param length length
   * @return a new TcpSackOption object.
   * @throws IllegalRawDataException if parsing the raw data fails.
   */
  public static CustomTcpSackOption newInstance(
    byte[] rawData, int offset, int length
  ) throws IllegalRawDataException {
    ByteArrays.validateBounds(rawData, offset, length);
    return new CustomTcpSackOption(rawData, offset, length);
  }

  private CustomTcpSackOption(byte[] rawData, int offset, int length) throws IllegalRawDataException {
    if (length < 2) {
      StringBuilder sb = new StringBuilder(50);
      sb.append("The raw data length must be more than 1. rawData: ")
        .append(ByteArrays.toHexString(rawData, " "))
        .append(", offset: ")
        .append(offset)
        .append(", length: ")
        .append(length);
      throw new IllegalRawDataException(sb.toString());
    }
    if (rawData[offset] != kind.value()) {
      StringBuilder sb = new StringBuilder(100);
      sb.append("The kind must be: ")
        .append(kind.valueAsString())
        .append(" rawData: ")
        .append(ByteArrays.toHexString(rawData, " "))
        .append(", offset: ")
        .append(offset)
        .append(", length: ")
        .append(length);
      throw new IllegalRawDataException(sb.toString());
    }

    this.length = rawData[1 + offset];
    int lengthFieldAsInt = getLengthAsInt();
    if (lengthFieldAsInt < 2) {
      throw new IllegalRawDataException(
                  "The value of length field must be  more than 1 but: " + lengthFieldAsInt
                );
    }

    if ((lengthFieldAsInt - 2) % (INT_SIZE_IN_BYTES * 2) != 0) {
      StringBuilder sb = new StringBuilder(100);
      sb.append(
           "The value of length field must be an integer multiple of 8 octets long but: "
         )
        .append(lengthFieldAsInt);
      throw new IllegalRawDataException(sb.toString());
    }
    if (length < lengthFieldAsInt) {
      StringBuilder sb = new StringBuilder(100);
      sb.append("rawData is too short. length field: ")
        .append(lengthFieldAsInt)
        .append(", rawData: ")
        .append(ByteArrays.toHexString(rawData, " "))
        .append(", offset: ")
        .append(offset)
        .append(", length: ")
        .append(length);
      throw new IllegalRawDataException(sb.toString());
    }

    for (int i = 2; i < lengthFieldAsInt; i += INT_SIZE_IN_BYTES * 2) {
      sacks.add(
        new Sack(
          ByteArrays.getInt(rawData, i + offset),
          ByteArrays.getInt(rawData, i + INT_SIZE_IN_BYTES + offset)
        )
      );
    }
  }

  private CustomTcpSackOption(Builder builder) {
    if (
         builder == null
      || builder.sacks == null
    ) {
      StringBuilder sb = new StringBuilder();
      sb.append("builder: ").append(builder)
        .append(" builder.sacks: ").append(builder.sacks);
      throw new NullPointerException(sb.toString());
    }

    this.sacks.addAll(builder.sacks);

    if (builder.correctLengthAtBuild) {
      this.length = (byte)length();
    }
    else {
      this.length = builder.length;
    }
  }

  @Override
  public TcpOptionKind getKind() {
    return kind;
  }

  /**
   *
   * @return length
   */
  public byte getLength() { return length; }

  /**
   *
   * @return length
   */
  public int getLengthAsInt() { return 0xFF & length; }

  @Override
  public int length() {
    return sacks.size() * INT_SIZE_IN_BYTES * 2 + 2;
  }

  @Override
  public byte[] getRawData() {
    byte[] rawData = new byte[length()];
    rawData[0] = kind.value();
    rawData[1] = length;

    int offset = 2;
    for (Sack sack: sacks) {
      System.arraycopy(
        ByteArrays.toByteArray(sack.leftEdge), 0,
        rawData, offset, INT_SIZE_IN_BYTES
      );
      System.arraycopy(
        ByteArrays.toByteArray(sack.rightEdge), 0,
        rawData, offset + INT_SIZE_IN_BYTES, INT_SIZE_IN_BYTES
      );
      offset += INT_SIZE_IN_BYTES * 2;
    }

    return rawData;
  }

  /**
   *
   * @return a new Builder object populated with this object's fields.
   */
  public Builder getBuilder() {
    return new Builder(this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[Kind: ")
      .append(kind);
    sb.append("] [Length: ")
      .append(getLengthAsInt())
      .append(" bytes]");
    for (Sack sack: sacks) {
      sb.append(" [LE: ")
        .append(sack.getLeftEdgeAsLong())
        .append(" RE: ")
        .append(sack.getRightEdgeAsLong())
        .append("]");
    }
    return sb.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) { return true; }
    if (!this.getClass().isInstance(obj)) { return false; }

    CustomTcpSackOption other = (CustomTcpSackOption)obj;
    return
         length == other.length
      && sacks.equals(other.sacks);
  }

  public List<Sack> getSacks() {
	return sacks;
}

@Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + length;
    result = 31 * result + sacks.hashCode();
    return result;
  }

  /**
   * @author Kaito Yamada
   * @since pcap4j 1.2.0
   */
  public static final class Builder
  implements LengthBuilder<CustomTcpSackOption> {

    private byte length;
    private boolean correctLengthAtBuild;
    private List<Sack> sacks;

    /**
     *
     */
    public Builder() {}

    private Builder(CustomTcpSackOption option) {
      this.length = option.length;
    }

    /**
     * @param length length
     * @return this Builder object for method chaining.
     */
    public Builder length(byte length) {
      this.length = length;
      return this;
    }

    /**
     * @param sacks sacks
     * @return this Builder object for method chaining.
     */
    public Builder sacks(List<Sack> sacks) {
      this.sacks = sacks;
      return this;
    }

    @Override
    public Builder correctLengthAtBuild(boolean correctLengthAtBuild) {
      this.correctLengthAtBuild = correctLengthAtBuild;
      return this;
    }

    @Override
    public CustomTcpSackOption build() {
      return new CustomTcpSackOption(this);
    }

  }

  /**
   * @author Kaito Yamada
   * @since pcap4j 1.2.0
   */
  public static final class Sack implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1218420566089129438L;

    private final int leftEdge;
    private final int rightEdge;

    /**
     * @param leftEdge leftEdge
     * @param rightEdge rightEdge
     */
    public Sack(int leftEdge, int rightEdge) {
      this.leftEdge = leftEdge;
      this.rightEdge = rightEdge;
    }

    /**
     * @return leftEdge
     */
    public int getLeftEdge() {
      return leftEdge;
    }

    /**
     * @return leftEdge
     */
    public long getLeftEdgeAsLong() {
      return 0xFFFFFFFFL & leftEdge;
    }

    /**
     * @return rightEdge
     */
    public int getRightEdge() {
      return rightEdge;
    }

    /**
     * @return rightEdge
     */
    public long getRightEdgeAsLong() {
      return 0xFFFFFFFFL & rightEdge;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) { return true; }
      if (!this.getClass().isInstance(obj)) { return false; }

      Sack other = (Sack)obj;
      return
           leftEdge == other.leftEdge
        && rightEdge == other.rightEdge;
    }

    @Override
    public int hashCode() {
      int result = 17;
      result = 31 * result + leftEdge;
      result = 31 * result + rightEdge;
      return result;
    }

  }

}
