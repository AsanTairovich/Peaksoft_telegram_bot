package com.example.peaksoft_telegram_bot.model.enums;

import com.vdurmont.emoji.EmojiParser;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Emojis {
    EARTH_ASIA(EmojiParser.parseToUnicode(":earth_asia:")),
    TROPHY(EmojiParser.parseToUnicode(":trophy:")),
    AMBULANCE(EmojiParser.parseToUnicode(":ambulance:")),
    COMPUTER(EmojiParser.parseToUnicode(":computer:"));

    private String emojiName;

    @Override
    public String toString() {
        return emojiName;
    }
}
