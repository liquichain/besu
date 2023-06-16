package org.hyperledger.besu.consensus.ibft;

import org.hyperledger.besu.consensus.ibft.validation.LiquichainIBFTValidator;
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

import javax.swing.text.html.Option;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LiquichainIBFTTransactionValidator {
  private static final Logger LOG =
      LoggerFactory.getLogger(LiquichainIBFTTransactionValidator.class);

  private final LiquichainIBFTValidator validator;

  public LiquichainIBFTTransactionValidator(final LiquichainIBFTValidator validator) {
    this.validator = validator;
  }

  public ValidationResult<TransactionInvalidReason> validateContractAddress(final Address contractAddress) {
    List<String> allowList = validator.getAllowListContractAddresses();
    LOG.info(allowList.toString());
    if (allowList != null && !allowList.isEmpty()) {
      final Optional<String> matchAddress = allowList.stream().filter(address -> address.equals(contractAddress.toString())).findAny();
      if (matchAddress.isEmpty()) {
        ValidationResult<TransactionInvalidReason> result = ValidationResult.invalid(TransactionInvalidReason.INVALID_CONTRACT_ADDRESS, "Contract address does not appear in allow list");
        return result;
      }
    }
    return ValidationResult.valid();
  }
}
