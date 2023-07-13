package org.hyperledger.besu.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hyperledger.besu.datatypes.Address;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

public class JsonLiquichainIBFTConfigOptions extends JsonBftConfigOptions implements LiquichainIBFTConfigOptions {
  public static final JsonLiquichainIBFTConfigOptions DEFAULT = new JsonLiquichainIBFTConfigOptions(
      JsonUtil.createEmptyObjectNode());

  // private static final Logger LOG =
  // LoggerFactory.getLogger(JsonCustomIbftConfigOptions.class);
  private static final String SMART_CONTRACT_WHITELIST_KEY = "smartcontractwhitelist";
  private static final String SMART_CONTRACT_BLACKLIST_KEY = "smartcontractblacklist";

  private static final String MAX_BLOCK_FORGING_DELAY_KEY = "notransactionblockperiodseconds";
  private final ObjectMapper mapper;

  /**
   * Instantiates a new Json bft config options.
   *
   * @param bftConfigRoot the bft config root
   */
  public JsonLiquichainIBFTConfigOptions(final ObjectNode bftConfigRoot) {
    super(bftConfigRoot);
    mapper = JsonUtil.getObjectMapper();
  }

  @Override
  public Collection<Address> getSmartContractWhiteList() {
    Optional<ArrayNode> maybeNode = JsonUtil.getArrayNode(this.bftConfigRoot, SMART_CONTRACT_WHITELIST_KEY);

    Collection<Address> whiteList = new ArrayList<>();

    try {
      if (maybeNode.isPresent()) {
        whiteList = mapper.treeToValue(maybeNode.get(),
            mapper.getTypeFactory().constructCollectionType(Collection.class, Address.class));
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    return whiteList;
  }

  @Override
  public Collection<Address> getSmartContractBlackList() {
    Optional<ArrayNode> maybeNode = JsonUtil.getArrayNode(this.bftConfigRoot, SMART_CONTRACT_BLACKLIST_KEY);
    Collection<Address> blackList = new ArrayList<>();
    try {
      if (maybeNode.isPresent()) {
        blackList = mapper.treeToValue(maybeNode.get(),
            mapper.getTypeFactory().constructCollectionType(Collection.class, Address.class));
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    return blackList;
  }

  @Override
  public long getNoTransactionBlockPeriodSeconds() {
    OptionalLong blockForgingDelay = JsonUtil.getLong(this.bftConfigRoot, MAX_BLOCK_FORGING_DELAY_KEY);
    if (blockForgingDelay.isEmpty()) {
      return 0;
    }
    return blockForgingDelay.getAsLong();
  }
}
