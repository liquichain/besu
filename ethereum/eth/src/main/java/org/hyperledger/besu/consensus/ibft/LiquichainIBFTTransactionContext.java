package org.hyperledger.besu.consensus.ibft;

import org.hyperledger.besu.ethereum.ConsensusContext;

public class LiquichainIBFTTransactionContext implements ConsensusContext {
  private final LiquichainIBFTValidationProvider validationProvider;

  public LiquichainIBFTTransactionContext(final LiquichainIBFTValidationProvider validationProvider) {
    this.validationProvider = validationProvider;
  }

  @Override
  public <C extends ConsensusContext> C as(Class<C> klass) {
    return klass.cast(this);
  }
  public LiquichainIBFTValidationProvider getValidationProvider() {
    return this.validationProvider;
  }
}
