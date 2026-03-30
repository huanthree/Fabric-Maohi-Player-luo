package com.example.maohi.bot;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class BotPlayer extends ServerPlayer {

    private static final String BOT_NAME = "Maohi";
    private static final UUID BOT_UUID = generateV4UUID();
    private final BotAI ai;

    public BotPlayer(MinecraftServer server, ServerLevel level) {
        super(server, level, createProfile(), ClientInformation.createDefault());
        this.ai = new BotAI(this);
        this.setGameMode(GameType.CREATIVE);
    }

    private static UUID generateV4UUID() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        long msb = (r.nextLong() & 0xFFFFFFFFFFFF0FFFL) | 0x0000000000004000L;
        long lsb = (r.nextLong() & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L;
        return new UUID(msb, lsb);
    }

    private static GameProfile createProfile() {
        return new GameProfile(BOT_UUID, BOT_NAME);
    }

    public void botTick() {
        if (this.getHealth() < this.getMaxHealth()) this.setHealth(this.getMaxHealth());
        this.getFoodData().setFoodLevel(20);
        ai.tick();
    }

    public static BotPlayer spawn(MinecraftServer server) throws Exception {
        ServerLevel overworld = server.overworld();
        BotPlayer bot = new BotPlayer(server, overworld);
        bot.setPos(0.5, 64, 0.5);

        // 直接加入世界，不走 placeNewPlayer（避免 Connection null 问题）
        overworld.addFreshEntity(bot);

        // 用反射强制把 bot 加进 PlayerList 的玩家列表
        // 这样 getPlayerCount() 会返回 1，服务器不会暂停
        java.lang.reflect.Field playersField = null;
        for (java.lang.reflect.Field f : server.getPlayerList().getClass().getDeclaredFields()) {
            // 找 List<ServerPlayer> 类型的字段（players 列表）
            if (f.getType() == java.util.List.class) {
                f.setAccessible(true);
                @SuppressWarnings("unchecked")
                java.util.List<ServerPlayer> players = (java.util.List<ServerPlayer>) f.get(server.getPlayerList());
                if (players != null) {
                    players.add(bot);
                    playersField = f;
                    break;
                }
            }
        }

        if (playersField == null) {
            throw new Exception("Could not find players list via reflection");
        }

        return bot;
    }

    public BotAI getAI() { return ai; }
}
