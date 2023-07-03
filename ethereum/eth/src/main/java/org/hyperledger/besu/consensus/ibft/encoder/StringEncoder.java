package org.hyperledger.besu.consensus.ibft.encoder;

import org.apache.tuweni.bytes.Bytes;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringEncoder {

  public static Charset charset = StandardCharsets.UTF_8;

  public static Bytes getBytes(final String str) {
    return Bytes.wrap(str.getBytes(charset));
  }

  public static String readBytes(final Bytes bytes) {
    return new String(bytes.toArray(), charset);
  }
}
