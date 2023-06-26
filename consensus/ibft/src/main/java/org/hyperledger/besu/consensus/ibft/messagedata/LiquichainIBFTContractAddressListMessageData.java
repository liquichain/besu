package org.hyperledger.besu.consensus.ibft.messagedata;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.consensus.common.bft.messagedata.AbstractBftMessageData;

public class LiquichainIBFTContractAddressListMessageData extends AbstractBftMessageData {
  /**
   * Instantiates a new Abstract bft message data.
   *
   * @param data the data
   */
  protected LiquichainIBFTContractAddressListMessageData(Bytes data) {
    super(data);
  }

  @Override
  public int getCode() {
    return LiquichainIBFT.CONTRACT_ADDRESS_LIST;
  }
}
