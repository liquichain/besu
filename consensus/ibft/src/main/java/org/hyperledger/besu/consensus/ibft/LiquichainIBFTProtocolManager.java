package org.hyperledger.besu.consensus.ibft;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.consensus.ibft.encoder.StringEncoder;
import org.hyperledger.besu.consensus.ibft.messagedata.LiquichainIBFTContractAddressListMessageData;
import org.hyperledger.besu.consensus.ibft.validation.LiquichainIBFTValidator;
import org.hyperledger.besu.ethereum.p2p.network.ProtocolManager;
import org.hyperledger.besu.ethereum.p2p.peers.Peer;
import org.hyperledger.besu.ethereum.p2p.rlpx.connections.PeerConnection;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.Capability;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.Message;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.MessageData;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.messages.DisconnectMessage;

import java.util.Arrays;
import java.util.List;

public class LiquichainIBFTProtocolManager implements ProtocolManager {
  private final String subProtocolName;
  private final Capability supportedCapability;
  private final LiquichainIBFTValidator validator;

  public LiquichainIBFTProtocolManager(final LiquichainIBFTValidator validator, final Capability supportedCapability, final String subProtocolName) {
    this.subProtocolName = subProtocolName;
    this.supportedCapability = supportedCapability;
    this.validator = validator;
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
    final LiquichainIBFTContractAddressListMessageData messageData = LiquichainIBFTContractAddressListMessageData.fromMessageData(message.getData());
    final Bytes peerId = message.getConnection().getPeer().getId();
    validator.updatePeerContractAddressList(StringEncoder.readBytes(peerId), messageData.getContractAddressList(), messageData.getType());
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
