package org.hyperledger.besu.consensus.ibft;

import org.hyperledger.besu.consensus.ibft.validation.LiquichainIBFTValidator;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.ethereum.GasLimitCalculator;
import org.hyperledger.besu.ethereum.core.Transaction;
import org.hyperledger.besu.ethereum.mainnet.MainnetTransactionValidator;
import org.hyperledger.besu.ethereum.mainnet.TransactionValidationParams;
import org.hyperledger.besu.ethereum.mainnet.ValidationResult;
import org.hyperledger.besu.ethereum.mainnet.feemarket.FeeMarket;
import org.hyperledger.besu.ethereum.transaction.TransactionInvalidReason;
import org.hyperledger.besu.evm.account.Account;
import org.hyperledger.besu.evm.gascalculator.GasCalculator;
import org.hyperledger.besu.plugin.data.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LiquichainIBFTTransactionValidator extends MainnetTransactionValidator {
  private static final Logger LOG =
      LoggerFactory.getLogger(LiquichainIBFTTransactionValidator.class);

  private final LiquichainIBFTValidator validator;

  public LiquichainIBFTTransactionValidator(
      final LiquichainIBFTValidator validator,
      final GasCalculator gasCalculator,
      final GasLimitCalculator gasLimitCalculator,
      final boolean checkSignatureMalleability,
      final Optional<BigInteger> chainId) {
    this(
        validator,
        gasCalculator,
        gasLimitCalculator,
        checkSignatureMalleability,
        chainId,
        Set.of(TransactionType.FRONTIER));
  }

  public LiquichainIBFTTransactionValidator(
      final LiquichainIBFTValidator validator,

      final GasCalculator gasCalculator,
      final GasLimitCalculator gasLimitCalculator,
      final boolean checkSignatureMalleability,
      final Optional<BigInteger> chainId,
      final Set<TransactionType> acceptedTransactionTypes) {
    this(
        validator,
        gasCalculator,
        gasLimitCalculator,
        FeeMarket.legacy(),
        checkSignatureMalleability,
        chainId,
        acceptedTransactionTypes,
        Integer.MAX_VALUE);
  }

  public LiquichainIBFTTransactionValidator(
      final LiquichainIBFTValidator validator,
      final GasCalculator gasCalculator,
      final GasLimitCalculator gasLimitCalculator,
      final FeeMarket feeMarket,
      final boolean checkSignatureMalleability,
      final Optional<BigInteger> chainId,
      final Set<TransactionType> acceptedTransactionTypes,
      final int maxInitcodeSize) {
    super(gasCalculator, gasLimitCalculator, feeMarket, checkSignatureMalleability, chainId, acceptedTransactionTypes, maxInitcodeSize);
    this.validator = validator;
  }

  @Override
  public ValidationResult<TransactionInvalidReason> validateForTo(final Transaction transaction, final Account to, final TransactionValidationParams validationParams) {
    LOG.info("Start Liquichain Transaction Process");
    if (!transaction.isContractCreation()) {
      if (to.hasCode()) {
        ValidationResult<TransactionInvalidReason> validationResult = validateContractAddress(to.getAddress());
        if (!validationResult.isValid()) {
          return validationResult;
        }
      }
    }
    return super.validateForTo(transaction, to, validationParams);
  }


  public ValidationResult<TransactionInvalidReason> validateContractAddress(final Address contractAddress) {
    List<String> whiteList = validator.getSmartContractWhiteList();
    if (whiteList != null && !whiteList.isEmpty()) {
      final Optional<String> matchAddress = whiteList.stream().filter(address -> address.equals(contractAddress.toString())).findAny();
      if (matchAddress.isEmpty()) {
        ValidationResult<TransactionInvalidReason> result = ValidationResult.invalid(TransactionInvalidReason.INVALID_CONTRACT_ADDRESS, "Contract address does not appear in allow list");
        return result;
      }
    }

    List<String> blackList = validator.getSmartContractBlackList();
    if (blackList != null && !blackList.isEmpty()) {
      final Optional<String> matchAddress = blackList.stream().filter(address -> address.equals(contractAddress.toString())).findAny();
      if (matchAddress.isPresent()) {
        ValidationResult<TransactionInvalidReason> result = ValidationResult.invalid(TransactionInvalidReason.INVALID_CONTRACT_ADDRESS, "Contract address does not appear in allow list");
        return result;
      }
    }
    return ValidationResult.valid();
  }
}
