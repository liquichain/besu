package org.hyperledger.besu.ethereum.eth.transactions;

import org.hyperledger.besu.consensus.ibft.LiquichainIBFTTransactionContext;
import org.hyperledger.besu.consensus.ibft.LiquichainIBFTValidationProvider;
import org.hyperledger.besu.ethereum.ProtocolContext;
import org.hyperledger.besu.ethereum.chain.MutableBlockchain;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.Transaction;
import org.hyperledger.besu.ethereum.eth.manager.EthContext;
import org.hyperledger.besu.ethereum.eth.manager.EthPeer;
import org.hyperledger.besu.ethereum.eth.messages.EthPV65;
import org.hyperledger.besu.evm.account.Account;
import org.hyperledger.besu.evm.worldstate.WorldState;
import org.hyperledger.besu.plugin.data.TransactionType;
import static org.hyperledger.besu.plugin.data.TransactionType.BLOB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class LiquichainIBFTTransactionBroadcaster extends TransactionBroadcaster {
  private static final Logger LOG = LoggerFactory.getLogger(LiquichainIBFTTransactionBroadcaster.class);

  private final LiquichainIBFTValidationProvider validationProvider;
  private final PeerTransactionTracker transactionTracker;

  private final ProtocolContext protocolContext;

  private static final EnumSet<TransactionType> ANNOUNCE_HASH_ONLY_TX_TYPES = EnumSet.of(BLOB);

  private static final Boolean HASH_ONLY_BROADCAST = Boolean.TRUE;
  private static final Boolean FULL_BROADCAST = Boolean.FALSE;

  private final TransactionsMessageSender transactionsMessageSender;
  private final NewPooledTransactionHashesMessageSender newPooledTransactionHashesMessageSender;
  private final EthContext ethContext;

  public LiquichainIBFTTransactionBroadcaster(final ProtocolContext protocolContext,
                                              final EthContext ethContext,
                                              final PendingTransactions pendingTransactions,
                                              final PeerTransactionTracker transactionTracker,
                                              final TransactionsMessageSender transactionsMessageSender,
                                              final NewPooledTransactionHashesMessageSender newPooledTransactionHashesMessageSender) {
    super(ethContext, pendingTransactions, transactionTracker, transactionsMessageSender, newPooledTransactionHashesMessageSender);

    this.transactionsMessageSender = transactionsMessageSender;
    this.newPooledTransactionHashesMessageSender = newPooledTransactionHashesMessageSender;
    this.ethContext = ethContext;
    this.transactionTracker = transactionTracker;
    this.protocolContext = protocolContext;
    this.validationProvider = protocolContext.getConsensusContext(LiquichainIBFTTransactionContext.class).getValidationProvider();
    LOG.info("ValidationProvider " + validationProvider);
  }


  private WorldState getWorldState() {
    final BlockHeader chainHeadBlockHeader = getChainHeadBlockHeader().orElse(null);

    return protocolContext
        .getWorldStateArchive()
        .getMutable(chainHeadBlockHeader, false)
        .orElseThrow();
  }

  private Optional<BlockHeader> getChainHeadBlockHeader() {
    final MutableBlockchain blockchain = protocolContext.getBlockchain();
    return blockchain.getBlockHeader(blockchain.getChainHeadHash());
  }

  @Override
  public void onTransactionsAdded(final Collection<Transaction> transactions) {
    final int currPeerCount = ethContext.getEthPeers().peerCount();
    if (currPeerCount == 0) {
      return;
    }

    final int numPeersToSendFullTransactions = (int) Math.ceil(Math.sqrt(currPeerCount));

    final Map<Boolean, List<Transaction>> transactionByBroadcastMode =
        transactions.stream()
            .collect(
                Collectors.partitioningBy(
                    tx -> ANNOUNCE_HASH_ONLY_TX_TYPES.contains(tx.getType())));

    final List<EthPeer> sendOnlyFullTransactionPeers = new ArrayList<>(currPeerCount);
    final List<EthPeer> sendOnlyHashPeers = new ArrayList<>(currPeerCount);
    final List<EthPeer> sendMixedPeers = new ArrayList<>(currPeerCount);

    ethContext
        .getEthPeers()
        .streamAvailablePeers()
        .forEach(
            peer -> {
              if (peer.hasSupportForMessage(EthPV65.NEW_POOLED_TRANSACTION_HASHES)) {
                sendOnlyHashPeers.add(peer);
              } else {
                sendOnlyFullTransactionPeers.add(peer);
              }
            });

    if (sendOnlyFullTransactionPeers.size() < numPeersToSendFullTransactions) {
      final int delta =
          Math.min(
              numPeersToSendFullTransactions - sendOnlyFullTransactionPeers.size(),
              sendOnlyHashPeers.size());

      Collections.shuffle(sendOnlyHashPeers);

      // move peers from the mixed list to reach the required size for full transaction peers
      movePeersBetweenLists(sendOnlyHashPeers, sendMixedPeers, delta);
    }

    LOG.atTrace()
        .setMessage(
            "Sending full transactions to {} peers, transaction hashes only to {} peers and mixed to {} peers."
                + " Peers w/o eth/65 {}, peers with eth/65 {}")
        .addArgument(sendOnlyFullTransactionPeers::size)
        .addArgument(sendOnlyHashPeers::size)
        .addArgument(sendMixedPeers::size)
        .addArgument(sendOnlyFullTransactionPeers)
        .addArgument(() -> sendOnlyHashPeers.toString() + sendMixedPeers.toString())
        .log();

    sendToFullTransactionsPeers(
        transactionByBroadcastMode.get(FULL_BROADCAST), sendOnlyFullTransactionPeers);

    sendToOnlyHashPeers(transactionByBroadcastMode, sendOnlyHashPeers);

    sendToMixedPeers(transactionByBroadcastMode, sendMixedPeers);
  }

  private void sendToFullTransactionsPeers(
      final List<Transaction> fullBroadcastTransactions, final List<EthPeer> fullTransactionPeers) {
    sendFullTransactions(fullBroadcastTransactions, fullTransactionPeers);
  }

  private void sendToOnlyHashPeers(
      final Map<Boolean, List<Transaction>> txsByHashOnlyBroadcast,
      final List<EthPeer> hashOnlyPeers) {
    final List<Transaction> allTransactions =
        txsByHashOnlyBroadcast.values().stream().flatMap(List::stream).collect(Collectors.toList());

    sendTransactionHashes(allTransactions, hashOnlyPeers);
  }

  private void sendToMixedPeers(
      final Map<Boolean, List<Transaction>> txsByHashOnlyBroadcast,
      final List<EthPeer> mixedPeers) {
    sendFullTransactions(txsByHashOnlyBroadcast.get(FULL_BROADCAST), mixedPeers);
    sendTransactionHashes(txsByHashOnlyBroadcast.get(HASH_ONLY_BROADCAST), mixedPeers);
  }

  private void sendFullTransactions(
      final List<Transaction> transactions, final List<EthPeer> fullTransactionPeers) {
    if (!transactions.isEmpty()) {
      fullTransactionPeers.forEach(
          peer -> {
            transactions.forEach(
                transaction -> {
                  var isAllowed = isTransactionAllowedToSend(peer, transaction);
                  LOG.atInfo().setMessage("Peer {}, Is Allowed To Send Transaction {}")
                      .addArgument(peer.getId())
                      .addArgument(isAllowed).log();
                  if (isAllowed) {
                    transactionTracker.addToPeerSendQueue(peer, transaction);
                  }
                });
            ethContext
                .getScheduler()
                .scheduleSyncWorkerTask(
                    () -> transactionsMessageSender.sendTransactionsToPeer(peer));
          });
    }
  }

  private void sendTransactionHashes(
      final List<Transaction> transactions, final List<EthPeer> transactionHashPeers) {
    if (!transactions.isEmpty()) {
      transactionHashPeers.stream()
          .forEach(
              peer -> {
                transactions.forEach(
                    transaction -> {
                      var isAllowed = isTransactionAllowedToSend(peer, transaction);
                      LOG.atInfo().setMessage("Peer {}, Is Allowed To Send Trasaction {}")
                          .addArgument(peer.getId())
                          .addArgument(isAllowed).log();
                      if (isAllowed) {
                        transactionTracker.addToPeerSendQueue(peer, transaction);
                      }
                    });
                ethContext
                    .getScheduler()
                    .scheduleSyncWorkerTask(
                        () ->
                            newPooledTransactionHashesMessageSender.sendTransactionHashesToPeer(
                                peer));
              });
    }
  }

  private void movePeersBetweenLists(
      final List<EthPeer> sourceList, final List<EthPeer> destinationList, final int num) {

    final int stopIndex = sourceList.size() - num;
    for (int i = sourceList.size() - 1; i >= stopIndex; i--) {
      destinationList.add(sourceList.remove(i));
    }
  }

  private boolean isTransactionAllowedToSend(final EthPeer peer, final Transaction transaction) {

    var worldState = getWorldState();

    if (transaction.getTo().isPresent()) {
      final Optional<Account> toAccount = Optional.ofNullable(worldState.get(transaction.getTo().get()));
      if (toAccount.isPresent() && toAccount.get().hasCode()) {
        return validationProvider.validateBySmartContractList(toAccount.get().getAddress(), Optional.of(peer));
      }
    }

    return true;
  }
}
