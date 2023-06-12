package org.hyperledger.besu.config;

import java.util.List;

public interface CustomIbftConfigOptions extends BftConfigOptions {
  List<String> getAllowListContractAddresses();

}
