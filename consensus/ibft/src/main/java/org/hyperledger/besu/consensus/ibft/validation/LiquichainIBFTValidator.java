package org.hyperledger.besu.consensus.ibft.validation;

import org.hyperledger.besu.config.LiquichainIBFTConfigOptions;

import java.util.List;

public class LiquichainIBFTValidator {
  private final List<String> whiteList;
  private final List<String> blackList;

  public LiquichainIBFTValidator(final LiquichainIBFTConfigOptions ibftConfigOptions) {
    whiteList = ibftConfigOptions.getSmartContractWhiteList();
    blackList = ibftConfigOptions.getSmartContractBlackList();
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
}
