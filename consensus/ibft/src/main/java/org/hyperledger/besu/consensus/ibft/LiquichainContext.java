package org.hyperledger.besu.consensus.ibft;

import org.hyperledger.besu.ethereum.ConsensusContext;

public class LiquichainContext implements ConsensusContext {

  private final LiquichainIBFTContext ibftContext;
  private final LiquichainIBFTTransactionContext transactionContext;

  public LiquichainContext(final LiquichainIBFTContext ibftContext, final LiquichainIBFTTransactionContext transactionContext) {
    this.ibftContext = ibftContext;
    this.transactionContext = transactionContext;
  }

  @Override
  public <C extends ConsensusContext> C as(final Class<C> klass) {

    if (klass.isInstance(ibftContext)) {
      return klass.cast(ibftContext);
    }
    return klass.cast(transactionContext);
  }
}
