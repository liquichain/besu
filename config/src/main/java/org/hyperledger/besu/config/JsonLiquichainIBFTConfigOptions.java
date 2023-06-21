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
  private static final String CONTRACT_ADDRESSES_KEY = "contractaddresses";
  private final ObjectMapper mapper;
  private List<String> allowList;

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
  public List<String> getAllowListContractAddresses() {
    Optional<ArrayNode> maybeNode = JsonUtil.getArrayNode(this.bftConfigRoot, CONTRACT_ADDRESSES_KEY);


    try {
      if (maybeNode.isEmpty()) {
        allowList = new ArrayList<>();
      } else {
        allowList = mapper.treeToValue(maybeNode.get(), mapper.getTypeFactory().constructCollectionType(List.class, String.class));
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    return allowList;
  }
}
