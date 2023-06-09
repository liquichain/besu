package org.hyperledger.besu.consensus.common.bft.validation;

import org.hyperledger.besu.datatypes.*;
import org.hyperledger.besu.ethereum.*;
import org.hyperledger.besu.ethereum.core.*;
import org.hyperledger.besu.ethereum.mainnet.*;
import org.hyperledger.besu.ethereum.transaction.*;
import org.hyperledger.besu.evm.gascalculator.*;

import java.math.*;
import java.util.*;

public class IBtfTransactionValidator extends MainnetTransactionValidator {
  private final Optional<List<String>> allowListProvider;

  public IBtfTransactionValidator(Optional<List<String>> allowListProvider, GasCalculator gasCalculator, GasLimitCalculator gasLimitCalculator, boolean checkSignatureMalleability, Optional<BigInteger> chainId) {
    super(gasCalculator, gasLimitCalculator, checkSignatureMalleability, chainId);
    this.allowListProvider = allowListProvider;
  }

  @Override
  public ValidationResult<TransactionInvalidReason> validate(Transaction transaction, Optional<Wei> baseFee, TransactionValidationParams transactionValidationParams) {
    List<String> allowList = allowListProvider.get();
    if (allowList != null && !allowList.isEmpty()) {
      final Address transactionContractAddress = transaction.contractAddress().get();
      final Optional<String> matchAddress = allowList.stream().filter(address -> address.equals(transactionContractAddress.toString())).findAny();
      if (matchAddress.isEmpty()) {
        return ValidationResult.invalid(TransactionInvalidReason.INVALID_CONTRACT_ADDRESS, "Contract address does not appear in allow list");
      }
    }
    return super.validate(transaction, baseFee, transactionValidationParams);
  }
}
