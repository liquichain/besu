package org.hyperledger.besu.consensus.ibft;

import org.hyperledger.besu.consensus.common.EpochManager;
import org.hyperledger.besu.consensus.common.bft.BftBlockInterface;
import org.hyperledger.besu.consensus.common.bft.BftContext;
import org.hyperledger.besu.consensus.common.validator.ValidatorProvider;
import org.hyperledger.besu.consensus.ibft.validation.LiquichainIBFTValidator;

public class LiquichainIBFTContext extends BftContext {

  private final LiquichainIBFTValidator validator;
  /**
   * Instantiates a new Bft context.
   *
   * @param validatorProvider the validator provider
   * @param epochManager      the epoch manager
   * @param blockInterface    the block interface
   */
  public LiquichainIBFTContext(final LiquichainIBFTValidator validator,
                               final ValidatorProvider validatorProvider,
                               final EpochManager epochManager,
                               final BftBlockInterface blockInterface) {
    super(validatorProvider, epochManager, blockInterface);
    this.validator = validator;
  }

  public LiquichainIBFTValidator getValidator() {
    return this.validator;
  }

}
