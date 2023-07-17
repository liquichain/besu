package org.hyperledger.besu.consensus.ibft;

import org.hyperledger.besu.config.QbftConfigOptions;
import org.hyperledger.besu.consensus.common.ForksSchedule;
import org.hyperledger.besu.consensus.common.validator.ValidatorProvider;
import org.hyperledger.besu.consensus.common.validator.VoteProvider;
import org.hyperledger.besu.consensus.common.validator.blockbased.BlockValidatorProvider;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.ethereum.chain.Blockchain;
import org.hyperledger.besu.ethereum.core.BlockHeader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class LiquichainIBFTValidatorProvider implements ValidatorProvider {
  private final Blockchain blockchain;
  private final ForksSchedule<QbftConfigOptions> forksSchedule;
  private final BlockValidatorProvider blockValidatorProvider;

  public LiquichainIBFTValidatorProvider(final Blockchain blockchain,
                                         final ForksSchedule<QbftConfigOptions> forksSchedule,
                                         final BlockValidatorProvider blockValidatorProvider) {
    this.blockchain = blockchain;
    this.forksSchedule = forksSchedule;
    this.blockValidatorProvider = blockValidatorProvider;
  }

  @Override
  public Collection<Address> getValidatorsAtHead() {
    return blockValidatorProvider.getValidatorsAtHead();
  }

  @Override
  public Collection<Address> getValidatorsAfterBlock(BlockHeader header) {
    return blockValidatorProvider.getValidatorsAfterBlock(header);
  }

  @Override
  public Collection<Address> getValidatorsForBlock(BlockHeader header) {
    return blockValidatorProvider.getValidatorsForBlock(header);
  }

  @Override
  public Optional<VoteProvider> getVoteProviderAtHead() {
    return Optional.empty();
  }

  public Collection<Address> getValidatorsForSmartContract(Address contractAddress) {
    return new ArrayList<>();
  }
}
