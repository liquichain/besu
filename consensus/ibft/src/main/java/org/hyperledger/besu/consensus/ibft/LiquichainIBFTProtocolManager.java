package org.hyperledger.besu.consensus.ibft;

import org.hyperledger.besu.consensus.ibft.enums.LiquichainIBFTAllowListType;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.ethereum.p2p.network.ProtocolManager;
import org.hyperledger.besu.ethereum.p2p.peers.Peer;
import org.hyperledger.besu.ethereum.p2p.rlpx.connections.PeerConnection;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.Capability;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.Message;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.MessageData;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.messages.DisconnectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LiquichainIBFTProtocolManager implements ProtocolManager {
  private final String subProtocolName;
  private final Capability supportedCapability;
  private final LiquichainIBFTValidationProvider validationProvider;

  private final LiquichainSwarmPeers swarmPeers;
  private static final Logger LOG = LoggerFactory.getLogger(LiquichainIBFTProtocolManager.class);

  public LiquichainIBFTProtocolManager(final LiquichainIBFTValidationProvider validationProvider,
                                       final LiquichainSwarmPeers swarmPeers,
                                       final Capability supportedCapability, final String subProtocolName) {
    this.subProtocolName = subProtocolName;
    this.supportedCapability = supportedCapability;
    this.validationProvider = validationProvider;
    this.swarmPeers = swarmPeers;
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
    final MessageData data = message.getData();
    switch (data.getCode()) {
      case LiquichainIBFT.CONTRACT_ADDRESS_LIST:
        LOG.info("Running Contract address list");
        final LiquichainIBFTContractAddressListMessageData messageData = LiquichainIBFTContractAddressListMessageData.fromMessageData(message.getData());
        final PeerConnection peerConnection = message.getConnection();
        final Set<Address> contractAddressList = messageData.getContractAddressList();
        final LiquichainIBFTAllowListType type = messageData.getType();
        validationProvider.updatePeerContractAddressList(peerConnection.getPeerInfo().getAddress(), contractAddressList, type);
        swarmPeers.addNodeToSmartContracts(peerConnection, contractAddressList);
        break;
      default:
        throw new IllegalArgumentException(
            "Received message does not conform to any recognised IBFT message structure.");
    }
  }

  @Override
  public void handleNewConnection(final PeerConnection peerConnection) {
    validationProvider.handlePeer(peerConnection);
  }

  @Override
  public boolean shouldConnect(final Peer peer, final boolean incoming) {
    return false;
  }

  @Override
  public void handleDisconnect(final PeerConnection peerConnection, final DisconnectMessage.DisconnectReason disconnectReason, final boolean initiatedByPeer) {
    swarmPeers.remove(peerConnection);
  }

  @Override
  public int getHighestProtocolVersion() {
    return supportedCapability.getVersion();
  }
}
