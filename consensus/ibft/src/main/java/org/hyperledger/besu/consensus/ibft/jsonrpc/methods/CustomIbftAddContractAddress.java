package org.hyperledger.besu.consensus.ibft.jsonrpc.methods;

import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.ethereum.api.jsonrpc.RpcMethod;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.JsonRpcRequestContext;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.methods.JsonRpcMethod;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcSuccessResponse;

public class CustomIbftAddContractAddress implements JsonRpcMethod {

  @Override
  public String getName() {
    return RpcMethod.IBFT_ADD_CONTRACT_ADDRESS.getMethodName();
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequestContext requestContext) {
//    final Address contractAddress = requestContext.getRequiredParameter(0, Address.class);
    final Boolean add = requestContext.getRequiredParameter(1, Boolean.class);

    if (add) {

    }
    else {

    }

    return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), true);
  }
}
