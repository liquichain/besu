package org.hyperledger.besu.consensus.ibft;

import org.hyperledger.besu.consensus.common.bft.Gossiper;
import org.hyperledger.besu.consensus.ibft.messagedata.LiquichainIBFT;
import org.hyperledger.besu.ethereum.eth.manager.EthPeer;
import org.hyperledger.besu.ethereum.p2p.rlpx.connections.PeerConnection;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.Message;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.MessageData;

public class LiquichainIBFTGossip {

  LiquichainIBFTGossip() {

  }

  public void send(final EthPeer peer, MessageData messageData) {
    try {
      peer.send(messageData);
    } catch (PeerConnection.PeerNotConnected e) {
      throw new RuntimeException(e);
    }

  }
}
