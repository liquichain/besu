package org.hyperledger.besu.consensus.ibft.validation;

import org.hyperledger.besu.config.LiquichainIBFTConfigOptions;
import org.hyperledger.besu.ethereum.eth.manager.EthContext;
import org.hyperledger.besu.ethereum.eth.manager.EthPeer;

import java.util.List;

public class LiquichainIBFTValidator {
  private final List<String> whiteList;
  private final List<String> blackList;


  private EthContext ethContext;

  public LiquichainIBFTValidator(final LiquichainIBFTConfigOptions ibftConfigOptions) {
    whiteList = ibftConfigOptions.getSmartContractWhiteList();
    blackList = ibftConfigOptions.getSmartContractBlackList();
  }

  public void setEthContext(EthContext context) {
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

  public void handlePeer(final EthPeer peer) {


  }

}
