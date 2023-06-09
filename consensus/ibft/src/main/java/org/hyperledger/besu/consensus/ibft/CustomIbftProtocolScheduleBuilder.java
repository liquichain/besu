package org.hyperledger.besu.consensus.ibft;

import org.hyperledger.besu.config.*;
import org.hyperledger.besu.consensus.common.*;
import org.hyperledger.besu.consensus.common.bft.*;
import org.hyperledger.besu.consensus.common.bft.validation.*;
import org.hyperledger.besu.datatypes.*;
import org.hyperledger.besu.ethereum.core.*;
import org.hyperledger.besu.ethereum.mainnet.*;
import org.hyperledger.besu.evm.internal.*;

import java.math.*;
import java.util.*;
import java.util.function.*;

public class CustomIbftProtocolScheduleBuilder extends IbftProtocolScheduleBuilder {
  private static final BigInteger DEFAULT_CHAIN_ID = BigInteger.ONE;

  /**
   * Create protocol schedule.
   *
   * @param config                the config
   * @param forksSchedule         the forks schedule
   * @param privacyParameters     the privacy parameters
   * @param isRevertReasonEnabled the is revert reason enabled
   * @param bftExtraDataCodec     the bft extra data codec
   * @param evmConfiguration      the evm configuration
   * @return the protocol schedule
   */
  public static BftProtocolSchedule create(
      final GenesisConfigOptions config,
      final ForksSchedule<BftConfigOptions> forksSchedule,
      final PrivacyParameters privacyParameters,
      final boolean isRevertReasonEnabled,
      final BftExtraDataCodec bftExtraDataCodec,
      final EvmConfiguration evmConfiguration) {
    return new IbftProtocolScheduleBuilder()
        .createProtocolSchedule(
            config,
            forksSchedule,
            privacyParameters,
            isRevertReasonEnabled,
            bftExtraDataCodec,
            evmConfiguration);
  }

  /**
   * Create protocol schedule.
   *
   * @param config            the config
   * @param forksSchedule     the forks schedule
   * @param bftExtraDataCodec the bft extra data codec
   * @param evmConfiguration  the evm configuration
   * @return the protocol schedule
   */
  public static BftProtocolSchedule create(
      final GenesisConfigOptions config,
      final ForksSchedule<BftConfigOptions> forksSchedule,
      final BftExtraDataCodec bftExtraDataCodec,
      final EvmConfiguration evmConfiguration) {
    return create(
        config,
        forksSchedule,
        PrivacyParameters.DEFAULT,
        false,
        bftExtraDataCodec,
        evmConfiguration);
  }

  @Override
  public BftProtocolSchedule createProtocolSchedule(GenesisConfigOptions config, ForksSchedule<? extends BftConfigOptions> forksSchedule, PrivacyParameters privacyParameters, boolean isRevertReasonEnabled, BftExtraDataCodec bftExtraDataCodec, EvmConfiguration evmConfiguration) {
    final Map<Long, Function<ProtocolSpecBuilder, ProtocolSpecBuilder>> specMap = new HashMap<>();

    forksSchedule
        .getForks()
        .forEach(
            forkSpec ->
                specMap.put(
                    forkSpec.getBlock(),
                    builder -> applyBftChanges(config, builder, forkSpec.getValue(), bftExtraDataCodec)));

    final ProtocolSpecAdapters specAdapters = new ProtocolSpecAdapters(specMap);

    final ProtocolSchedule protocolSchedule =
        new ProtocolScheduleBuilder(
            config,
            DEFAULT_CHAIN_ID,
            specAdapters,
            privacyParameters,
            isRevertReasonEnabled,
            evmConfiguration)
            .createProtocolSchedule();

    return new BftProtocolSchedule((DefaultProtocolSchedule) protocolSchedule);
  }

  private ProtocolSpecBuilder applyBftChanges(
      final GenesisConfigOptions config,
      final ProtocolSpecBuilder builder,
      final BftConfigOptions configOptions,
      final BftExtraDataCodec bftExtraDataCodec) {
    if (configOptions.getEpochLength() <= 0) {
      throw new IllegalArgumentException("Epoch length in config must be greater than zero");
    }
    if (configOptions.getBlockRewardWei().signum() < 0) {
      throw new IllegalArgumentException("Bft Block reward in config cannot be negative");
    }

    return builder
        .blockHeaderValidatorBuilder(
            feeMarket -> createBlockHeaderRuleset(configOptions, feeMarket))
        .ommerHeaderValidatorBuilder(
            feeMarket -> createBlockHeaderRuleset(configOptions, feeMarket))
        .transactionValidatorBuilder(
            (gasCalculator, gasLimitCalculator) ->
                new IBtfTransactionValidator(Optional.of(config.getCustomIbftConfigOptions().getAllowListContractAddresses()),
                    gasCalculator, gasLimitCalculator, true, Optional.empty()))
        .blockBodyValidatorBuilder(MainnetBlockBodyValidator::new)
        .blockValidatorBuilder(EmptyTransactionBlockValidator::new)
        .blockImporterBuilder(MainnetBlockImporter::new)
        .difficultyCalculator((time, parent, protocolContext) -> BigInteger.ONE)
        .skipZeroBlockRewards(true)
        .blockHeaderFunctions(BftBlockHeaderFunctions.forOnchainBlock(bftExtraDataCodec))
        .blockReward(Wei.of(configOptions.getBlockRewardWei()))
        .miningBeneficiaryCalculator(
            header -> configOptions.getMiningBeneficiary().orElseGet(header::getCoinbase));
  }

}
