package org.hyperledger.besu.consensus.ibft;

import org.hyperledger.besu.config.GenesisConfigOptions;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSpecBuilder;
import org.hyperledger.besu.ethereum.mainnet.feemarket.BaseFeeMarket;
import org.hyperledger.besu.ethereum.mainnet.feemarket.FeeMarket;
import org.hyperledger.besu.plugin.data.TransactionType;

import java.math.BigInteger;
import java.util.Optional;
import java.util.Set;

public class LiquichainProtocolSpecs {

  public static final int SPURIOUS_DRAGON_CONTRACT_SIZE_LIMIT = 24576;
  public static final int SHANGHAI_INIT_CODE_SIZE_LIMIT = 2 * SPURIOUS_DRAGON_CONTRACT_SIZE_LIMIT;

  private LiquichainProtocolSpecs() {
  }

  public static ProtocolSpecBuilder frontierDefinition(final ProtocolSpecBuilder builder, final LiquichainIBFTValidationProvider validationProvider) {
    return builder.
        transactionValidatorBuilder(
            (gasCalculator, gasLimitCalculator) ->
                new LiquichainIBFTTransactionValidator(validationProvider,
                    gasCalculator, gasLimitCalculator, false, Optional.empty()));
  }

  public static ProtocolSpecBuilder homesteadDefinition(final ProtocolSpecBuilder builder, final LiquichainIBFTValidationProvider validationProvider) {
    return frontierDefinition(builder, validationProvider)
        .transactionValidatorBuilder(
            (gasCalculator, gasLimitCalculator) ->
                new LiquichainIBFTTransactionValidator(validationProvider,
                    gasCalculator, gasLimitCalculator, true, Optional.empty()));
  }

  public static ProtocolSpecBuilder daoRecoveryInitDefinition(final ProtocolSpecBuilder builder, final LiquichainIBFTValidationProvider validationProvider) {
    return homesteadDefinition(builder, validationProvider);
  }

  public static ProtocolSpecBuilder daoRecoveryTransitionDefinition(final ProtocolSpecBuilder builder, final LiquichainIBFTValidationProvider validationProvider) {
    return daoRecoveryInitDefinition(builder, validationProvider);
  }

  public static ProtocolSpecBuilder tangerineWhistleDefinition(final ProtocolSpecBuilder builder, final LiquichainIBFTValidationProvider validationProvider) {
    return homesteadDefinition(builder, validationProvider);
  }

  public static ProtocolSpecBuilder spuriousDragonDefinition(final ProtocolSpecBuilder builder, final Optional<BigInteger> chainId, final LiquichainIBFTValidationProvider validationProvider) {

    return tangerineWhistleDefinition(builder, validationProvider)
        .transactionValidatorBuilder(
            (gasCalculator, gasLimitCalculator) ->
                new LiquichainIBFTTransactionValidator(validationProvider, gasCalculator, gasLimitCalculator, true, chainId));
  }

  public static ProtocolSpecBuilder byzantiumDefinition(final ProtocolSpecBuilder builder, final Optional<BigInteger> chainId, final LiquichainIBFTValidationProvider validationProvider) {
    return spuriousDragonDefinition(builder, chainId, validationProvider);
  }

  public static ProtocolSpecBuilder constantinopleDefinition(final ProtocolSpecBuilder builder, final Optional<BigInteger> chainId, final LiquichainIBFTValidationProvider validationProvider) {
    return byzantiumDefinition(builder, chainId, validationProvider);
  }

  public static ProtocolSpecBuilder petersburgDefinition(final ProtocolSpecBuilder builder, final Optional<BigInteger> chainId, final LiquichainIBFTValidationProvider validationProvider) {
    return constantinopleDefinition(builder, chainId, validationProvider);
  }

  public static ProtocolSpecBuilder istanbulDefinition(final ProtocolSpecBuilder builder, final Optional<BigInteger> chainId, final LiquichainIBFTValidationProvider validationProvider) {
    return petersburgDefinition(builder, chainId, validationProvider);
  }

  public static ProtocolSpecBuilder muirGlacierDefinition(final ProtocolSpecBuilder builder, final Optional<BigInteger> chainId, final LiquichainIBFTValidationProvider validationProvider) {
    return istanbulDefinition(builder, chainId, validationProvider);
  }

  public static ProtocolSpecBuilder berlinDefinition(final ProtocolSpecBuilder builder, final Optional<BigInteger> chainId, final LiquichainIBFTValidationProvider validationProvider) {
    return muirGlacierDefinition(builder, chainId, validationProvider)
        .transactionValidatorBuilder(
            (gasCalculator, gasLimitCalculator) ->
                new LiquichainIBFTTransactionValidator(
                    validationProvider,
                    gasCalculator,
                    gasLimitCalculator,
                    true,
                    chainId,
                    Set.of(TransactionType.FRONTIER, TransactionType.ACCESS_LIST)));
  }

  public static ProtocolSpecBuilder londonDefinition(final ProtocolSpecBuilder builder, final Optional<BigInteger> chainId, final LiquichainIBFTValidationProvider validationProvider) {
    return berlinDefinition(builder, chainId, validationProvider)
        .transactionValidatorBuilder(
            (gasCalculator, gasLimitCalculator) ->
                new LiquichainIBFTTransactionValidator(
                    validationProvider,
                    gasCalculator,
                    gasLimitCalculator,
                    true,
                    chainId,
                    Set.of(
                        TransactionType.FRONTIER,
                        TransactionType.ACCESS_LIST,
                        TransactionType.EIP1559)));
  }

  public static ProtocolSpecBuilder arrowGlacierDefinition(final ProtocolSpecBuilder builder, final Optional<BigInteger> chainId, final LiquichainIBFTValidationProvider validationProvider) {
    return londonDefinition(builder, chainId, validationProvider);
  }

  public static ProtocolSpecBuilder grayGlacierDefinition(final ProtocolSpecBuilder builder, final Optional<BigInteger> chainId, final LiquichainIBFTValidationProvider validationProvider) {
    return arrowGlacierDefinition(builder, chainId, validationProvider);
  }

  public static ProtocolSpecBuilder parisDefinition(final ProtocolSpecBuilder builder, final Optional<BigInteger> chainId, final LiquichainIBFTValidationProvider validationProvider) {
    return grayGlacierDefinition(builder, chainId, validationProvider);
  }

  public static ProtocolSpecBuilder shanghaiDefinition(final ProtocolSpecBuilder builder,
                                                       final Optional<BigInteger> chainId,
                                                       final LiquichainIBFTValidationProvider validationProvider,
                                                       final GenesisConfigOptions genesisConfigOptions) {

    final long londonForkBlockNumber = genesisConfigOptions.getLondonBlockNumber().orElse(0L);
    final BaseFeeMarket londonFeeMarket =
        genesisConfigOptions.isZeroBaseFee()
            ? FeeMarket.zeroBaseFee(londonForkBlockNumber)
            : FeeMarket.london(londonForkBlockNumber, genesisConfigOptions.getBaseFeePerGas());

    return parisDefinition(builder, chainId, validationProvider)
        .transactionValidatorBuilder(
            (gasCalculator, gasLimitCalculator) ->
                new LiquichainIBFTTransactionValidator(
                    validationProvider,
                    gasCalculator,
                    gasLimitCalculator,
                    londonFeeMarket,
                    true,
                    chainId,
                    Set.of(
                        TransactionType.FRONTIER,
                        TransactionType.ACCESS_LIST,
                        TransactionType.EIP1559),
                    SHANGHAI_INIT_CODE_SIZE_LIMIT));
  }

  public static ProtocolSpecBuilder cancunDefinition(final ProtocolSpecBuilder builder,
                                                     final Optional<BigInteger> chainId,
                                                     final LiquichainIBFTValidationProvider validationProvider,
                                                     final GenesisConfigOptions genesisConfigOptions) {
    final long londonForkBlockNumber = genesisConfigOptions.getLondonBlockNumber().orElse(0L);
    final BaseFeeMarket cancunFeeMarket =
        genesisConfigOptions.isZeroBaseFee()
            ? FeeMarket.zeroBaseFee(londonForkBlockNumber)
            : FeeMarket.cancun(londonForkBlockNumber, genesisConfigOptions.getBaseFeePerGas());
    return shanghaiDefinition(builder, chainId, validationProvider, genesisConfigOptions).transactionValidatorBuilder(
        (gasCalculator, gasLimitCalculator) ->
            new LiquichainIBFTTransactionValidator(
                validationProvider,
                gasCalculator,
                gasLimitCalculator,
                cancunFeeMarket,
                true,
                chainId,
                Set.of(
                    TransactionType.FRONTIER,
                    TransactionType.ACCESS_LIST,
                    TransactionType.EIP1559,
                    TransactionType.BLOB),
                SHANGHAI_INIT_CODE_SIZE_LIMIT));
  }

  public static ProtocolSpecBuilder futureEipsDefinition(final ProtocolSpecBuilder builder,
                                                         final Optional<BigInteger> chainId,
                                                         final LiquichainIBFTValidationProvider validationProvider,
                                                         final GenesisConfigOptions genesisConfigOptions) {
    return cancunDefinition(builder, chainId, validationProvider, genesisConfigOptions);
  }

  public static ProtocolSpecBuilder experimentalEipsDefinition(final ProtocolSpecBuilder builder,
                                                               final Optional<BigInteger> chainId,
                                                               final LiquichainIBFTValidationProvider validationProvider,
                                                               final GenesisConfigOptions genesisConfigOptions) {
    return futureEipsDefinition(builder, chainId, validationProvider, genesisConfigOptions);
  }

  public static ProtocolSpecBuilder classicRecoveryInitDefinition(
      final ProtocolSpecBuilder builder,
      final LiquichainIBFTValidationProvider validationProvider) {
    return homesteadDefinition(builder, validationProvider);
  }

  public static ProtocolSpecBuilder classicTangerineWhistleDefinition(final ProtocolSpecBuilder builder,
                                                                      final Optional<BigInteger> chainId,
                                                                      final LiquichainIBFTValidationProvider validationProvider) {
    return homesteadDefinition(builder, validationProvider)
        .transactionValidatorBuilder(
            (gasCalculator, gasLimitCalculator) ->
                new LiquichainIBFTTransactionValidator(validationProvider,
                    gasCalculator,
                    gasLimitCalculator, true,
                    chainId));
  }

  public static ProtocolSpecBuilder classicDieHardDefinition(
      final ProtocolSpecBuilder builder,
      final Optional<BigInteger> chainId,
      final LiquichainIBFTValidationProvider validationProvider) {
    return classicTangerineWhistleDefinition(builder, chainId, validationProvider);
  }

  public static ProtocolSpecBuilder classicGothamDefinition(
      final ProtocolSpecBuilder builder,
      final Optional<BigInteger> chainId,
      final LiquichainIBFTValidationProvider validationProvider) {
    return classicDieHardDefinition(builder, chainId, validationProvider);
  }

  public static ProtocolSpecBuilder classicDefuseDifficultyBombDefinition(
      final ProtocolSpecBuilder builder,
      final Optional<BigInteger> chainId,
      final LiquichainIBFTValidationProvider validationProvider) {
    return classicGothamDefinition(builder, chainId, validationProvider);
  }

  public static ProtocolSpecBuilder classicAtlantisDefinition(
      final ProtocolSpecBuilder builder,
      final Optional<BigInteger> chainId,
      final LiquichainIBFTValidationProvider validationProvider) {
    return classicGothamDefinition(builder, chainId, validationProvider);
  }

  public static ProtocolSpecBuilder classicAghartaDefinition(
      final ProtocolSpecBuilder builder,
      final Optional<BigInteger> chainId,
      final LiquichainIBFTValidationProvider validationProvider) {
    return classicAtlantisDefinition(builder, chainId, validationProvider);
  }

  public static ProtocolSpecBuilder classicPhoenixDefinition(
      final ProtocolSpecBuilder builder,
      final Optional<BigInteger> chainId,
      final LiquichainIBFTValidationProvider validationProvider) {
    return classicAghartaDefinition(builder, chainId, validationProvider);
  }

  public static ProtocolSpecBuilder classicThanosDefinition(
      final ProtocolSpecBuilder builder,
      final Optional<BigInteger> chainId,
      final LiquichainIBFTValidationProvider validationProvider) {
    return classicPhoenixDefinition(builder, chainId, validationProvider);
  }

  public static ProtocolSpecBuilder classicMagnetoDefinition(
      final ProtocolSpecBuilder builder,
      final Optional<BigInteger> chainId,
      final LiquichainIBFTValidationProvider validationProvider) {
    return classicThanosDefinition(builder, chainId, validationProvider)
        .transactionValidatorBuilder(
            (gasCalculator, gasLimitCalculator) ->
                new LiquichainIBFTTransactionValidator(
                    validationProvider,
                    gasCalculator,
                    gasLimitCalculator,
                    true,
                    chainId,
                    Set.of(TransactionType.FRONTIER, TransactionType.ACCESS_LIST)));
  }

  public static ProtocolSpecBuilder classicMystiqueDefinition(
      final ProtocolSpecBuilder builder,
      final Optional<BigInteger> chainId,
      final LiquichainIBFTValidationProvider validationProvider) {
    return classicMagnetoDefinition(builder, chainId, validationProvider);
  }
}
