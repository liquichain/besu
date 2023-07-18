package org.hyperledger.besu.consensus.ibft;

import org.hyperledger.besu.consensus.common.bft.BftExtraDataCodec;
import org.hyperledger.besu.consensus.common.bft.BftProtocolSchedule;
import org.hyperledger.besu.consensus.common.bft.ConsensusRoundIdentifier;
import org.hyperledger.besu.consensus.common.bft.blockcreation.BftBlockCreatorFactory;
import org.hyperledger.besu.consensus.common.bft.statemachine.BftFinalState;
import org.hyperledger.besu.consensus.ibft.network.IbftMessageTransmitter;
import org.hyperledger.besu.consensus.ibft.payload.MessageFactory;
import org.hyperledger.besu.consensus.ibft.statemachine.IbftRound;
import org.hyperledger.besu.consensus.ibft.statemachine.RoundState;
import org.hyperledger.besu.consensus.ibft.validation.MessageValidatorFactory;
import org.hyperledger.besu.ethereum.ProtocolContext;
import org.hyperledger.besu.ethereum.blockcreation.BlockCreator;
import org.hyperledger.besu.ethereum.chain.MinedBlockObserver;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.eth.transactions.PendingTransactions;
import org.hyperledger.besu.util.Subscribers;

public class LiquichainIBFTRoundFactory {
  private final BftFinalState finalState;
  private final BftBlockCreatorFactory<?> blockCreatorFactory;
  private final ProtocolContext protocolContext;
  private final BftProtocolSchedule protocolSchedule;
  private final Subscribers<MinedBlockObserver> minedBlockObservers;
  private final MessageValidatorFactory messageValidatorFactory;
  private final MessageFactory messageFactory;
  private final BftExtraDataCodec bftExtraDataCodec;

  private final PendingTransactions pendingTransactions;
  /**
   * Instantiates a new Ibft round factory.
   *
   * @param finalState the final state
   * @param protocolContext the protocol context
   * @param protocolSchedule the protocol schedule
   * @param minedBlockObservers the mined block observers
   * @param messageValidatorFactory the message validator factory
   * @param messageFactory the message factory
   * @param bftExtraDataCodec the bft extra data codec
   */
  public LiquichainIBFTRoundFactory(
      final BftFinalState finalState,
      final ProtocolContext protocolContext,
      final BftProtocolSchedule protocolSchedule,
      final Subscribers<MinedBlockObserver> minedBlockObservers,
      final MessageValidatorFactory messageValidatorFactory,
      final MessageFactory messageFactory,
      final BftExtraDataCodec bftExtraDataCodec,
      final PendingTransactions pendingTransactions) {
    this.finalState = finalState;
    this.blockCreatorFactory = finalState.getBlockCreatorFactory();
    this.protocolContext = protocolContext;
    this.protocolSchedule = protocolSchedule;
    this.minedBlockObservers = minedBlockObservers;
    this.messageValidatorFactory = messageValidatorFactory;
    this.messageFactory = messageFactory;
    this.bftExtraDataCodec = bftExtraDataCodec;
    this.pendingTransactions = pendingTransactions;
  }

  /**
   * Create new ibft round.
   *
   * @param parentHeader the parent header
   * @param round the round
   * @return the ibft round
   */
  public IbftRound createNewRound(final BlockHeader parentHeader, final int round) {
    long nextBlockHeight = parentHeader.getNumber() + 1;
    final ConsensusRoundIdentifier roundIdentifier =
        new ConsensusRoundIdentifier(nextBlockHeight, round);

    final RoundState roundState =
        new RoundState(
            roundIdentifier,
            finalState.getQuorum(),
            messageValidatorFactory.createMessageValidator(roundIdentifier, parentHeader));

    return createNewRoundWithState(parentHeader, roundState);
  }

  /**
   * Create new Ibft round with state.
   *
   * @param parentHeader the parent header
   * @param roundState the round state
   * @return the ibft round
   */
  public IbftRound createNewRoundWithState(
      final BlockHeader parentHeader, final RoundState roundState) {
    final ConsensusRoundIdentifier roundIdentifier = roundState.getRoundIdentifier();
    final BlockCreator blockCreator =
        blockCreatorFactory.create(parentHeader, roundIdentifier.getRoundNumber());

    final IbftMessageTransmitter messageTransmitter =
        new IbftMessageTransmitter(messageFactory, finalState.getValidatorMulticaster());

    return new IbftRound(
        roundState,
        blockCreator,
        protocolContext,
        protocolSchedule.getByBlockNumber(roundIdentifier.getSequenceNumber()).getBlockImporter(),
        minedBlockObservers,
        finalState.getNodeKey(),
        messageFactory,
        messageTransmitter,
        finalState.getRoundTimer(),
        bftExtraDataCodec);
  }
}
