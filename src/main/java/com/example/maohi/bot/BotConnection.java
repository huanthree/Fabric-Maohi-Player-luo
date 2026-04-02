package com.example.maohi.bot;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.concurrent.ThreadLocalRandom;

public class BotConnection extends ServerGamePacketListenerImpl {

    // 模拟真实玩家的随机延迟（30~180 ms）
    private final int fakePing = ThreadLocalRandom.current().nextInt(30, 180);

    public BotConnection(MinecraftServer server, BotPlayer player) {
        super(server, makeDummyConnection(), player,
              CommonListenerCookie.createInitial(player.getGameProfile(), false));
    }

    // 创建一个永远显示"已连接"的假连接，所有发出的包直接丢弃
    private static Connection makeDummyConnection() {
        return new Connection(PacketFlow.SERVERBOUND) {
            @Override
            public boolean isConnected() { return true; }

            @Override
            public void send(Packet<?> packet) { /* 丢弃，不发送 */ }
        };
    }

    @Override
    public int latency() { return fakePing; }

    @Override
    public void tick() { /* 不处理任何数据包 */ }

    @Override
    public boolean isAcceptingMessages() { return true; }
}
