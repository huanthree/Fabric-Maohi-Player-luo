package com.example.maohi.bot;

import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public class BotManager {

    // 最多同时维持 3 个虚拟玩家
    public static final int MAX_BOTS = 3;

    private static final List<BotPlayer> activeBots = new ArrayList<>();

    // 距离下次生成的冷却计时（单位：tick，20 tick = 1 秒）
    private static int respawnCooldown = 0;

    // ── 注册（外部手动加入时用）────────────────────────────────────────────────
    public static void registerBot(BotPlayer bot) {
        activeBots.add(bot);
    }

    // ── 每 tick 调用（由 Maohi.java 的 ServerTickEvents 驱动）─────────────────
    public static void tick(MinecraftServer server) {
        // 清理已死亡 / 已移除的 bot
        activeBots.removeIf(BotPlayer::isRemoved);

        // 自动补位：不足 3 个时自动重新生成
        if (activeBots.size() < MAX_BOTS) {
            if (respawnCooldown > 0) {
                respawnCooldown--;
            } else {
                try {
                    BotPlayer bot = BotPlayer.spawn(server);
                    activeBots.add(bot);
                    respawnCooldown = 60;  // 成功后等 3 秒再生成下一个
                } catch (Exception e) {
                    respawnCooldown = 200; // 失败后等 10 秒再重试
                }
            }
        }

        // 驱动每个 bot 的 AI
        for (BotPlayer bot : new ArrayList<>(activeBots)) {
            try { bot.botTick(); } catch (Exception ignored) {}
        }
    }

    // ── 服务器启动时批量生成（由 Maohi.java 的 SERVER_STARTED 事件调用）────────
    public static void spawnInitialBots(MinecraftServer server) {
        for (int i = 0; i < MAX_BOTS; i++) {
            try {
                BotPlayer bot = BotPlayer.spawn(server);
                activeBots.add(bot);
                Thread.sleep(400); // 每个 bot 间隔 0.4 秒，避免同时涌入
            } catch (Exception ignored) {
                // 生成失败的槽位会由 tick() 自动补上
            }
        }
    }

    // ── 服务器关闭时清理（由 Maohi.java 的 SERVER_STOPPING 事件调用）──────────
    public static void removeAll() {
        for (BotPlayer bot : new ArrayList<>(activeBots)) {
            try { bot.discard(); } catch (Exception ignored) {}
        }
        activeBots.clear();
    }

    // ── 工具方法 ──────────────────────────────────────────────────────────────
    public static int getBotCount() {
        return activeBots.size();
    }
}
