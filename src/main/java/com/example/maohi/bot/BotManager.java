package com.example.maohi.bot;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class BotPlayer extends ServerPlayer {

    // ── 随机名字素材库 ────────────────────────────────────────────────────────
    private static final String[] FIRST = {
        "Steve", "Alex", "Notch", "Dragon", "Enderman", "Wolf",
        "Fox", "Shadow", "Storm", "Blaze", "Diamond", "Emerald", "Hunter",
        "Crafter", "Miner", "Builder", "Knight", "Archer", "Wizard",
        "Dark", "Cool", "Pro", "Epic", "Swift", "Iron", "Gold", "Cyber"
    };
    private static final String[] SUFFIX = {
        "123", "007", "XD", "MC", "HD", "Gaming", "Pro", "99", "42", "2026"
    };

    // ── 随机生成玩家名 ────────────────────────────────────────────────────────
    public static String generateBotName() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        String name;
        int style = r.nextInt(5);
        switch (style) {
            case 0  -> name = FIRST[r.nextInt(FIRST.length)] + r.nextInt(10, 999);
            case 1  -> name = FIRST[r.nextInt(FIRST.length)] + SUFFIX[r.nextInt(SUFFIX.length)];
            case 2  -> name = FIRST[r.nextInt(FIRST.length)] + "_" + FIRST[r.nextInt(FIRST.length)];
            case 3  -> name = (r.nextBoolean() ? "x" : "") + FIRST[r.nextInt(FIRST.length)]
                              + (r.nextBoolean() ? "x" : "");
            default -> name = FIRST[r.nextInt(FIRST.length)];
        }
        // Minecraft 用户名：只允许字母、数字、下划线，长度 3-16
        name = name.replaceAll("[^a-zA-Z0-9_]", "");
        if (name.length() > 16) name = name.substring(0, 16);
        if (name.length() < 3)  name = name + "Bot";
        return name;
    }

    // ── 构造 ─────────────────────────────────────────────────────────────────
    private final BotAI ai;

    public BotPlayer(MinecraftServer server, ServerLevel level, String name) {
        super(server, level, createProfile(name), ClientInformation.createDefault());
        this.ai = new BotAI(this);
        this.setGameMode(GameType.CREATIVE);
    }

    private static GameProfile createProfile(String name) {
        return new GameProfile(generateV4UUID(), name);
    }

    private static UUID generateV4UUID() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        long msb = (r.nextLong() & 0xFFFFFFFFFFFF0FFFL) | 0x0000000000004000L;
        long lsb = (r.nextLong() & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L;
        return new UUID(msb, lsb);
    }

    // ── 每 tick 更新 ─────────────────────────────────────────────────────────
    public void botTick() {
        // 保持满血满食物，永不死亡
        if (this.getHealth() < this.getMaxHealth()) this.setHealth(this.getMaxHealth());
        this.getFoodData().setFoodLevel(20);
        this.getFoodData().setSaturation(20f);
        ai.tick();
    }

    // ── 生成 bot ─────────────────────────────────────────────────────────────
    public static BotPlayer spawn(MinecraftServer server) throws Exception {
        ServerLevel overworld = server.overworld();

        // 每次生成不同的随机名字
        String name = generateBotName();
        BotPlayer bot = new BotPlayer(server, overworld, name);
        bot.setPos(0.5, 64, 0.5);

        // 设置虚拟连接，防止服务器 tick 时 NullPointerException
        bot.connection = new BotConnection(server, bot);

        // 加入世界实体列表
        overworld.addFreshEntity(bot);

        // 通过反射把 bot 加进 PlayerList，
        // 使 getPlayerCount() > 0，服务器不会认为无人在线
        Field playersField = null;
        for (Field f : server.getPlayerList().getClass().getDeclaredFields()) {
            if (f.getType() == List.class) {
                f.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<ServerPlayer> players =
                    (List<ServerPlayer>) f.get(server.getPlayerList());
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

    // ── 访问器 ───────────────────────────────────────────────────────────────
    public BotAI getAI() { return ai; }
}
