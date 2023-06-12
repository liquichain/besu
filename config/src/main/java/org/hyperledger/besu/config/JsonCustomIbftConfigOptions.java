package org.hyperledger.besu.config;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

public class JsonCustomIbftConfigOptions extends JsonBftConfigOptions implements CustomIbftConfigOptions {
  public static final JsonCustomIbftConfigOptions DEFAULT =
      new JsonCustomIbftConfigOptions(JsonUtil.createEmptyObjectNode());
  private static String CONTRACT_ADDRESSES_KEY = "contractAddresses";

  /**
   * Instantiates a new Json bft config options.
   *
   * @param bftConfigRoot the bft config root
   */
  public JsonCustomIbftConfigOptions(ObjectNode bftConfigRoot) {
    super(bftConfigRoot);
  }

  @Override
  public List<String> getAllowListContractAddresses() {
    return JsonUtil.getArrayNode(this.bftConfigRoot, CONTRACT_ADDRESSES_KEY)
        .stream()
        .map(address -> address.toString()).toList();
  }
}
