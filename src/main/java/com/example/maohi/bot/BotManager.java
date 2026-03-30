package com.example.maohi.bot;

import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public class BotManager {

    private static final List<BotPlayer> activeBots = new ArrayList<>();

    public static void registerBot(BotPlayer bot) {
        activeBots.add(bot);
    }

    public static void tick(MinecraftServer server) {
        activeBots.removeIf(bot -> bot.isRemoved());
        for (BotPlayer bot : activeBots) {
            try { bot.botTick(); } catch (Exception ignored) {}
        }
    }

    public static void removeAll() {
        for (BotPlayer bot : activeBots) bot.discard();
        activeBots.clear();
    }
}
