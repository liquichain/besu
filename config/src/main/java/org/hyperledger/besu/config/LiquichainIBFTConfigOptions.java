package org.hyperledger.besu.config;

import java.util.List;

public interface LiquichainIBFTConfigOptions extends BftConfigOptions {
  List<String> getSmartContractWhiteList();

  List<String> getSmartContractBlackList();

  long getNoTransactionBlockPeriodSeconds();
}
