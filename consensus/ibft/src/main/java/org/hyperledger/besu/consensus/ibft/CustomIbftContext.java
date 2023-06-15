package org.hyperledger.besu.consensus.ibft;

import org.hyperledger.besu.config.CustomIbftConfigOptions;
import org.hyperledger.besu.consensus.common.EpochManager;
import org.hyperledger.besu.consensus.common.bft.BftBlockInterface;
import org.hyperledger.besu.consensus.common.bft.BftContext;
import org.hyperledger.besu.consensus.common.validator.ValidatorProvider;
import org.hyperledger.besu.consensus.ibft.validation.CustomIbftValidator;

import java.util.List;

public class CustomIbftContext extends BftContext {

  private final CustomIbftValidator validator;
  /**
   * Instantiates a new Bft context.
   *
   * @param validatorProvider the validator provider
   * @param epochManager      the epoch manager
   * @param blockInterface    the block interface
   */
  public CustomIbftContext(final CustomIbftValidator validator,
                           final ValidatorProvider validatorProvider,
                           final EpochManager epochManager,
                           final BftBlockInterface blockInterface) {
    super(validatorProvider, epochManager, blockInterface);
    this.validator = validator;
  }

  public CustomIbftValidator getValidator() {
    return this.validator;
  }

}
