package org.hyperledger.besu.consensus.ibft;

import org.hyperledger.besu.config.BftConfigOptions;
import org.hyperledger.besu.config.GenesisConfigOptions;
import org.hyperledger.besu.consensus.common.ForksSchedule;
import org.hyperledger.besu.consensus.common.bft.BftBlockHeaderFunctions;
import org.hyperledger.besu.consensus.common.bft.BftExtraDataCodec;
import org.hyperledger.besu.consensus.common.bft.BftProtocolSchedule;
import org.hyperledger.besu.consensus.ibft.validation.LiquichainIBFTValidator;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.ethereum.core.PrivacyParameters;
import org.hyperledger.besu.ethereum.mainnet.DefaultProtocolSchedule;
import org.hyperledger.besu.ethereum.mainnet.MainnetBlockBodyValidator;
import org.hyperledger.besu.ethereum.mainnet.MainnetBlockImporter;
import org.hyperledger.besu.ethereum.mainnet.MainnetProtocolSpecs;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSchedule;
import org.hyperledger.besu.ethereum.mainnet.ProtocolScheduleBuilder;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSpecAdapters;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSpecBuilder;
import org.hyperledger.besu.evm.internal.EvmConfiguration;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class LiquichainIBFTProtocolScheduleBuilder extends IbftProtocolScheduleBuilder {
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
      final EvmConfiguration evmConfiguration,
      final LiquichainIBFTValidator validator) {
    return new LiquichainIBFTProtocolScheduleBuilder()
        .createProtocolSchedule(
            config,
            forksSchedule,
            privacyParameters,
            isRevertReasonEnabled,
            bftExtraDataCodec,
            evmConfiguration,
            validator);
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
      final EvmConfiguration evmConfiguration,
      final LiquichainIBFTValidator validator
  ) {
    return create(
        config,
        forksSchedule,
        PrivacyParameters.DEFAULT,
        false,
        bftExtraDataCodec,
        evmConfiguration,
        validator);
  }

  public BftProtocolSchedule createProtocolSchedule(final GenesisConfigOptions config,
                                                    final ForksSchedule<? extends BftConfigOptions> forksSchedule,
                                                    final PrivacyParameters privacyParameters,
                                                    final boolean isRevertReasonEnabled,
                                                    final BftExtraDataCodec bftExtraDataCodec,
                                                    final EvmConfiguration evmConfiguration,
                                                    final LiquichainIBFTValidator validator) {
    final Map<Long, Function<ProtocolSpecBuilder, ProtocolSpecBuilder>> specMap = new HashMap<>();

    forksSchedule
        .getForks()
        .forEach(
            forkSpec ->
                specMap.put(
                    forkSpec.getBlock(),
                    builder -> applyBftChanges(config, builder, forkSpec.getValue(), bftExtraDataCodec, validator)));

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
      final BftExtraDataCodec bftExtraDataCodec,
      final LiquichainIBFTValidator validator) {
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
                new LiquichainIBFTTransactionValidator(
                    validator,
                    gasCalculator,
                    gasLimitCalculator,
                    false,
                    config.getChainId()))
        .blockBodyValidatorBuilder(MainnetBlockBodyValidator::new)
        .blockValidatorBuilder(MainnetProtocolSpecs.blockValidatorBuilder())
        .blockImporterBuilder(MainnetBlockImporter::new)
        .difficultyCalculator((time, parent, protocolContext) -> BigInteger.ONE)
        .skipZeroBlockRewards(true)
        .blockHeaderFunctions(BftBlockHeaderFunctions.forOnchainBlock(bftExtraDataCodec))
        .blockReward(Wei.of(configOptions.getBlockRewardWei()))
        .miningBeneficiaryCalculator(
            header -> configOptions.getMiningBeneficiary().orElseGet(header::getCoinbase));
  }

}
