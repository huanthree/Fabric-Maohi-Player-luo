package com.example.maohi.bot;

// Bot 的生命周期已完全由 BotManager + Fabric API 事件接管：
//   - 启动时生成：ServerLifecycleEvents.SERVER_STARTED → BotManager.spawnInitialBots()
//   - 每 tick 驱动：ServerTickEvents.END_SERVER_TICK   → BotManager.tick()
//   - 关闭时清理：ServerLifecycleEvents.SERVER_STOPPING → BotManager.removeAll()
//
// 此文件保留仅为避免其他地方引用时编译报错。
public class BotScheduler {
}
