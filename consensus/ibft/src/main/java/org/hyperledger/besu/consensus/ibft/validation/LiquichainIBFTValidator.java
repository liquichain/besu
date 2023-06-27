package org.hyperledger.besu.consensus.ibft.validation;

import org.hyperledger.besu.config.LiquichainIBFTConfigOptions;
import org.hyperledger.besu.consensus.ibft.enums.LiquichainIBFTAllowListType;
import org.hyperledger.besu.consensus.ibft.messagedata.LiquichainIBFTContractAddressListMessageData;
import org.hyperledger.besu.ethereum.eth.manager.EthContext;
import org.hyperledger.besu.ethereum.eth.manager.EthPeer;
import org.hyperledger.besu.ethereum.p2p.rlpx.connections.PeerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LiquichainIBFTValidator {
  private final List<String> whiteList;
  private final List<String> blackList;
  private final Map<String, List<String>> peersWhiteList;
  private final Map<String, List<String>> peersBlackList;

  private EthContext ethContext;

  private static final Logger LOG = LoggerFactory.getLogger(LiquichainIBFTValidator.class);

  public LiquichainIBFTValidator(final LiquichainIBFTConfigOptions ibftConfigOptions) {
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
      peer.send(LiquichainIBFTContractAddressListMessageData.create(whiteList, LiquichainIBFTAllowListType.WHITE_LIST));
      peer.send(LiquichainIBFTContractAddressListMessageData.create(blackList, LiquichainIBFTAllowListType.BLACK_LIST));
    } catch (PeerConnection.PeerNotConnected e) {
      throw new RuntimeException(e);
    }

  }

}
