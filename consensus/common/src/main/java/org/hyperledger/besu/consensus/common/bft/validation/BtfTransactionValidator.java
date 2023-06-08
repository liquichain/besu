package org.hyperledger.besu.consensus.common.bft.validation;

import org.hyperledger.besu.ethereum.GasLimitCalculator;
import org.hyperledger.besu.ethereum.mainnet.MainnetTransactionValidator;
import org.hyperledger.besu.ethereum.mainnet.feemarket.FeeMarket;
import org.hyperledger.besu.evm.gascalculator.GasCalculator;
import org.hyperledger.besu.plugin.data.TransactionType;

import java.math.BigInteger;
import java.util.Optional;
import java.util.Set;

public class BtfTransactionValidator extends MainnetTransactionValidator {
    public BtfTransactionValidator(GasCalculator gasCalculator, GasLimitCalculator gasLimitCalculator, boolean checkSignatureMalleability, Optional<BigInteger> chainId) {
        super(gasCalculator, gasLimitCalculator, checkSignatureMalleability, chainId);
    }

    public BtfTransactionValidator(GasCalculator gasCalculator, GasLimitCalculator gasLimitCalculator, boolean checkSignatureMalleability, Optional<BigInteger> chainId, Set<TransactionType> acceptedTransactionTypes) {
        super(gasCalculator, gasLimitCalculator, checkSignatureMalleability, chainId, acceptedTransactionTypes);
    }

    public BtfTransactionValidator(GasCalculator gasCalculator, GasLimitCalculator gasLimitCalculator, FeeMarket feeMarket, boolean checkSignatureMalleability, Optional<BigInteger> chainId, Set<TransactionType> acceptedTransactionTypes, int maxInitcodeSize) {
        super(gasCalculator, gasLimitCalculator, feeMarket, checkSignatureMalleability, chainId, acceptedTransactionTypes, maxInitcodeSize);
    }
}
