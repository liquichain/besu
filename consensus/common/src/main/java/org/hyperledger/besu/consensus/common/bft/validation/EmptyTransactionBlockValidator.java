package org.hyperledger.besu.consensus.common.bft.validation;


import org.hyperledger.besu.ethereum.BlockProcessingResult;
import org.hyperledger.besu.ethereum.MainnetBlockValidator;
import org.hyperledger.besu.ethereum.ProtocolContext;
import org.hyperledger.besu.ethereum.chain.BadBlockManager;
import org.hyperledger.besu.ethereum.core.Block;
import org.hyperledger.besu.ethereum.core.BlockBody;
import org.hyperledger.besu.ethereum.mainnet.BlockBodyValidator;
import org.hyperledger.besu.ethereum.mainnet.BlockHeaderValidator;
import org.hyperledger.besu.ethereum.mainnet.BlockProcessor;
import org.hyperledger.besu.ethereum.mainnet.HeaderValidationMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmptyTransactionBlockValidator extends MainnetBlockValidator {
    private static final Logger LOG =
            LoggerFactory.getLogger(EmptyTransactionBlockValidator.class);

    public EmptyTransactionBlockValidator(BlockHeaderValidator blockHeaderValidator, BlockBodyValidator blockBodyValidator, BlockProcessor blockProcessor, BadBlockManager badBlockManager) {
        super(blockHeaderValidator, blockBodyValidator, blockProcessor, badBlockManager);
    }

    @Override
    public BlockProcessingResult validateAndProcessBlock(
            final ProtocolContext context,
            final Block block,
            final HeaderValidationMode headerValidationMode,
            final HeaderValidationMode ommerValidationMode,
            final boolean shouldPersist,
            final boolean shouldRecordBadBlock) {

        final BlockBody blockBody = block.getBody();

        if (blockBody.getTransactions().isEmpty()) {
            return new BlockProcessingResult("Block has no transaction");
        }

        return super.validateAndProcessBlock(context, block, headerValidationMode, ommerValidationMode, shouldPersist, shouldRecordBadBlock);
    }
}
