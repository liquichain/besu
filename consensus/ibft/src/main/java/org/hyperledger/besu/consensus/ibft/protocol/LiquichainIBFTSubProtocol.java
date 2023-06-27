package org.hyperledger.besu.consensus.ibft.protocol;

import org.hyperledger.besu.consensus.ibft.messagedata.LiquichainIBFT;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.Capability;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.SubProtocol;

public class LiquichainIBFTSubProtocol implements SubProtocol {

  public static String NAME = "LiquichainIBFT";
  /**
   * The constant IBFV1.
   */
  public static final Capability LiquichainIBFTV1 = Capability.create(NAME, 1);

  private static final LiquichainIBFTSubProtocol INSTANCE = new LiquichainIBFTSubProtocol();
  public static final int MESSAGE_SPACE = 4;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public int messageSpace(final int protocolVersion) {
    return MESSAGE_SPACE;
  }

  public static LiquichainIBFTSubProtocol get() {
    return INSTANCE;
  }


  @Override
  public boolean isValidMessageCode(final int protocolVersion, final int code) {
    switch (code) {
      case LiquichainIBFT.CONTRACT_ADDRESS_LIST:
        return true;
      default:
        return false;
    }
  }

  @Override
  public String messageName(final int protocolVersion, final int code) {
    switch (code) {
      case LiquichainIBFT.CONTRACT_ADDRESS_LIST:
        return "ContractAddressList";
      default:
        return INVALID_MESSAGE_NAME;
    }
  }
}
