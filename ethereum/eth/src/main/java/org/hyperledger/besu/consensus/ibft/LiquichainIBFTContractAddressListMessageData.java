package org.hyperledger.besu.consensus.ibft;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.consensus.ibft.encoder.StringEncoder;
import org.hyperledger.besu.consensus.ibft.enums.LiquichainIBFTAllowListType;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.AbstractMessageData;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.MessageData;
import org.hyperledger.besu.ethereum.rlp.BytesValueRLPInput;
import org.hyperledger.besu.ethereum.rlp.BytesValueRLPOutput;
import org.hyperledger.besu.ethereum.rlp.RLPInput;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class LiquichainIBFTContractAddressListMessageData extends AbstractLiquichainMessageData {
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
  public static LiquichainIBFTContractAddressListMessageData create(final Set<Address> addresses, final LiquichainIBFTAllowListType type) {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();


    out.writeBytes(StringEncoder.getBytes(type.getValue()));

    out.startList();
    for (final Address address : addresses) {
      out.writeBytes(StringEncoder.getBytes(address.toString()));
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

  public Set<Address> getContractAddressList() {
    final BytesValueRLPInput input = new BytesValueRLPInput(data, false);
    input.skipNext();
    return new HashSet<>(input.readList(Address::readFrom));
  }
}
