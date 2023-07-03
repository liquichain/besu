package org.hyperledger.besu.consensus.ibft;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.consensus.ibft.enums.LiquichainIBFTAllowListType;
import org.hyperledger.besu.consensus.ibft.messagedata.LiquichainIBFTContractAddressListMessageData;
import org.hyperledger.besu.ethereum.p2p.network.ProtocolManager;
import org.hyperledger.besu.ethereum.p2p.peers.Peer;
import org.hyperledger.besu.ethereum.p2p.rlpx.connections.PeerConnection;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.Capability;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.Message;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.messages.DisconnectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class LiquichainIBFTProtocolManager implements ProtocolManager {
  private final String subProtocolName;
  private final Capability supportedCapability;
  private final LiquichainIBFTValidationProvider validationProvider;

  private static final Logger LOG = LoggerFactory.getLogger(LiquichainIBFTProtocolManager.class);

  public LiquichainIBFTProtocolManager(final LiquichainIBFTValidationProvider validationProvider, final Capability supportedCapability, final String subProtocolName) {
    this.subProtocolName = subProtocolName;
    this.supportedCapability = supportedCapability;
    this.validationProvider = validationProvider;
  }

  @Override
  public String getSupportedProtocol() {
    return subProtocolName;
  }

  @Override
  public List<Capability> getSupportedCapabilities() {
    return Arrays.asList(supportedCapability);
  }

  @Override
  public void stop() {

  }

  @Override
  public void awaitStop() throws InterruptedException {

  }

  @Override
  public void processMessage(final Capability cap, final Message message) {
    LOG.info("Process Message " + message.getConnection().getPeer().getId());
    final LiquichainIBFTContractAddressListMessageData messageData = LiquichainIBFTContractAddressListMessageData.fromMessageData(message.getData());
    final Bytes peerId = message.getConnection().getPeer().getId();
    final List<String> contractAddressList = messageData.getContractAddressList();
    final LiquichainIBFTAllowListType type = messageData.getType();
    LOG.info("ContractAddress " + contractAddressList);
    LOG.info("Type " + type);
    validationProvider.updatePeerContractAddressList(peerId.toString(), contractAddressList, type);
  }

  @Override
  public void handleNewConnection(final PeerConnection peerConnection) {

  }

  @Override
  public boolean shouldConnect(final Peer peer, final boolean incoming) {
    return false;
  }

  @Override
  public void handleDisconnect(final PeerConnection peerConnection, final DisconnectMessage.DisconnectReason disconnectReason, final boolean initiatedByPeer) {

  }

  @Override
  public int getHighestProtocolVersion() {
    return supportedCapability.getVersion();
  }
}
