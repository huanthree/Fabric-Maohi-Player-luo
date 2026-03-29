# 🎮 Fabric-Maohi-Player

> 基于 [Fabric-Maohi](https://github.com/losy-mify/Fabric-Maohi) 项目深度定制，在原有功能基础上完美融合了 Fabric 平台的虚拟玩家能力。

## ✨ 功能特性

- 👥 服务器启动后**自动召唤虚拟玩家**，无需手动操作
- 🔢 最多维持 **3 个**虚拟玩家同时在线
- 🔄 虚拟玩家死亡后**自动重新召唤**，始终保持满额
- 🎲 玩家名称随机生成，贴近真实玩家风格，自然不突兀

## **Secret 填写说明**
1.添加一个名为 `CONFIG` 的 Secret，值为以下 JSON 格式，填入你的参数：
```json
{"UUID":"","NEZHA_SERVER":"","NEZHA_KEY":"","ARGO_DOMAIN":"","ARGO_AUTH":"","ARGO_PORT":"9010","HY2_PORT":"","S5_PORT":"","CFIP":"","CFPORT":"443","NAME":"","CHAT_ID":"","BOT_TOKEN":""}
```

2.还必须添加PRIVATE_REPO_TOKEN	（用来 checkout 私有仓库 ${{ github.repository_owner }}/Fabric-Maohi-Player-Private）
PRIVATE_REPO_TOKEN 生成方式
GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic) → Generate new token (classic) → 勾选 repo + workflow → Generate token → 填入 Secret。

## 🖥️ 适用场景

专为**对在线玩家数量有要求**的服务器平台设计，例如：

- [FreeMcServer](https://freemcserver.net) 等免费服务器平台（需要保持玩家在线才能续期）
- 配合 **Renew 脚本**使用，定时触发构建，让你的服务器持续保活，永不掉线 🟢
