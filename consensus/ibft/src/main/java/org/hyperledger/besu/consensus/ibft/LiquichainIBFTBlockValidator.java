package org.hyperledger.besu.consensus.ibft;


import org.hyperledger.besu.config.LiquichainIBFTConfigOptions;
import org.hyperledger.besu.ethereum.BlockProcessingResult;
import org.hyperledger.besu.ethereum.MainnetBlockValidator;
import org.hyperledger.besu.ethereum.ProtocolContext;
import org.hyperledger.besu.ethereum.chain.BadBlockManager;
import org.hyperledger.besu.ethereum.core.Block;
import org.hyperledger.besu.ethereum.core.BlockBody;
import org.hyperledger.besu.ethereum.core.TransactionReceipt;
import org.hyperledger.besu.ethereum.mainnet.BlockBodyValidator;
import org.hyperledger.besu.ethereum.mainnet.BlockHeaderValidator;
import org.hyperledger.besu.ethereum.mainnet.BlockProcessor;
import org.hyperledger.besu.ethereum.mainnet.HeaderValidationMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class LiquichainIBFTBlockValidator extends MainnetBlockValidator {
  private static final Logger LOG =
      LoggerFactory.getLogger(LiquichainIBFTBlockValidator.class);

  private final LiquichainIBFTConfigOptions configOptions;

  public LiquichainIBFTBlockValidator(
      final LiquichainIBFTConfigOptions configOptions,
      final BlockHeaderValidator blockHeaderValidator,
      final BlockBodyValidator blockBodyValidator,
      final BlockProcessor blockProcessor,
      final BadBlockManager badBlockManager) {
    super(blockHeaderValidator, blockBodyValidator, blockProcessor, badBlockManager);
    this.configOptions = configOptions;
  }

  @Override
  public BlockProcessingResult validateAndProcessBlock(
      final ProtocolContext context,
      final Block block,
      final HeaderValidationMode headerValidationMode,
      final HeaderValidationMode ommerValidationMode,
      final boolean shouldPersist,
      final boolean shouldRecordBadBlock) {

    if (!validateBlock(context, block)) {
      return new BlockProcessingResult("Block has no transaction");
    }

    return super.validateAndProcessBlock(context, block, headerValidationMode, ommerValidationMode, shouldPersist, shouldRecordBadBlock);
  }

  @Override
  public boolean fastBlockValidation(final ProtocolContext context,
                                     final Block block,
                                     final List<TransactionReceipt> receipts,
                                     final HeaderValidationMode headerValidationMode,
                                     final HeaderValidationMode ommerValidationMode) {

    if (!validateBlock(context, block)) {
      return false;
    }
    return super.fastBlockValidation(context, block, receipts, headerValidationMode, ommerValidationMode);
  }


  private boolean validateBlock(final ProtocolContext context,
                                final Block block) {
    final BlockBody blockBody = block.getBody();

    final Optional<Block> maybeLastBlock = context.getBlockchain().getBlockByNumber(block.getHeader().getNumber() - 1l);

    if (blockBody.getTransactions().isEmpty()) {
      if (maybeLastBlock.isPresent()) {
        Block lastBlock = maybeLastBlock.get();
        long diffTimestamp = block.getHeader().getTimestamp() - lastBlock.getHeader().getTimestamp();
        if (diffTimestamp < configOptions.getNoTransactionBlockPeriodSeconds()) {
          return false;
        }
      }
    }
    return true;
  }
}
