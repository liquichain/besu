package org.hyperledger.besu.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonLiquichainIBFTConfigOptions extends JsonBftConfigOptions implements LiquichainIBFTConfigOptions {
  public static final JsonLiquichainIBFTConfigOptions DEFAULT =
      new JsonLiquichainIBFTConfigOptions(JsonUtil.createEmptyObjectNode());

  //  private static final Logger LOG =
//      LoggerFactory.getLogger(JsonCustomIbftConfigOptions.class);
  private static final String SMART_CONTRACT_WHITELIST_KEY = "smartcontractwhitelist";
  private static final String SMART_CONTRACT_BLACKLIST_KEY = "smartcontractblacklist";

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
  public List<String> getSmartContractWhiteList() {
    Optional<ArrayNode> maybeNode = JsonUtil.getArrayNode(this.bftConfigRoot, SMART_CONTRACT_WHITELIST_KEY);

    List<String> whiteList = new ArrayList<>();

    try {
      if (maybeNode.isPresent()) {
        whiteList = mapper.treeToValue(maybeNode.get(), mapper.getTypeFactory().constructCollectionType(List.class, String.class));
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    return whiteList;
  }

  @Override
  public List<String> getSmartContractBlackList() {
    Optional<ArrayNode> maybeNode = JsonUtil.getArrayNode(this.bftConfigRoot, SMART_CONTRACT_BLACKLIST_KEY);
    List<String> blackList = new ArrayList<>();
    try {
      if (maybeNode.isPresent()) {
        blackList = mapper.treeToValue(maybeNode.get(), mapper.getTypeFactory().constructCollectionType(List.class, String.class));
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    return blackList;
  }

}
