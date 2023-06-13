package org.hyperledger.besu.consensus.common.bft.validation;

import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.ethereum.GasLimitCalculator;
import org.hyperledger.besu.ethereum.core.Transaction;
import org.hyperledger.besu.ethereum.mainnet.MainnetTransactionValidator;
import org.hyperledger.besu.ethereum.mainnet.TransactionValidationParams;
import org.hyperledger.besu.ethereum.mainnet.ValidationResult;
import org.hyperledger.besu.ethereum.transaction.TransactionInvalidReason;
import org.hyperledger.besu.evm.gascalculator.GasCalculator;
import org.hyperledger.besu.plugin.data.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class IbftTransactionValidator extends MainnetTransactionValidator {
  private static final Logger LOG =
      LoggerFactory.getLogger(EmptyTransactionBlockValidator.class);
  private final Optional<List<String>> allowListProvider;

  public IbftTransactionValidator(final Optional<List<String>> allowListProvider,
                                  final GasCalculator gasCalculator,
                                  final GasLimitCalculator gasLimitCalculator,
                                  final boolean checkSignatureMalleability,
                                  final Optional<BigInteger> chainId) {
    super(gasCalculator, gasLimitCalculator, checkSignatureMalleability, chainId, Set.of(TransactionType.FRONTIER));
    this.allowListProvider = allowListProvider;
    LOG.info("CreateIbftTransaction" + allowListProvider.get().toString());
  }

  @Override
  public ValidationResult<TransactionInvalidReason> validate(final Transaction transaction,
                                                             final Optional<Wei> baseFee,
                                                             final TransactionValidationParams transactionValidationParams) {
    List<String> allowList = allowListProvider.get();
    if (allowList != null && !allowList.isEmpty()) {
      final Address transactionContractAddress = transaction.contractAddress().get();
      final Optional<String> matchAddress = allowList.stream().filter(address -> address.equals(transactionContractAddress.toString())).findAny();
      if (matchAddress.isEmpty()) {
        return ValidationResult.invalid(TransactionInvalidReason.INVALID_CONTRACT_ADDRESS, "Contract address does not appear in allow list");
      }
    }
    return ValidationResult.valid();
  }
}
