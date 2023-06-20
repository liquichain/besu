package org.hyperledger.besu.consensus.ibft;

import org.hyperledger.besu.config.GenesisConfigOptions;
import org.hyperledger.besu.consensus.ibft.validation.LiquichainIBFTValidator;
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

  public static ProtocolSpecBuilder frontierDefinition(final ProtocolSpecBuilder builder, final LiquichainIBFTValidator validator) {
    return builder.
        transactionValidatorBuilder(
            (gasCalculator, gasLimitCalculator) ->
                new LiquichainIBFTTransactionValidator(validator,
                    gasCalculator, gasLimitCalculator, false, Optional.empty()));
  }

  public static ProtocolSpecBuilder homesteadDefinition(final ProtocolSpecBuilder builder, final LiquichainIBFTValidator validator) {
    return frontierDefinition(builder, validator)
        .transactionValidatorBuilder(
            (gasCalculator, gasLimitCalculator) ->
                new LiquichainIBFTTransactionValidator(validator,
                    gasCalculator, gasLimitCalculator, true, Optional.empty()));
  }

  public static ProtocolSpecBuilder daoRecoveryInitDefinition(final ProtocolSpecBuilder builder, final LiquichainIBFTValidator validator) {
    return homesteadDefinition(builder, validator);
  }

  public static ProtocolSpecBuilder daoRecoveryTransitionDefinition(final ProtocolSpecBuilder builder, final LiquichainIBFTValidator validator) {
    return daoRecoveryInitDefinition(builder, validator);
  }

  public static ProtocolSpecBuilder tangerineWhistleDefinition(final ProtocolSpecBuilder builder, final LiquichainIBFTValidator validator) {
    return homesteadDefinition(builder, validator);
  }

  public static ProtocolSpecBuilder spuriousDragonDefinition(final ProtocolSpecBuilder builder, final Optional<BigInteger> chainId, final LiquichainIBFTValidator validator) {

    return tangerineWhistleDefinition(builder, validator)
        .transactionValidatorBuilder(
            (gasCalculator, gasLimitCalculator) ->
                new LiquichainIBFTTransactionValidator(validator, gasCalculator, gasLimitCalculator, true, chainId));
  }

  public static ProtocolSpecBuilder byzantiumDefinition(final ProtocolSpecBuilder builder, final Optional<BigInteger> chainId, final LiquichainIBFTValidator validator) {
    return spuriousDragonDefinition(builder, chainId, validator);
  }

  public static ProtocolSpecBuilder constantinopleDefinition(final ProtocolSpecBuilder builder, final Optional<BigInteger> chainId, final LiquichainIBFTValidator validator) {
    return byzantiumDefinition(builder, chainId, validator);
  }

  public static ProtocolSpecBuilder petersburgDefinition(final ProtocolSpecBuilder builder, final Optional<BigInteger> chainId, final LiquichainIBFTValidator validator) {
    return constantinopleDefinition(builder, chainId, validator);
  }

  public static ProtocolSpecBuilder istanbulDefinition(final ProtocolSpecBuilder builder, final Optional<BigInteger> chainId, final LiquichainIBFTValidator validator) {
    return petersburgDefinition(builder, chainId, validator);
  }

  public static ProtocolSpecBuilder muirGlacierDefinition(final ProtocolSpecBuilder builder, final Optional<BigInteger> chainId, final LiquichainIBFTValidator validator) {
    return istanbulDefinition(builder, chainId, validator);
  }

  public static ProtocolSpecBuilder berlinDefinition(final ProtocolSpecBuilder builder, final Optional<BigInteger> chainId, final LiquichainIBFTValidator validator) {
    return muirGlacierDefinition(builder, chainId, validator)
        .transactionValidatorBuilder(
            (gasCalculator, gasLimitCalculator) ->
                new LiquichainIBFTTransactionValidator(
                    validator,
                    gasCalculator,
                    gasLimitCalculator,
                    true,
                    chainId,
                    Set.of(TransactionType.FRONTIER, TransactionType.ACCESS_LIST)));
  }

  public static ProtocolSpecBuilder londonDefinition(final ProtocolSpecBuilder builder, final Optional<BigInteger> chainId, final LiquichainIBFTValidator validator) {
    return berlinDefinition(builder, chainId, validator)
        .transactionValidatorBuilder(
            (gasCalculator, gasLimitCalculator) ->
                new LiquichainIBFTTransactionValidator(
                    validator,
                    gasCalculator,
                    gasLimitCalculator,
                    true,
                    chainId,
                    Set.of(
                        TransactionType.FRONTIER,
                        TransactionType.ACCESS_LIST,
                        TransactionType.EIP1559)));
  }

  public static ProtocolSpecBuilder arrowGlacierDefinition(final ProtocolSpecBuilder builder, final Optional<BigInteger> chainId, final LiquichainIBFTValidator validator) {
    return londonDefinition(builder, chainId, validator);
  }

  public static ProtocolSpecBuilder grayGlacierDefinition(final ProtocolSpecBuilder builder, final Optional<BigInteger> chainId, final LiquichainIBFTValidator validator) {
    return arrowGlacierDefinition(builder, chainId, validator);
  }

  public static ProtocolSpecBuilder parisDefinition(final ProtocolSpecBuilder builder, final Optional<BigInteger> chainId, final LiquichainIBFTValidator validator) {
    return grayGlacierDefinition(builder, chainId, validator);
  }

  public static ProtocolSpecBuilder shanghaiDefinition(final ProtocolSpecBuilder builder,
                                                       final Optional<BigInteger> chainId,
                                                       final LiquichainIBFTValidator validator,
                                                       final GenesisConfigOptions genesisConfigOptions) {

    final long londonForkBlockNumber = genesisConfigOptions.getLondonBlockNumber().orElse(0L);
    final BaseFeeMarket londonFeeMarket =
        genesisConfigOptions.isZeroBaseFee()
            ? FeeMarket.zeroBaseFee(londonForkBlockNumber)
            : FeeMarket.london(londonForkBlockNumber, genesisConfigOptions.getBaseFeePerGas());

    return parisDefinition(builder, chainId, validator)
        .transactionValidatorBuilder(
            (gasCalculator, gasLimitCalculator) ->
                new LiquichainIBFTTransactionValidator(
                    validator,
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
                                                     final LiquichainIBFTValidator validator,
                                                     final GenesisConfigOptions genesisConfigOptions) {
    final long londonForkBlockNumber = genesisConfigOptions.getLondonBlockNumber().orElse(0L);
    final BaseFeeMarket cancunFeeMarket =
        genesisConfigOptions.isZeroBaseFee()
            ? FeeMarket.zeroBaseFee(londonForkBlockNumber)
            : FeeMarket.cancun(londonForkBlockNumber, genesisConfigOptions.getBaseFeePerGas());
    return shanghaiDefinition(builder, chainId, validator, genesisConfigOptions).transactionValidatorBuilder(
        (gasCalculator, gasLimitCalculator) ->
            new LiquichainIBFTTransactionValidator(
                validator,
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
                                                         final LiquichainIBFTValidator validator,
                                                         final GenesisConfigOptions genesisConfigOptions) {
    return cancunDefinition(builder, chainId, validator, genesisConfigOptions);
  }

  public static ProtocolSpecBuilder experimentalEipsDefinition(final ProtocolSpecBuilder builder,
                                                               final Optional<BigInteger> chainId,
                                                               final LiquichainIBFTValidator validator,
                                                               final GenesisConfigOptions genesisConfigOptions) {
    return futureEipsDefinition(builder, chainId, validator, genesisConfigOptions);
  }

  public static ProtocolSpecBuilder classicRecoveryInitDefinition(
      final ProtocolSpecBuilder builder,
      final LiquichainIBFTValidator validator) {
    return homesteadDefinition(builder, validator);
  }

  public static ProtocolSpecBuilder classicTangerineWhistleDefinition(final ProtocolSpecBuilder builder,
                                                                      final Optional<BigInteger> chainId,
                                                                      final LiquichainIBFTValidator validator) {
    return homesteadDefinition(builder, validator)
        .transactionValidatorBuilder(
            (gasCalculator, gasLimitCalculator) ->
                new LiquichainIBFTTransactionValidator(validator,
                    gasCalculator,
                    gasLimitCalculator, true,
                    chainId));
  }

  public static ProtocolSpecBuilder classicDieHardDefinition(
      final ProtocolSpecBuilder builder,
      final Optional<BigInteger> chainId,
      final LiquichainIBFTValidator validator) {
    return classicTangerineWhistleDefinition(builder, chainId, validator);
  }

  public static ProtocolSpecBuilder classicGothamDefinition(
      final ProtocolSpecBuilder builder,
      final Optional<BigInteger> chainId,
      final LiquichainIBFTValidator validator) {
    return classicDieHardDefinition(builder, chainId, validator);
  }

  public static ProtocolSpecBuilder classicDefuseDifficultyBombDefinition(
      final ProtocolSpecBuilder builder,
      final Optional<BigInteger> chainId,
      final LiquichainIBFTValidator validator) {
    return classicGothamDefinition(builder, chainId, validator);
  }

  public static ProtocolSpecBuilder classicAtlantisDefinition(
      final ProtocolSpecBuilder builder,
      final Optional<BigInteger> chainId,
      final LiquichainIBFTValidator validator) {
    return classicGothamDefinition(builder, chainId, validator);
  }

  public static ProtocolSpecBuilder classicAghartaDefinition(
      final ProtocolSpecBuilder builder,
      final Optional<BigInteger> chainId,
      final LiquichainIBFTValidator validator) {
    return classicAtlantisDefinition(builder, chainId, validator);
  }

  public static ProtocolSpecBuilder classicPhoenixDefinition(
      final ProtocolSpecBuilder builder,
      final Optional<BigInteger> chainId,
      final LiquichainIBFTValidator validator) {
    return classicAghartaDefinition(builder, chainId, validator);
  }

  public static ProtocolSpecBuilder classicThanosDefinition(
      final ProtocolSpecBuilder builder,
      final Optional<BigInteger> chainId,
      final LiquichainIBFTValidator validator) {
    return classicPhoenixDefinition(builder, chainId, validator);
  }

  public static ProtocolSpecBuilder classicMagnetoDefinition(
      final ProtocolSpecBuilder builder,
      final Optional<BigInteger> chainId,
      final LiquichainIBFTValidator validator) {
    return classicThanosDefinition(builder, chainId, validator)
        .transactionValidatorBuilder(
            (gasCalculator, gasLimitCalculator) ->
                new LiquichainIBFTTransactionValidator(
                    validator,
                    gasCalculator,
                    gasLimitCalculator,
                    true,
                    chainId,
                    Set.of(TransactionType.FRONTIER, TransactionType.ACCESS_LIST)));
  }

  public static ProtocolSpecBuilder classicMystiqueDefinition(
      final ProtocolSpecBuilder builder,
      final Optional<BigInteger> chainId,
      final LiquichainIBFTValidator validator) {
    return classicMagnetoDefinition(builder, chainId, validator);
  }
}
