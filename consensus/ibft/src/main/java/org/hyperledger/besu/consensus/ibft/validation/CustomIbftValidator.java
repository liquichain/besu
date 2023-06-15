package org.hyperledger.besu.consensus.ibft.validation;

import org.hyperledger.besu.config.CustomIbftConfigOptions;

import java.util.List;

public class CustomIbftValidator {
  private final List<String> allowList;

  public CustomIbftValidator(final CustomIbftConfigOptions ibftConfigOptions) {
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
