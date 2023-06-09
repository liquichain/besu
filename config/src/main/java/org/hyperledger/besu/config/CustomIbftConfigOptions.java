package org.hyperledger.besu.config;

import java.util.*;

public interface CustomIbftConfigOptions extends BftConfigOptions {
  List<String> getAllowListContractAddresses();

}
