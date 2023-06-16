package org.hyperledger.besu.consensus.ibft.validation;

import org.hyperledger.besu.config.LiquichainIBFTConfigOptions;

import java.util.List;

public class LiquichainIBFTValidator {
  private final List<String> allowList;

  public LiquichainIBFTValidator(final LiquichainIBFTConfigOptions ibftConfigOptions) {
    allowList = ibftConfigOptions.getAllowListContractAddresses();
  }

  public List<String> getAllowListContractAddresses() {
    return allowList;
  }

  public void updateAllowList(final String address, final Boolean add) {
    Boolean contain = allowList.contains(address);
    if (add) {
      if (!contain) {
        allowList.add(address);
      }
    } else {
      if (contain) {
        allowList.remove(address);
      }
    }
  }
}
