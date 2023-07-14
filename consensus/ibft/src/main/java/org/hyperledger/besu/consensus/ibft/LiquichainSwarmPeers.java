package org.hyperledger.besu.consensus.ibft;

import org.hyperledger.besu.consensus.common.bft.network.PeerConnectionTracker;
import org.hyperledger.besu.consensus.common.bft.network.ValidatorMulticaster;
import org.hyperledger.besu.consensus.common.bft.network.ValidatorPeers;
import org.hyperledger.besu.consensus.common.validator.ValidatorProvider;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.ethereum.p2p.peers.Peer;
import org.hyperledger.besu.ethereum.p2p.rlpx.connections.PeerConnection;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.MessageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LiquichainSwarmPeers implements ValidatorMulticaster, PeerConnectionTracker {
  private static final Logger LOG = LoggerFactory.getLogger(ValidatorPeers.class);


  private final Map<Address, Set<PeerConnection>> connectionsByAddress = new ConcurrentHashMap<>();
  private final ValidatorProvider validatorProvider;
  private final String protocolName;


  private final Map<Address, Set<PeerConnection>> nodeByContractAddress = new ConcurrentHashMap<>();

  /**
   * Instantiates a new Validator peers.
   *
   * @param validatorProvider the validator provider
   * @param protocolName      the protocol name
   */
  public LiquichainSwarmPeers(final ValidatorProvider validatorProvider, final String protocolName) {
    this.validatorProvider = validatorProvider;
    this.protocolName = protocolName;
  }

  public void addNodeToSmartContracts(final PeerConnection peerConnection, final Set<Address> contractAddresses) {
    for (Address contractAddress : contractAddresses) {
      final Set<PeerConnection> addresses = nodeByContractAddress.computeIfAbsent(contractAddress,
          (k) -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
      addresses.add(peerConnection);
    }
  }

  public void removeNodeToSmartContracts(final PeerConnection peerConnection, final Set<Address> contractAddresses) {
    for (Address contractAddress : contractAddresses) {
      final Set<PeerConnection> addresses = nodeByContractAddress.computeIfAbsent(contractAddress,
          (k) -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
      if (addresses.contains(peerConnection))
        addresses.remove(peerConnection);
    }
  }

  @Override
  public void add(final PeerConnection newConnection) {
//    final Address peerAddress = newConnection.getPeerInfo().getAddress();
//    final Set<PeerConnection> connections =
//        connectionsByAddress.computeIfAbsent(
//            peerAddress, (k) -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
//    connections.add(newConnection);
  }

  @Override
  public void remove(final PeerConnection removedConnection) {
//    final Address peerAddress = removedConnection.getPeerInfo().getAddress();
//    final Set<PeerConnection> connections = connectionsByAddress.get(peerAddress);
//    if (connections == null) {
//      return;
//    }
//    connections.remove(removedConnection);

    for (Map.Entry<Address, Set<PeerConnection>> entry : nodeByContractAddress.entrySet()) {
      Set<PeerConnection> connections = entry.getValue();
      if (connections == null) continue;
      connections.remove(removedConnection);
    }

  }

  @Override
  public void send(final MessageData message) {
    sendMessageToSpecificAddresses(getLatestValidators(), message);
  }

  @Override
  public void send(final MessageData message, final Collection<Address> denylist) {
    final Collection<Address> includedValidators =
        getLatestValidators().stream()
            .filter(a -> !denylist.contains(a))
            .collect(Collectors.toSet());
    sendMessageToSpecificAddresses(includedValidators, message);
  }


  private void sendMessageToSpecificAddresses(
      final Collection<Address> recipients, final MessageData message) {
    LOG.trace(
        "Sending message to peers messageCode={} recipients={}", message.getCode(), recipients);
    recipients.stream()
        .map(connectionsByAddress::get)
        .filter(Objects::nonNull)
        .flatMap(Set::stream)
        .forEach(
            connection -> {
              try {
                connection.sendForProtocol(protocolName, message);
              } catch (final PeerConnection.PeerNotConnected peerNotConnected) {
                LOG.trace(
                    "Lost connection to a validator. remoteAddress={} peerInfo={}",
                    connection.getRemoteAddress(),
                    connection.getPeerInfo());
              }
            });
  }

  private Collection<Address> getLatestValidators() {
    return validatorProvider.getValidatorsAtHead();
  }
}
