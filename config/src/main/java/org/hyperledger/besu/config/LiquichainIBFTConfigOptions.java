package org.hyperledger.besu.config;

import org.hyperledger.besu.datatypes.Address;

import java.util.Collection;

public interface LiquichainIBFTConfigOptions extends BftConfigOptions {
  Collection<Address> getSmartContractWhiteList();

  Collection<Address> getSmartContractBlackList();

  long getNoTransactionBlockPeriodSeconds();
}
