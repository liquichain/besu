package org.hyperledger.besu.consensus.common.bft.validation;

import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.ethereum.mainnet.ValidationResult;
import org.hyperledger.besu.ethereum.transaction.TransactionInvalidReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class IbftTransactionValidator {
  private static final Logger LOG =
      LoggerFactory.getLogger(IbftTransactionValidator.class);

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
