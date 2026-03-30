package com.example.maohi.bot;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.concurrent.ThreadLocalRandom;

public class BotConnection extends ServerGamePacketListenerImpl {

    private final Connection dummyConn;
    private final int fakePing = ThreadLocalRandom.current().nextInt(30, 180);

    public BotConnection(MinecraftServer server, BotPlayer player) {
        super(server, makeDummy(), player,
            CommonListenerCookie.createInitial(player.getGameProfile(), false));
        this.dummyConn = makeDummy();
    }

    private static Connection makeDummy() {
        return new Connection(PacketFlow.SERVERBOUND) {
            @Override public boolean isConnected() { return true; }
            @Override public void send(Packet<?> p) { /* 丢弃 */ }
        };
    }

    /** 给 BotPlayer.spawn() 用，传给 placeNewPlayer */
    public Connection getConnection() { return dummyConn; }

    @Override public int latency() { return fakePing; }
    @Override public void tick() { /* 不处理包 */ }
    @Override public boolean isAcceptingMessages() { return true; }

    // 不覆盖 onDisconnect —— 让父类处理即可，避免签名不匹配的编译错误
}
