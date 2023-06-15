package org.hyperledger.besu.consensus.ibft.jsonrpc;

import org.hyperledger.besu.consensus.ibft.jsonrpc.methods.CustomIbftAddContractAddress;
import org.hyperledger.besu.ethereum.ProtocolContext;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.methods.JsonRpcMethod;

import java.util.Map;

public class CustomIbftJsonRpcMethods extends IbftJsonRpcMethods {
  /**
   * Instantiates a new Ibft json rpc methods.
   *
   * @param context the context
   */
  public CustomIbftJsonRpcMethods(final ProtocolContext context) {
    super(context);
  }

  @Override
  protected Map<String, JsonRpcMethod> create() {

    Map<String, JsonRpcMethod> methods = super.create();

    JsonRpcMethod addContractMethod = new CustomIbftAddContractAddress();
    methods.put(addContractMethod.getName(), addContractMethod);
    return methods;

  }
}
