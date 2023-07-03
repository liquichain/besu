package org.hyperledger.besu.consensus.ibft.enums;

public enum LiquichainIBFTAllowListType {
  WHITE_LIST("whitelist"),
  BLACK_LIST("blacklist");

  private final String value;

  LiquichainIBFTAllowListType(final String type) {
    this.value = type;
  }

  public String getValue() {
    return value;
  }

  public static LiquichainIBFTAllowListType fromString(final String value) {
    for (final LiquichainIBFTAllowListType type : LiquichainIBFTAllowListType.values()) {
      if (type.getValue().equalsIgnoreCase(value)) {
        return type;
      }
    }
    return null;
  }
}
