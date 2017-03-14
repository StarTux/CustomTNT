package com.winthier.custom_tnt;

enum CustomTNTType {
    MINING("mining_tnt"),
    WOODCUTTING("woodcutting_tnt"),
    NUKE("nuke"),
    SILK("silk_tnt"),
    KINETIC("kinetic_tnt"),
    POWER("power_tnt");

    public final String key;
    public final String customId;

    CustomTNTType(String customId) {
        this.key = name().toLowerCase();
        this.customId = "tnt:" + customId;
    }
}
