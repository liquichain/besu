package org.hyperledger.besu.consensus.ibft;

import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.ethereum.chain.Blockchain;
import org.hyperledger.besu.ethereum.core.ProcessableBlockHeader;
import org.hyperledger.besu.ethereum.core.Transaction;
import org.hyperledger.besu.ethereum.core.feemarket.CoinbaseFeePriceCalculator;
import org.hyperledger.besu.ethereum.mainnet.MainnetTransactionProcessor;
import org.hyperledger.besu.ethereum.mainnet.MainnetTransactionValidator;
import org.hyperledger.besu.ethereum.mainnet.TransactionValidationParams;
import org.hyperledger.besu.ethereum.mainnet.ValidationResult;
import org.hyperledger.besu.ethereum.mainnet.feemarket.FeeMarket;
import org.hyperledger.besu.ethereum.privacy.storage.PrivateMetadataUpdater;
import org.hyperledger.besu.ethereum.processing.TransactionProcessingResult;
import org.hyperledger.besu.ethereum.transaction.TransactionInvalidReason;
import org.hyperledger.besu.ethereum.vm.BlockHashLookup;
import org.hyperledger.besu.evm.account.Account;
import org.hyperledger.besu.evm.gascalculator.GasCalculator;
import org.hyperledger.besu.evm.processor.AbstractMessageProcessor;
import org.hyperledger.besu.evm.tracing.OperationTracer;
import org.hyperledger.besu.evm.worldstate.WorldUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class LiquichainIBFTTransactionProcessor extends MainnetTransactionProcessor {
  public LiquichainIBFTTransactionProcessor(final GasCalculator gasCalculator,
                                            final MainnetTransactionValidator transactionValidator,
                                            final AbstractMessageProcessor contractCreationProcessor,
                                            final AbstractMessageProcessor messageCallProcessor, final boolean clearEmptyAccounts,
                                            final boolean warmCoinbase,
                                            final int maxStackSize,
                                            final FeeMarket feeMarket,
                                            final CoinbaseFeePriceCalculator coinbaseFeePriceCalculator) {
    super(gasCalculator, transactionValidator, contractCreationProcessor, messageCallProcessor, clearEmptyAccounts, warmCoinbase, maxStackSize, feeMarket, coinbaseFeePriceCalculator);
  }


  @Override
  public TransactionProcessingResult processTransaction(final Blockchain ignoredBlockchain,
                                                        final WorldUpdater worldState,
                                                        final ProcessableBlockHeader blockHeader,
                                                        final Transaction transaction,
                                                        final Address miningBeneficiary,
                                                        final OperationTracer operationTracer,
                                                        final BlockHashLookup blockHashLookup,
                                                        final Boolean isPersistingPrivateState,
                                                        final TransactionValidationParams transactionValidationParams,
                                                        final PrivateMetadataUpdater privateMetadataUpdater,
                                                        final Wei dataGasPrice) {

    return super.processTransaction(ignoredBlockchain,
        worldState,
        blockHeader,
        transaction,
        miningBeneficiary,
        operationTracer,
        blockHashLookup,
        isPersistingPrivateState,
        transactionValidationParams,
        privateMetadataUpdater,
        dataGasPrice);
  }
}
