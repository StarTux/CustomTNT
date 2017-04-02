package com.winthier.custom_tnt;

enum CustomTNTType {
    MINING("mining_tnt"),
    WOODCUTTING("woodcutting_tnt"),
    NUKE("nuke"),
    SILK("silk_tnt"),
    KINETIC("kinetic_tnt"),
    POWER("power_tnt"),
    INCENDIARY("incendiary_bomb"),
    FRAGMENTATION("fragmentation_bomb"),
    PRESSURE("pressure_bomb");

    public final String key;
    public final String customId;

    CustomTNTType(String customId) {
        this.key = name().toLowerCase();
        this.customId = "tnt:" + customId;
    }
}
