package org.hyperledger.besu.consensus.ibft;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.config.LiquichainIBFTConfigOptions;
import org.hyperledger.besu.consensus.common.ConsensusHelpers;
import org.hyperledger.besu.consensus.common.ForksSchedule;
import org.hyperledger.besu.consensus.common.bft.BftExtraData;
import org.hyperledger.besu.consensus.common.bft.BftExtraDataCodec;
import org.hyperledger.besu.consensus.common.bft.blockcreation.BftBlockCreatorFactory;
import org.hyperledger.besu.consensus.common.validator.ValidatorProvider;
import org.hyperledger.besu.consensus.common.validator.blockbased.BlockValidatorProvider;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.ethereum.ProtocolContext;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.MiningParameters;
import org.hyperledger.besu.ethereum.eth.transactions.PendingTransaction;
import org.hyperledger.besu.ethereum.eth.transactions.PendingTransactions;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSchedule;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class LiquichainIBFTBlockCreatorFactory extends BftBlockCreatorFactory<LiquichainIBFTConfigOptions> {

  /**
   * Instantiates a new Bft block creator factory.
   *
   * @param pendingTransactions the pending transactions
   * @param protocolContext     the protocol context
   * @param protocolSchedule    the protocol schedule
   * @param forksSchedule       the forks schedule
   * @param miningParams        the mining params
   * @param localAddress        the local address
   * @param bftExtraDataCodec   the bft extra data codec
   */
  public LiquichainIBFTBlockCreatorFactory(final PendingTransactions pendingTransactions,
                                           final ProtocolContext protocolContext,
                                           final ProtocolSchedule protocolSchedule,
                                           final ForksSchedule<LiquichainIBFTConfigOptions> forksSchedule, MiningParameters miningParams, Address localAddress, BftExtraDataCodec bftExtraDataCodec) {
    super(pendingTransactions, protocolContext, protocolSchedule, forksSchedule, miningParams, localAddress, bftExtraDataCodec);
  }

  @Override
  public Bytes createExtraData(int round, BlockHeader parentHeader) {

    final LiquichainIBFTContext context = protocolContext.getConsensusContext(LiquichainIBFTContext.class);

    final ValidatorProvider validatorProvider = context.getValidatorProvider();

    if (validatorProvider instanceof LiquichainIBFTValidatorProvider) {
      final BftExtraData extraData =
          new BftExtraData(
              ConsensusHelpers.zeroLeftPad(vanityData, BftExtraDataCodec.EXTRA_VANITY_LENGTH),
              Collections.emptyList(),
              Optional.empty(),
              round,
              Collections.emptyList());

      return bftExtraDataCodec.encode(extraData);
    }
    return super.createExtraData(round, parentHeader);
  }
}
