package org.hyperledger.besu.consensus.ibft;

import org.apache.tuweni.bytes.Bytes32;
import org.hyperledger.besu.config.BftConfigOptions;
import org.hyperledger.besu.consensus.common.ForksSchedule;
import org.hyperledger.besu.consensus.common.bft.BftExtraDataCodec;
import org.hyperledger.besu.consensus.common.bft.blockcreation.BftBlockCreator;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.ethereum.ProtocolContext;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.Transaction;
import org.hyperledger.besu.ethereum.core.Withdrawal;
import org.hyperledger.besu.ethereum.eth.transactions.PendingTransactions;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSchedule;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class LiquichainIBFTBlockCreator extends BftBlockCreator {
  /**
   * Instantiates a new Bft block creator.
   *
   * @param forksSchedule          the forks schedule
   * @param localAddress           the local address
   * @param targetGasLimitSupplier the target gas limit supplier
   * @param extraDataCalculator    the extra data calculator
   * @param pendingTransactions    the pending transactions
   * @param protocolContext        the protocol context
   * @param protocolSchedule       the protocol schedule
   * @param minTransactionGasPrice the min transaction gas price
   * @param minBlockOccupancyRatio the min block occupancy ratio
   * @param parentHeader           the parent header
   * @param bftExtraDataCodec      the bft extra data codec
   */
  public LiquichainIBFTBlockCreator(final ForksSchedule<? extends BftConfigOptions> forksSchedule,
                                    final Address localAddress, Supplier<Optional<Long>> targetGasLimitSupplier,
                                    final ExtraDataCalculator extraDataCalculator,
                                    final PendingTransactions pendingTransactions,
                                    final ProtocolContext protocolContext,
                                    final ProtocolSchedule protocolSchedule,
                                    final Wei minTransactionGasPrice, Double minBlockOccupancyRatio,
                                    final BlockHeader parentHeader,
                                    final BftExtraDataCodec bftExtraDataCodec) {
    super(forksSchedule, localAddress, targetGasLimitSupplier, extraDataCalculator, pendingTransactions, protocolContext, protocolSchedule, minTransactionGasPrice, minBlockOccupancyRatio, parentHeader, bftExtraDataCodec);
  }

  @Override
  protected BlockCreationResult createBlock(Optional<List<Transaction>> maybeTransactions, Optional<List<BlockHeader>> maybeOmmers, Optional<List<Withdrawal>> maybeWithdrawals, Optional<Bytes32> maybePrevRandao, long timestamp, boolean rewardCoinbase) {
    return super.createBlock(maybeTransactions, maybeOmmers, maybeWithdrawals, maybePrevRandao, timestamp, rewardCoinbase);
  }
}
