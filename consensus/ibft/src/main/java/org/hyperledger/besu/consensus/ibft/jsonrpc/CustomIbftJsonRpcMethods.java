package org.hyperledger.besu.consensus.ibft.jsonrpc;

import org.hyperledger.besu.consensus.ibft.CustomIbftContext;
import org.hyperledger.besu.consensus.ibft.jsonrpc.methods.CustomIbftAddContractAddress;
import org.hyperledger.besu.ethereum.ProtocolContext;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.methods.JsonRpcMethod;

import java.util.Map;

public class CustomIbftJsonRpcMethods extends IbftJsonRpcMethods {

  private final ProtocolContext context;

  /**
   * Instantiates a new Ibft json rpc methods.
   *
   * @param context the context
   */
  public CustomIbftJsonRpcMethods(final ProtocolContext context) {
    super(context);
    this.context = context;
  }

  @Override
  protected Map<String, JsonRpcMethod> create() {

    Map<String, JsonRpcMethod> methods = super.create();
    final CustomIbftContext bftContext = context.getConsensusContext(CustomIbftContext.class);

    JsonRpcMethod addContractMethod = new CustomIbftAddContractAddress(bftContext.getValidator());
    methods.put(addContractMethod.getName(), addContractMethod);
    return methods;

  }
}
