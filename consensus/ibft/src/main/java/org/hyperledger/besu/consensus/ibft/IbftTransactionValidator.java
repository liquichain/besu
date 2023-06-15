package org.hyperledger.besu.consensus.ibft;

import org.hyperledger.besu.config.CustomIbftConfigOptions;
import org.hyperledger.besu.consensus.ibft.validation.CustomIbftValidator;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.ethereum.GasLimitCalculator;
import org.hyperledger.besu.ethereum.core.Transaction;
import org.hyperledger.besu.ethereum.mainnet.MainnetTransactionValidator;
import org.hyperledger.besu.ethereum.mainnet.TransactionValidationParams;
import org.hyperledger.besu.ethereum.mainnet.ValidationResult;
import org.hyperledger.besu.ethereum.transaction.TransactionInvalidReason;
import org.hyperledger.besu.evm.gascalculator.GasCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public class IbftTransactionValidator extends MainnetTransactionValidator {
  private static final Logger LOG =
      LoggerFactory.getLogger(IbftTransactionValidator.class);

  private final CustomIbftValidator validator;

  public IbftTransactionValidator(final CustomIbftValidator validator,
                                  final GasCalculator gasCalculator,
                                  final GasLimitCalculator gasLimitCalculator,
                                  final boolean checkSignatureMalleability,
                                  final Optional<BigInteger> chainId) {
    super(gasCalculator, gasLimitCalculator, checkSignatureMalleability, chainId);
    this.validator = validator;
  }

  @Override
  public ValidationResult<TransactionInvalidReason> validate(final Transaction transaction,
                                                             final Optional<Wei> baseFee,
                                                             final TransactionValidationParams transactionValidationParams) {

    List<String> allowList = validator.getAllowListContractAddresses();
    LOG.info(allowList.toString());
    if (allowList != null && !allowList.isEmpty()) {
      Optional<Address> contractAddress = transaction.getTo();
      final Optional<String> matchAddress = allowList.stream().filter(address -> address.equals(contractAddress.get().toString())).findAny();
      LOG.info(contractAddress.toString());
      LOG.info(matchAddress.get());
      if (matchAddress.isEmpty()) {
        LOG.info(TransactionInvalidReason.INVALID_CONTRACT_ADDRESS + "Result");
        ValidationResult<TransactionInvalidReason> result = ValidationResult.invalid(TransactionInvalidReason.INVALID_CONTRACT_ADDRESS, "Contract address does not appear in allow list");
        LOG.debug(result.toString());
        return result;
      }
    }
    return super.validate(transaction, baseFee, transactionValidationParams);
  }

  public ValidationResult<TransactionInvalidReason> validateContractAddress(final Address contractAddress, final List<String> allowList) {
    LOG.info(allowList.toString());
    if (allowList != null && !allowList.isEmpty()) {
      final Optional<String> matchAddress = allowList.stream().filter(address -> address.equals(contractAddress.toString())).findAny();
      LOG.info(contractAddress.toString());
      LOG.info(matchAddress.get());
      if (matchAddress.isEmpty()) {
        LOG.info(TransactionInvalidReason.INVALID_CONTRACT_ADDRESS + "Result");
        ValidationResult<TransactionInvalidReason> result = ValidationResult.invalid(TransactionInvalidReason.INVALID_CONTRACT_ADDRESS, "Contract address does not appear in allow list");
        LOG.debug(result.toString());
        return result;
      }
    }
    return ValidationResult.valid();
  }
}
