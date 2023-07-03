package org.hyperledger.besu.consensus.ibft;

import org.hyperledger.besu.consensus.common.EpochManager;
import org.hyperledger.besu.consensus.common.bft.BftBlockInterface;
import org.hyperledger.besu.consensus.common.bft.BftContext;
import org.hyperledger.besu.consensus.common.validator.ValidatorProvider;

public class LiquichainIBFTContext extends BftContext {

  private final LiquichainIBFTValidationProvider validationProvider;
  /**
   * Instantiates a new Bft context.
   *
   * @param validatorProvider the validator provider
   * @param epochManager      the epoch manager
   * @param blockInterface    the block interface
   */
  public LiquichainIBFTContext(final LiquichainIBFTValidationProvider validationProvider,
                               final ValidatorProvider validatorProvider,
                               final EpochManager epochManager,
                               final BftBlockInterface blockInterface) {
    super(validatorProvider, epochManager, blockInterface);
    this.validationProvider = validationProvider;
  }

  public LiquichainIBFTValidationProvider getValidationProvider() {
    return this.validationProvider;
  }

}
