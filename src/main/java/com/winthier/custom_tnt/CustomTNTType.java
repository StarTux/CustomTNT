package com.winthier.custom_tnt;

enum CustomTNTType {
    MINING,
    WOODCUTTING;

    public final String key;
    public final String customId;
    public final float yield = 16.0f;

    CustomTNTType() {
        this.key = name().toLowerCase();
        this.customId = "tnt:" + name().toLowerCase() + "_tnt";
    }
}
