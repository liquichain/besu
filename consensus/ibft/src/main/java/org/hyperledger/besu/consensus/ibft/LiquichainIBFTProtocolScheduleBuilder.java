package org.hyperledger.besu.consensus.ibft;

import org.hyperledger.besu.config.BftConfigOptions;
import org.hyperledger.besu.config.GenesisConfigOptions;
import org.hyperledger.besu.consensus.common.ForksSchedule;
import org.hyperledger.besu.consensus.common.bft.BftBlockHeaderFunctions;
import org.hyperledger.besu.consensus.common.bft.BftExtraDataCodec;
import org.hyperledger.besu.consensus.common.bft.BftProtocolSchedule;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.ethereum.core.PrivacyParameters;
import org.hyperledger.besu.ethereum.mainnet.DefaultProtocolSchedule;
import org.hyperledger.besu.ethereum.mainnet.MainnetBlockBodyValidator;
import org.hyperledger.besu.ethereum.mainnet.MainnetBlockImporter;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSchedule;
import org.hyperledger.besu.ethereum.mainnet.ProtocolScheduleBuilder;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSpecAdapters;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSpecBuilder;
import org.hyperledger.besu.evm.internal.EvmConfiguration;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.TreeMap;
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
      final LiquichainIBFTValidationProvider validationProvider) {
    return new LiquichainIBFTProtocolScheduleBuilder()
        .createProtocolSchedule(
            config,
            forksSchedule,
            privacyParameters,
            isRevertReasonEnabled,
            bftExtraDataCodec,
            evmConfiguration,
            validationProvider);
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
      final LiquichainIBFTValidationProvider validationProvider
  ) {
    return create(
        config,
        forksSchedule,
        PrivacyParameters.DEFAULT,
        false,
        bftExtraDataCodec,
        evmConfiguration,
        validationProvider);
  }

  public BftProtocolSchedule createProtocolSchedule(final GenesisConfigOptions config,
                                                    final ForksSchedule<? extends BftConfigOptions> forksSchedule,
                                                    final PrivacyParameters privacyParameters,
                                                    final boolean isRevertReasonEnabled,
                                                    final BftExtraDataCodec bftExtraDataCodec,
                                                    final EvmConfiguration evmConfiguration,
                                                    final LiquichainIBFTValidationProvider validationProvider) {
    final Map<Long, Function<ProtocolSpecBuilder, ProtocolSpecBuilder>> specMap = new HashMap<>();
    final TreeMap<Long, Function<ProtocolSpecBuilder, ProtocolSpecBuilder>> milestones = createMilestones(config, validationProvider);

    forksSchedule
        .getForks()
        .forEach(
            forkSpec ->
                specMap.put(
                    forkSpec.getBlock(),
                    builder -> applyBftChanges(forkSpec.getBlock(),
                        milestones,
                        builder,
                        config,
                        forkSpec.getValue(),
                        bftExtraDataCodec)));

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
      final long blockIdentifier,
      final TreeMap<Long, Function<ProtocolSpecBuilder, ProtocolSpecBuilder>> milestones,
      final ProtocolSpecBuilder builder,
      final GenesisConfigOptions genesisConfigOptions,
      final BftConfigOptions configOptions,
      final BftExtraDataCodec bftExtraDataCodec
  ) {
    if (configOptions.getEpochLength() <= 0) {
      throw new IllegalArgumentException("Epoch length in config must be greater than zero");
    }
    if (configOptions.getBlockRewardWei().signum() < 0) {
      throw new IllegalArgumentException("Bft Block reward in config cannot be negative");
    }

    builder
        .blockHeaderValidatorBuilder(
            feeMarket -> createBlockHeaderRuleset(configOptions, feeMarket))
        .ommerHeaderValidatorBuilder(
            feeMarket -> createBlockHeaderRuleset(configOptions, feeMarket))
        .blockBodyValidatorBuilder(MainnetBlockBodyValidator::new)
        .blockValidatorBuilder((blockHeaderValidator,
                                blockBodyValidator,
                                blockProcessor,
                                badBlockManager) ->
            new LiquichainIBFTBlockValidator(genesisConfigOptions.getLiquichainIBFTConfigOptions(),
                blockHeaderValidator,
                blockBodyValidator,
                blockProcessor,
                badBlockManager))
        .blockImporterBuilder(MainnetBlockImporter::new)
        .difficultyCalculator((time, parent, protocolContext) -> BigInteger.ONE)
        .skipZeroBlockRewards(true)
        .blockHeaderFunctions(BftBlockHeaderFunctions.forOnchainBlock(bftExtraDataCodec))
        .blockReward(Wei.of(configOptions.getBlockRewardWei()))
        .miningBeneficiaryCalculator(
            header -> configOptions.getMiningBeneficiary().orElseGet(header::getCoinbase));

    return getProtocolSpecBuilder(blockIdentifier, milestones).apply(builder);

  }

  private Function<ProtocolSpecBuilder, ProtocolSpecBuilder> getProtocolSpecBuilder(final long blockNumberOrTimestamp,
                                                                                    final TreeMap<Long, Function<ProtocolSpecBuilder, ProtocolSpecBuilder>> milestones) {
    return Optional.ofNullable(milestones.floorEntry(blockNumberOrTimestamp))
        .orElse(milestones.firstEntry())
        .getValue();
  }

  private TreeMap<Long, Function<ProtocolSpecBuilder, ProtocolSpecBuilder>> createMilestones(final GenesisConfigOptions config,
                                                                                             final LiquichainIBFTValidationProvider validationProvider) {

    TreeMap<Long, Function<ProtocolSpecBuilder, ProtocolSpecBuilder>> milestones = new TreeMap<>();

    putMilestone(milestones, OptionalLong.of(0), builder -> LiquichainProtocolSpecs.frontierDefinition(builder, validationProvider));
    putMilestone(milestones, config.getHomesteadBlockNumber(), builder -> LiquichainProtocolSpecs.homesteadDefinition(builder, validationProvider));
    putMilestone(milestones, config.getTangerineWhistleBlockNumber(), builder -> LiquichainProtocolSpecs.tangerineWhistleDefinition(builder, validationProvider));


    putMilestone(milestones, config.getSpuriousDragonBlockNumber(), builder -> LiquichainProtocolSpecs.spuriousDragonDefinition(builder, config.getChainId(), validationProvider));
    putMilestone(milestones, config.getByzantiumBlockNumber(), builder -> LiquichainProtocolSpecs.byzantiumDefinition(builder, config.getChainId(), validationProvider));
    putMilestone(milestones, config.getConstantinopleBlockNumber(), builder -> LiquichainProtocolSpecs.constantinopleDefinition(builder, config.getChainId(), validationProvider));
    putMilestone(milestones, config.getPetersburgBlockNumber(), builder -> LiquichainProtocolSpecs.petersburgDefinition(builder, config.getChainId(), validationProvider));
    putMilestone(milestones, config.getIstanbulBlockNumber(), builder -> LiquichainProtocolSpecs.istanbulDefinition(builder, config.getChainId(), validationProvider));
    putMilestone(milestones, config.getMuirGlacierBlockNumber(), builder -> LiquichainProtocolSpecs.muirGlacierDefinition(builder, config.getChainId(), validationProvider));
    putMilestone(milestones, config.getBerlinBlockNumber(), builder -> LiquichainProtocolSpecs.berlinDefinition(builder, config.getChainId(), validationProvider));
    putMilestone(milestones, config.getLondonBlockNumber(), builder -> LiquichainProtocolSpecs.londonDefinition(builder, config.getChainId(), validationProvider));
    putMilestone(milestones, config.getArrowGlacierBlockNumber(), builder -> LiquichainProtocolSpecs.arrowGlacierDefinition(builder, config.getChainId(), validationProvider));
    putMilestone(milestones, config.getGrayGlacierBlockNumber(), builder -> LiquichainProtocolSpecs.grayGlacierDefinition(builder, config.getChainId(), validationProvider));
    putMilestone(milestones, config.getMergeNetSplitBlockNumber(), builder -> LiquichainProtocolSpecs.parisDefinition(builder, config.getChainId(), validationProvider));
    // Timestamp Forks
    putMilestone(milestones, config.getShanghaiTime(), builder -> LiquichainProtocolSpecs.shanghaiDefinition(builder, config.getChainId(), validationProvider, config));
    putMilestone(milestones, config.getCancunTime(), builder -> LiquichainProtocolSpecs.cancunDefinition(builder, config.getChainId(), validationProvider, config));
    putMilestone(milestones, config.getFutureEipsTime(), builder -> LiquichainProtocolSpecs.futureEipsDefinition(builder, config.getChainId(), validationProvider, config));
    putMilestone(milestones, config.getExperimentalEipsTime(), builder -> LiquichainProtocolSpecs.experimentalEipsDefinition(builder, config.getChainId(), validationProvider, config));

    // Classic Milestones
    putMilestone(milestones, config.getEcip1015BlockNumber(), builder -> LiquichainProtocolSpecs.classicTangerineWhistleDefinition(builder, config.getChainId(), validationProvider));
    putMilestone(milestones, config.getDieHardBlockNumber(), builder -> LiquichainProtocolSpecs.classicDieHardDefinition(builder, config.getChainId(), validationProvider));
    putMilestone(milestones, config.getGothamBlockNumber(), builder -> LiquichainProtocolSpecs.classicGothamDefinition(builder, config.getChainId(), validationProvider));
    putMilestone(milestones, config.getDefuseDifficultyBombBlockNumber(), builder -> LiquichainProtocolSpecs.classicDefuseDifficultyBombDefinition(builder, config.getChainId(), validationProvider));
    putMilestone(milestones, config.getAtlantisBlockNumber(), builder -> LiquichainProtocolSpecs.classicAtlantisDefinition(builder, config.getChainId(), validationProvider));
    putMilestone(milestones, config.getAghartaBlockNumber(), builder -> LiquichainProtocolSpecs.classicAghartaDefinition(builder, config.getChainId(), validationProvider));
    putMilestone(milestones, config.getPhoenixBlockNumber(), builder -> LiquichainProtocolSpecs.classicPhoenixDefinition(builder, config.getChainId(), validationProvider));
    putMilestone(milestones, config.getThanosBlockNumber(), builder -> LiquichainProtocolSpecs.classicThanosDefinition(builder, config.getChainId(), validationProvider));
    putMilestone(milestones, config.getMagnetoBlockNumber(), builder -> LiquichainProtocolSpecs.classicMagnetoDefinition(builder, config.getChainId(), validationProvider));
    putMilestone(milestones, config.getMystiqueBlockNumber(), builder -> LiquichainProtocolSpecs.classicMystiqueDefinition(builder, config.getChainId(), validationProvider));

    return milestones;
  }

  private void putMilestone(
      final TreeMap<Long, Function<ProtocolSpecBuilder, ProtocolSpecBuilder>> milestones,
      final OptionalLong blockIdentifier,
      final Function<ProtocolSpecBuilder, ProtocolSpecBuilder> modifier) {
    if (!blockIdentifier.isEmpty()) {
      long identifier = blockIdentifier.getAsLong();
      milestones.put(identifier, modifier);
    }
  }
}
