/*
 * Copyright (C) 2024 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatlog.util;

import com.google.gson.JsonObject;
import io.github.darkkronicle.advancedchatcore.chat.ChatMessage;
import io.github.darkkronicle.advancedchatcore.interfaces.IJsonSave;
import io.github.darkkronicle.advancedchatlog.config.ChatLogConfigStorage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.registry.RegistryWrapper;

import static java.awt.SystemColor.text;


@Environment(EnvType.CLIENT)
public class LogChatMessageSerializer implements IJsonSave<LogChatMessage> {

    private DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public LogChatMessageSerializer() {}

    private Style cleanStyle(Style style) {
        if (!ChatLogConfigStorage.General.CLEAN_SAVE.config.getBooleanValue()) {
            return style;
        }
        style = style.withClickEvent(null);
        style = style.withHoverEvent(null);
        style = style.withInsertion(null);
        return style;
    }

    private Text transfer(Text text) {
        Text base = Text.empty();
        for (Text t : text.getSiblings()) {
            Text newT = Text.literal(t.getString()).fillStyle(cleanStyle(t.getStyle()));
            base.getSiblings().add(newT);
        }
        return base;
    }

    @Override
    public LogChatMessage load(JsonObject obj) {
        LocalDateTime dateTime = LocalDateTime.from(formatter.parse(obj.get("time").getAsString()));
        LocalDate date = dateTime.toLocalDate();
        LocalTime time = dateTime.toLocalTime();


        // Deserialize the Text objects
        Text display = Text.Serialization.fromJson(String.valueOf(obj.get("display")), DynamicRegistryManager.EMPTY);
        Text original = Text.Serialization.fromJson(String.valueOf(obj.get("original")), DynamicRegistryManager.EMPTY);

        int stacks = obj.get("stacks").getAsByte();
        ChatMessage message =
                ChatMessage.builder()
                        .time(time)
                        .displayText(display)
                        .originalText(original)
                        .build();
        return new LogChatMessage(message, date);
    }

    @Override
    public JsonObject save(LogChatMessage message) {
        JsonObject json = new JsonObject();
        ChatMessage chat = message.getMessage();
        LocalDateTime dateTime = LocalDateTime.of(message.getDate(), chat.getTime());

        json.addProperty("time", formatter.format(dateTime));
        json.addProperty("stacks", chat.getStacks());

        // Convert Text to JSON and add to the JSON object
        Text display = null;
        json.add("display", new Text.Serializer(BuiltinRegistries.createWrapperLookup()).serialize(display, null, null));
        Text original = null;
        json.add("original", new Text.Serializer(BuiltinRegistries.createWrapperLookup()).serialize(original, null, null));

        return json;
    }
}
