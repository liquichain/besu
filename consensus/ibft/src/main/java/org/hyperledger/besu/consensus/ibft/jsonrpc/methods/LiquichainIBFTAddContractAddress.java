package org.hyperledger.besu.consensus.ibft.jsonrpc.methods;

import org.hyperledger.besu.consensus.ibft.validation.LiquichainIBFTValidator;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.ethereum.api.jsonrpc.RpcMethod;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.JsonRpcRequestContext;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.methods.JsonRpcMethod;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcError;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcErrorResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcSuccessResponse;

public class LiquichainIBFTAddContractAddress implements JsonRpcMethod {

  private final LiquichainIBFTValidator validator;

  public LiquichainIBFTAddContractAddress(final LiquichainIBFTValidator validator) {
    this.validator = validator;
  }

  @Override
  public String getName() {
    return RpcMethod.IBFT_ADD_CONTRACT_ADDRESS.getMethodName();
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequestContext requestContext) {
    final String type = requestContext.getRequiredParameter(0, String.class).toLowerCase();
    final String contractAddress = requestContext.getRequiredParameter(1, String.class);
    final Boolean add = requestContext.getRequiredParameter(2, Boolean.class);

    if ("blacklist".equals(type)) {
      validator.updateSmartContractBlackList(contractAddress.toString(), add);
    } else if ("whitelist".equals(type)) {
      validator.updateSmartContractWhiteList(contractAddress.toString(), add);
    } else {
      return new JsonRpcErrorResponse(requestContext.getRequest().getId(), JsonRpcError.INVALID_LIST_TYPE);
    }
    return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), true);
  }
}
