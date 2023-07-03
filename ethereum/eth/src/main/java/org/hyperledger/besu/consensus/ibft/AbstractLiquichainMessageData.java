package org.hyperledger.besu.consensus.ibft;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.AbstractMessageData;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.MessageData;

import java.util.function.Function;

public abstract class AbstractLiquichainMessageData extends AbstractMessageData {
  /**
   * Instantiates a new Abstract bft message data.
   *
   * @param data the data
   */
  protected AbstractLiquichainMessageData(final Bytes data) {
    super(data);
  }

  /**
   * From message data to type of AbstractBftMessageData.
   *
   * @param <T>         the type parameter AbstractBftMessageData
   * @param messageData the message data
   * @param messageCode the message code
   * @param clazz       the clazz
   * @param constructor the constructor
   * @return the type of AbstractBftMessageData
   */
  protected static <T extends AbstractLiquichainMessageData> T fromMessageData(
      final MessageData messageData,
      final int messageCode,
      final Class<T> clazz,
      final Function<Bytes, T> constructor) {
    if (clazz.isInstance(messageData)) {
      @SuppressWarnings("unchecked")
      T castMessage = (T) messageData;
      return castMessage;
    }
    final int code = messageData.getCode();
    if (code != messageCode) {
      throw new IllegalArgumentException(
          String.format(
              "MessageData has code %d and thus is not a %s", code, clazz.getSimpleName()));
    }

    return constructor.apply(messageData.getData());
  }
}
