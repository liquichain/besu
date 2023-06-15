package org.hyperledger.besu.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kotlin.jvm.internal.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonCustomIbftConfigOptions extends JsonBftConfigOptions implements CustomIbftConfigOptions {
  public static final JsonCustomIbftConfigOptions DEFAULT =
      new JsonCustomIbftConfigOptions(JsonUtil.createEmptyObjectNode());

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
  public JsonCustomIbftConfigOptions(final ObjectNode bftConfigRoot) {
    super(bftConfigRoot);
    mapper = JsonUtil.getObjectMapper();
  }

  @Override
  public List<String> getAllowListContractAddresses() {

    ArrayNode node = JsonUtil.getArrayNode(this.bftConfigRoot, CONTRACT_ADDRESSES_KEY).get();
    try {
      allowList = mapper.treeToValue(node, mapper.getTypeFactory().constructCollectionType(List.class, String.class));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    return allowList;
  }
}
