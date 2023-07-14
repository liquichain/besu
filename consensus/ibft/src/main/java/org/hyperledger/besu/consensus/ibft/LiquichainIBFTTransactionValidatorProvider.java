package org.hyperledger.besu.consensus.ibft;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.hyperledger.besu.config.QbftConfigOptions;
import org.hyperledger.besu.consensus.common.ForksSchedule;
import org.hyperledger.besu.consensus.common.validator.ValidatorProvider;
import org.hyperledger.besu.consensus.common.validator.VoteProvider;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.ethereum.chain.Blockchain;
import org.hyperledger.besu.ethereum.core.BlockHeader;

import java.util.Collection;
import java.util.Optional;

public class LiquichainIBFTTransactionValidatorProvider implements ValidatorProvider {
  private final Blockchain blockchain;
  private final ForksSchedule<QbftConfigOptions> forksSchedule;
  private final Cache<Long, Collection<Address>> afterBlockValidatorCache =
      CacheBuilder.newBuilder().maximumSize(100).build();
  private final Cache<Long, Collection<Address>> forBlockValidatorCache =
      CacheBuilder.newBuilder().maximumSize(100).build();

  public LiquichainIBFTTransactionValidatorProvider(final Blockchain blockchain,
                                                    final ForksSchedule<QbftConfigOptions> forksSchedule) {
    this.blockchain = blockchain;
    this.forksSchedule = forksSchedule;
  }

  @Override
  public Collection<Address> getValidatorsAtHead() {
    return null;
  }

  @Override
  public Collection<Address> getValidatorsAfterBlock(BlockHeader header) {
    return null;
  }

  @Override
  public Collection<Address> getValidatorsForBlock(BlockHeader header) {
    return null;
  }

  @Override
  public Optional<VoteProvider> getVoteProviderAtHead() {
    return Optional.empty();
  }
}
