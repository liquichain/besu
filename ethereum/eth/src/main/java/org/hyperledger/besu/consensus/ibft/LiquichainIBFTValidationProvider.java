package org.hyperledger.besu.consensus.ibft;

import org.hyperledger.besu.config.LiquichainIBFTConfigOptions;
import org.hyperledger.besu.consensus.ibft.enums.LiquichainIBFTAllowListType;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.ethereum.eth.manager.EthContext;
import org.hyperledger.besu.ethereum.eth.manager.EthPeer;
import org.hyperledger.besu.ethereum.mainnet.ValidationResult;
import org.hyperledger.besu.ethereum.p2p.rlpx.connections.PeerConnection;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.PeerInfo;
import org.hyperledger.besu.ethereum.transaction.TransactionInvalidReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LiquichainIBFTValidationProvider {
  private final Set<Address> whiteList;
  private final Set<Address> blackList;
  private final Map<Address, Set<Address>> peersWhiteList;
  private final Map<Address, Set<Address>> peersBlackList;

  private EthContext ethContext;

  private static final Logger LOG = LoggerFactory.getLogger(LiquichainIBFTValidationProvider.class);

  public LiquichainIBFTValidationProvider(final LiquichainIBFTConfigOptions ibftConfigOptions) {
    whiteList = new HashSet<>(ibftConfigOptions.getSmartContractWhiteList());
    blackList = new HashSet<>(ibftConfigOptions.getSmartContractBlackList());

    peersWhiteList = new HashMap<>();
    peersBlackList = new HashMap<>();
  }

  public void setEthContext(final EthContext context) {
    ethContext = context;
    ethContext.getEthPeers().subscribeConnect(this::handlePeer);
  }

  public void updateSmartContractWhiteList(final Address address, final Boolean add) {
    if (add) {
      whiteList.add(address);
    } else {
      whiteList.remove(address);
    }
    sendListOnUpdate(LiquichainIBFTAllowListType.WHITE_LIST);
  }

  public void updateSmartContractBlackList(final Address address, final Boolean add) {
    if (add) {
      blackList.add(address);
    } else {
      blackList.remove(address);
    }
    sendListOnUpdate(LiquichainIBFTAllowListType.BLACK_LIST);
  }

  public void updatePeerContractAddressList(final Address peerAddress, final Set<Address> contractAddresses, final LiquichainIBFTAllowListType type) {
    LOG.info(String.format("PeerId: %s, ContractCount: %d, type: %s", peerAddress, contractAddresses.size(), type.getValue()));
    if (type.equals(LiquichainIBFTAllowListType.BLACK_LIST)) {
      peersBlackList.put(peerAddress, contractAddresses);
    } else {
      peersWhiteList.put(peerAddress, contractAddresses);
    }
  }

  public void handlePeer(final EthPeer peer) {
    try {
      peer.send(LiquichainIBFTContractAddressListMessageData.create(whiteList, LiquichainIBFTAllowListType.WHITE_LIST), LiquichainIBFTSubProtocol.get().getName());
      peer.send(LiquichainIBFTContractAddressListMessageData.create(blackList, LiquichainIBFTAllowListType.BLACK_LIST), LiquichainIBFTSubProtocol.get().getName());
    } catch (PeerConnection.PeerNotConnected e) {
      throw new RuntimeException(e);
    }
  }

  public Set<Address> getSmartContracListByPeer(final EthPeer peer, final LiquichainIBFTAllowListType type) {
    Address peerAddress = peer.getConnection().getPeerInfo().getAddress();
    return switch (type) {
      case WHITE_LIST ->
          peersWhiteList.computeIfAbsent(peerAddress, (k) -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
      case BLACK_LIST ->
          peersBlackList.computeIfAbsent(peerAddress, (k) -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
    };
  }

  public boolean validateBySmartContractList(final Address contractAddress, final Optional<EthPeer> peer) {
    Set<Address> whiteList;
    Set<Address> blackList;
    if (peer.isEmpty()) {
      whiteList = this.whiteList;
      blackList = this.blackList;
    } else {
      whiteList = getSmartContracListByPeer(peer.get(), LiquichainIBFTAllowListType.WHITE_LIST);
      blackList = getSmartContracListByPeer(peer.get(), LiquichainIBFTAllowListType.BLACK_LIST);

    }

    LOG.info("Whitelist " + whiteList);
    LOG.info("Blacklist " + blackList);

    if (whiteList != null && !whiteList.isEmpty()) {
      if (whiteList.contains(contractAddress)) {
        return false;
      }
    }

    if (blackList != null && !blackList.isEmpty()) {
      return !blackList.contains(contractAddress);
    }
    return true;
  }


  private void sendListOnUpdate(final LiquichainIBFTAllowListType type) {
    final LiquichainIBFTContractAddressListMessageData messageData = switch (type) {
      case WHITE_LIST ->
          LiquichainIBFTContractAddressListMessageData.create(whiteList, LiquichainIBFTAllowListType.WHITE_LIST);
      case BLACK_LIST ->
          LiquichainIBFTContractAddressListMessageData.create(blackList, LiquichainIBFTAllowListType.BLACK_LIST);
    };

    LOG.info("Send List " + messageData.getCode() + " " + ethContext.getEthPeers().streamAvailablePeers().count());

    ethContext.getEthPeers().streamAvailablePeers().forEach(peer -> {
      LOG.info("Stream To Peer", peer.getId());
      try {
        peer.send(messageData, LiquichainIBFTSubProtocol.get().getName());
      } catch (PeerConnection.PeerNotConnected e) {
        LOG.info("Error " + e);
        throw new RuntimeException(e);
      }
    });
  }

}
