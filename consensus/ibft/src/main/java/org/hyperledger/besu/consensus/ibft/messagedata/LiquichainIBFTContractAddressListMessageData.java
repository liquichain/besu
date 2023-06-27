package org.hyperledger.besu.consensus.ibft.messagedata;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.consensus.common.bft.messagedata.AbstractBftMessageData;
import org.hyperledger.besu.consensus.ibft.encoder.StringEncoder;
import org.hyperledger.besu.consensus.ibft.enums.LiquichainIBFTAllowListType;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.MessageData;
import org.hyperledger.besu.ethereum.rlp.BytesValueRLPInput;
import org.hyperledger.besu.ethereum.rlp.BytesValueRLPOutput;
import org.hyperledger.besu.ethereum.rlp.RLPInput;

import java.util.List;

public class LiquichainIBFTContractAddressListMessageData extends AbstractBftMessageData {
  /**
   * Instantiates a new Abstract bft message data.
   *
   * @param data the data
   */
  protected LiquichainIBFTContractAddressListMessageData(final Bytes data) {
    super(data);
  }

  private static final int MESSAGE_CODE = LiquichainIBFT.CONTRACT_ADDRESS_LIST;

  @Override
  public int getCode() {
    return MESSAGE_CODE;
  }

  /**
   * Create liquichain ibft contract address list message data.
   *
   * @param addresses the addresses
   * @param type      the type
   * @return the liquichain ibft contract address list message data
   */
  public static LiquichainIBFTContractAddressListMessageData create(final List<String> addresses, final LiquichainIBFTAllowListType type) {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();


    out.writeBytes(StringEncoder.getBytes(type.getValue()));

    out.startList();
    for (final String address : addresses) {
      out.writeBytes(StringEncoder.getBytes(address));
    }
    out.endList();

    return new LiquichainIBFTContractAddressListMessageData(out.encoded());
  }

  public static LiquichainIBFTContractAddressListMessageData fromMessageData(final MessageData messageData) {
    return fromMessageData(messageData,
        MESSAGE_CODE,
        LiquichainIBFTContractAddressListMessageData.class,
        LiquichainIBFTContractAddressListMessageData::new);
  }

  public LiquichainIBFTAllowListType getType() {
    final BytesValueRLPInput input = new BytesValueRLPInput(data, false);
    final Bytes type = input.readBytes();
    return LiquichainIBFTAllowListType.fromString(StringEncoder.readBytes(type));
  }

  public List<String> getContractAddressList() {
    final BytesValueRLPInput input = new BytesValueRLPInput(data, false);
    return input.readList(this::readFrom);
  }

  public String readFrom(final RLPInput input) {
    final Bytes address = input.readBytes();
    return StringEncoder.readBytes(address);
  }
}
