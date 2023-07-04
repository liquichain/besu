package org.hyperledger.besu.consensus.ibft.jsonrpc.methods;

import org.hyperledger.besu.consensus.ibft.LiquichainIBFTValidationProvider;
import org.hyperledger.besu.consensus.ibft.enums.LiquichainIBFTAllowListType;
import org.hyperledger.besu.ethereum.api.jsonrpc.RpcMethod;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.JsonRpcRequestContext;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.methods.JsonRpcMethod;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcError;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcErrorResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcSuccessResponse;

public class LiquichainIBFTAddContractAddress implements JsonRpcMethod {

  private final LiquichainIBFTValidationProvider validationProvider;

  public LiquichainIBFTAddContractAddress(final LiquichainIBFTValidationProvider validationProvider) {
    this.validationProvider = validationProvider;
  }

  @Override
  public String getName() {
    return RpcMethod.IBFT_ADD_CONTRACT_ADDRESS.getMethodName();
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequestContext requestContext) {
    final LiquichainIBFTAllowListType type = LiquichainIBFTAllowListType.fromString(requestContext.getRequiredParameter(0, String.class));
    final String contractAddress = requestContext.getRequiredParameter(1, String.class);
    final Boolean add = requestContext.getRequiredParameter(2, Boolean.class);

    if (type == null) {
      return new JsonRpcErrorResponse(requestContext.getRequest().getId(), JsonRpcError.INVALID_LIST_TYPE);
    }
    if (type.equals(LiquichainIBFTAllowListType.BLACK_LIST)) {
      validationProvider.updateSmartContractBlackList(contractAddress, add);
    } else if (type.equals(LiquichainIBFTAllowListType.WHITE_LIST)) {
      validationProvider.updateSmartContractWhiteList(contractAddress, add);
    }
    return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), true);
  }
}
