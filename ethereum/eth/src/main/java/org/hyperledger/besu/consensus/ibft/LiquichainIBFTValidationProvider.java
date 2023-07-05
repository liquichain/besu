package org.hyperledger.besu.consensus.ibft;

import org.hyperledger.besu.config.LiquichainIBFTConfigOptions;
import org.hyperledger.besu.consensus.ibft.enums.LiquichainIBFTAllowListType;
import org.hyperledger.besu.ethereum.eth.manager.EthContext;
import org.hyperledger.besu.ethereum.eth.manager.EthPeer;
import org.hyperledger.besu.ethereum.mainnet.ValidationResult;
import org.hyperledger.besu.ethereum.p2p.rlpx.connections.PeerConnection;
import org.hyperledger.besu.ethereum.transaction.TransactionInvalidReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LiquichainIBFTValidationProvider {
  private final List<String> whiteList;
  private final List<String> blackList;
  private final Map<String, List<String>> peersWhiteList;
  private final Map<String, List<String>> peersBlackList;

  private EthContext ethContext;

  private static final Logger LOG = LoggerFactory.getLogger(LiquichainIBFTValidationProvider.class);

  public LiquichainIBFTValidationProvider(final LiquichainIBFTConfigOptions ibftConfigOptions) {
    whiteList = ibftConfigOptions.getSmartContractWhiteList();
    blackList = ibftConfigOptions.getSmartContractBlackList();

    peersWhiteList = new HashMap<>();
    peersBlackList = new HashMap<>();
  }

  public void setEthContext(final EthContext context) {
    ethContext = context;
    ethContext.getEthPeers().subscribeConnect(this::handlePeer);
  }

  public List<String> getSmartContractWhiteList() {
    return whiteList;
  }

  public List<String> getSmartContractBlackList() {
    return blackList;
  }

  public void updateSmartContractWhiteList(final String address, final Boolean add) {
    Boolean contain = whiteList.contains(address);
    if (add) {
      if (!contain) {
        whiteList.add(address);
      }
    } else {
      if (contain) {
        whiteList.remove(address);
      }
    }
    sendListOnUpdate(LiquichainIBFTAllowListType.WHITE_LIST);
  }

  public void updateSmartContractBlackList(final String address, final Boolean add) {
    Boolean contain = blackList.contains(address);
    if (add) {
      if (!contain) {
        blackList.add(address);
      }
    } else {
      if (contain) {
        blackList.remove(address);
      }
    }
    sendListOnUpdate(LiquichainIBFTAllowListType.BLACK_LIST);
  }

  public void updatePeerContractAddressList(final String peerId, final List<String> contractAddresses, final LiquichainIBFTAllowListType type) {
    LOG.info(String.format("PeerId: %s, ContractCount: %d, type: %s", peerId, contractAddresses.size(), type.getValue()));
    if (type.equals(LiquichainIBFTAllowListType.BLACK_LIST)) {
      peersBlackList.put(peerId, contractAddresses);
    } else {
      peersWhiteList.put(peerId, contractAddresses);
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

  public List<String> getSmartContracListByPeer(final EthPeer peer, final LiquichainIBFTAllowListType type) {
    return switch (type) {
      case WHITE_LIST -> peersWhiteList.getOrDefault(peer.getId().toString(), new ArrayList<>());
      case BLACK_LIST -> peersBlackList.getOrDefault(peer.getId().toString(), new ArrayList<>());
    };
  }

  public boolean validateBySmartContractList(final String contractAddress, final Optional<EthPeer> peer) {
    List<String> whiteList;
    List<String> blackList;
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
      final Optional<String> matchAddress = whiteList.stream().filter(address -> address.equals(contractAddress)).findAny();
      LOG.info("Whitelist Is Present " + matchAddress.isPresent());
      if (matchAddress.isEmpty()) {
        return false;
      }
    }

    if (blackList != null && !blackList.isEmpty()) {
      final Optional<String> matchAddress = blackList.stream().filter(address -> address.equals(contractAddress)).findAny();
      LOG.info("Blacklist is Present " + matchAddress.isEmpty());
      return matchAddress.isEmpty();
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
