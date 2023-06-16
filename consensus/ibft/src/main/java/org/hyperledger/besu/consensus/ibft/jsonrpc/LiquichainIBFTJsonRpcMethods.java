package org.hyperledger.besu.consensus.ibft.jsonrpc;

import org.hyperledger.besu.consensus.ibft.LiquichainIBFTContext;
import org.hyperledger.besu.consensus.ibft.jsonrpc.methods.LiquichainIBFTAddContractAddress;
import org.hyperledger.besu.ethereum.ProtocolContext;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.methods.JsonRpcMethod;

import java.util.Map;

public class LiquichainIBFTJsonRpcMethods extends IbftJsonRpcMethods {

  private final ProtocolContext context;

  /**
   * Instantiates a new Ibft json rpc methods.
   *
   * @param context the context
   */
  public LiquichainIBFTJsonRpcMethods(final ProtocolContext context) {
    super(context);
    this.context = context;
  }

  @Override
  protected Map<String, JsonRpcMethod> create() {

    Map<String, JsonRpcMethod> methods = super.create();
    final LiquichainIBFTContext bftContext = context.getConsensusContext(LiquichainIBFTContext.class);

    JsonRpcMethod addContractMethod = new LiquichainIBFTAddContractAddress(bftContext.getValidator());
    methods.put(addContractMethod.getName(), addContractMethod);
    return methods;

  }
}
