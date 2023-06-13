package org.hyperledger.besu.consensus.common.bft.validation;

import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.ethereum.mainnet.ValidationResult;
import org.hyperledger.besu.ethereum.privacy.PrivateTransaction;
import org.hyperledger.besu.ethereum.privacy.PrivateTransactionValidator;
import org.hyperledger.besu.ethereum.transaction.TransactionInvalidReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public class PrivateIbftTransactionValidator extends PrivateTransactionValidator {

  private static final Logger LOG =
      LoggerFactory.getLogger(IbftTransactionValidator.class);
  private final Optional<List<String>> allowListProvider;

  public PrivateIbftTransactionValidator(final Optional<BigInteger> chainId, final Optional<List<String>> allowListProvider) {
    super(chainId);
    this.allowListProvider = allowListProvider;
  }

  @Override
  public ValidationResult<TransactionInvalidReason> validate(final PrivateTransaction transaction, final Long accountNonce, final boolean allowFutureNonces) {
    LOG.info("Start validation on customibft");
    if (!transaction.isContractCreation() && !transaction.contractAddress().isEmpty()) {
      List<String> allowList = allowListProvider.get();
      LOG.info(allowList.toString());
      if (allowList != null && !allowList.isEmpty()) {
        final Address transactionContractAddress = transaction.contractAddress().get();
        final Optional<String> matchAddress = allowList.stream().filter(address -> address.equals(transactionContractAddress.toString())).findAny();
        LOG.info(transactionContractAddress.toString());
        LOG.info(matchAddress.get());
        if (matchAddress.isEmpty() && !transaction.isContractCreation()) {
          return ValidationResult.invalid(TransactionInvalidReason.INVALID_CONTRACT_ADDRESS, "Contract address does not appear in allow list");
        }
      }
    }
    return super.validate(transaction, accountNonce, allowFutureNonces);
  }
}
