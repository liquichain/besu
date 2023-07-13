package org.hyperledger.besu.consensus.ibft;

import org.hyperledger.besu.consensus.common.bft.network.PeerConnectionTracker;
import org.hyperledger.besu.consensus.common.bft.network.ValidatorMulticaster;
import org.hyperledger.besu.consensus.common.bft.network.ValidatorPeers;
import org.hyperledger.besu.consensus.common.validator.ValidatorProvider;
import org.hyperledger.besu.datatypes.Address;
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

public class LiquichainSwamPeers implements ValidatorMulticaster, PeerConnectionTracker {
  private static final Logger LOG = LoggerFactory.getLogger(ValidatorPeers.class);


  private final Map<Address, Set<PeerConnection>> connectionsByAddress = new ConcurrentHashMap<>();
  private final ValidatorProvider validatorProvider;
  private final String protocolName;


  private final Map<Address, Set<Address>> nodeByContractAddress = new ConcurrentHashMap<>();

  /**
   * Instantiates a new Validator peers.
   *
   * @param validatorProvider the validator provider
   * @param protocolName      the protocol name
   */
  public LiquichainSwamPeers(ValidatorProvider validatorProvider, String protocolName) {
    this.validatorProvider = validatorProvider;
    this.protocolName = protocolName;
  }

  public void addNodeToSmartContract(Address contractAddress, Set<Address> nodeAddresses) {
    final Set<Address> addresses = nodeByContractAddress.computeIfAbsent(contractAddress,
        (k) -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
    addresses.addAll(nodeAddresses);
  }

  public void removeNodeToSmartContract(Address contractAddress, Set<Address> nodeAddresses) {
    final Set<Address> addresses = nodeByContractAddress.computeIfAbsent(contractAddress,
        (k) -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
    addresses.addAll(nodeAddresses);
  }

  @Override
  public void add(PeerConnection newConnection) {
    final Address peerAddress = newConnection.getPeerInfo().getAddress();
    final Set<PeerConnection> connections =
        connectionsByAddress.computeIfAbsent(
            peerAddress, (k) -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
    connections.add(newConnection);
  }

  @Override
  public void remove(PeerConnection removedConnection) {
    final Address peerAddress = removedConnection.getPeerInfo().getAddress();
    final Set<PeerConnection> connections = connectionsByAddress.get(peerAddress);
    if (connections == null) {
      return;
    }
    connections.remove(removedConnection);
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
