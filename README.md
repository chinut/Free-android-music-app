# 焰火音乐 (Free Android Music App)

一个基于 Kotlin + Jetpack Compose 开发的 Android 音乐播放器，界面简洁现代，功能完整，支持在线听歌、离线缓存、歌单管理、在线电视、电视遥控等多种实用功能。

> 本项目仅供学习交流使用，音源来自第三方公开接口，请勿用于商业用途。

---

## ✨ 功能特性

### 🎵 音乐播放

- 在线搜索歌曲、歌手、专辑
- 支持 QQ 音乐、网易云音乐双音源
- 后台播放，锁屏 / 通知栏控制
- 蓝牙耳机、线控耳机支持（上一曲 / 下一曲 / 播放暂停）
- 单曲循环、列表循环、随机播放、顺序播放四种模式

### 📝 歌词与封面

- 实时滚动歌词，全屏歌词界面
- 通知栏实时显示当前歌词
- 专辑封面自动下载并缓存
- 全屏播放时唱片封面旋转动画

### 💾 本地缓存

- 音频边播边缓存，听过的歌断网也能播
- 歌词、封面本地缓存，秒开无等待
- 缓存上限 500MB，自动淘汰最久未使用
- 支持一键清空所有缓存

### ❤️ 歌单管理

- 创建、重命名、删除歌单
- 收藏歌曲到歌单，一键移出
- 已收藏歌曲显示实心红心
- 歌单分享（生成分享码，好友粘贴导入）

### 📺 在线电视

内置 **34 个分类 / 808 个频道**，数据完全离线，不依赖任何在线接口。

| 分类 | 内容 |
| --- | --- |
| 央视 / 央视源2 | CCTV 全系列 |
| 卫视 | 全国 32 家省级卫视 |
| 少儿 / 教育 | 卡通与教育频道 |
| 各省 | 广东、山东、山西、浙江、江苏、四川、甘肃等地方台 |

- **直连流走原生播放器**，可按域名补 `Referer`，解决一批防盗链频道播不了的问题
- **自动换源**：同一家电视台在多个分类下有不同源，某个源失效时自动依次尝试备用源
- **不退出即可换台**，支持收藏频道与清晰度切换
- 频道列表保留底部导航，仅播放视频时全屏

### 📡 电视遥控

手机当遥控器，通过局域网控制电视上的「焰火TV」。

- **打开即连、无需配置**：先按缓存地址连接，失败则自动扫描局域网找到电视
- **断线自动重连**：连上后每 5 秒心跳
- 方向键 + 确定、返回/菜单、音量 ±/静音、上一集/播放暂停/下一集
- **发送文字到电视**：搜索框免去用遥控器一个个选字母
- 使用前提：手机与电视在同一 WiFi，电视已开启「允许手机调试」

### 🎯 智能推荐

- 首次启动引导选择喜欢的音乐类型和歌手
- 根据播放历史、搜索历史智能推荐
- 偏好之外自动加入 1-2 位"新歌手"，帮助扩展听歌范围
- 支持自定义喜欢的歌手（不受预设列表限制）
- 黑名单功能：可屏蔽指定歌手、风格或关键词

### 🎨 界面与体验

- 深色渐变背景，跟随专辑封面动态变化
- 支持横屏和竖屏自适应布局
- 开屏界面、全屏播放器、迷你播放器
- 全屏播放时屏幕常亮（可关闭）
- 毛玻璃风格背景，现代扁平化设计

### 🔄 自动更新

- 启动后自动检查 GitHub / Gitee 最新版本
- 双源下载，国内优先走 Gitee，GitHub 作为备用
- 应用内下载并一键安装
- 支持手动检查更新

### 📤 分享

- 单曲分享到微信、QQ 等应用
- 歌单分享码，跨设备导入
- 支持微信 / QQ 分享到本软件

---

## 📱 安装方法

### 方式一：直接安装 APK（推荐）

1. 打开本项目的 **Releases** 页面：
   - **Gitee**：<https://gitee.com/chinut/free-android-music-app/releases>
   - **GitHub**：<https://github.com/chinut/Free-android-music-app/releases>
2. 下载最新版本的 `app-release.apk`
3. 在手机上打开 APK 文件安装
4. 首次安装会提示"未知来源"，需要允许安装未知应用
5. 安装完成后即可使用

### 系统要求

| 项目 | 要求 |
| --- | --- |
| Android 版本 | Android 7.0 (API 24) 及以上 |
| 存储空间 | 建议预留 500MB 以上（用于音频缓存） |
| 网络 | 首次使用需要联网 |

---

## 🛠️ 从源码构建

### 环境要求

- Android Studio 最新稳定版
- JDK 21 或以上（本项目构建时使用 JDK 25，见 `gradle/gradle-daemon-jvm.properties`）
- Android SDK API 37

### 编译步骤

```bash
# 1. 克隆项目
git clone https://gitee.com/chinut/free-android-music-app.git

# 2. 用 Android Studio 打开项目

# 3. 等待 Gradle 同步完成

# 4. Build → Generate Signed Bundle / APK
#    选择 APK → 配置签名 → 生成 release 包
```

### 签名配置

生成 release APK 时需要使用 keystore 签名，建议保存好 keystore 文件，后续更新版本必须使用同一个签名。

---

## 📂 项目结构

```
app/src/main/java/com/example/music/
├── MusicApp.kt                  # Application 入口
├── MainActivity.kt              # 主 Activity
├── data/                        # 数据层
│   ├── Song.kt                  # 歌曲数据模型
│   ├── RecommendEngine.kt       # 推荐引擎
│   ├── UserPreferencesStore.kt  # 用户偏好存储
│   ├── api/                     # 网络接口
│   ├── cache/                   # 缓存管理
│   ├── db/                      # Room 数据库（歌单）
│   ├── share/                   # 分享码编解码
│   ├── update/                  # 自动更新
│   └── tv/                      # 在线电视与电视遥控
│       ├── TvCatalog.kt         # 频道目录解析
│       ├── TvAssets.kt          # tv-web 资源读取
│       ├── TvStreamUrl.kt       # 地址归一化 / 站点 UA / 请求头
│       ├── TvSourceMatcher.kt   # 同名频道多源匹配（自动换源）
│       ├── TvWebViewClient.kt   # 资源拦截与站点脚本注入
│       ├── TvWebBridge.kt       # _api 原生桥
│       ├── YanhuoRemote.kt      # 电视遥控协议客户端
│       ├── TvRemoteDiscovery.kt # 局域网自动发现电视
│       └── TvRemotePrefs.kt     # 遥控连接设置
├── player/                      # 播放器
│   ├── PlaybackService.kt       # 后台播放服务
│   ├── PlayerHolder.kt          # 播放控制器
│   └── LrcParser.kt             # 歌词解析
└── ui/                          # 界面
    ├── MusicApp.kt              # 主界面导航
    ├── theme/                   # 主题与背景
    ├── components/              # 通用组件
    └── screens/                 # 各个页面
        ├── HomeScreen.kt        # 首页
        ├── LiveTvScreen.kt      # 在线电视（频道浏览）
        ├── TvPlayerScreen.kt    # 在线电视（播放）
        ├── NativeStreamPlayer.kt# 直连流原生播放器
        ├── TvRemoteScreen.kt    # 电视遥控
        ├── PlaylistScreen.kt    # 歌单管理
        ├── FullPlayerScreen.kt  # 全屏播放器
        ├── SettingsScreen.kt    # 设置
        └── ...

app/src/main/assets/tv-web/      # 在线电视前端资源（移植自 utao，118 个文件）
```

---

## 🧰 技术栈

| 类别 | 技术 |
| --- | --- |
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 播放 | AndroidX Media3 (ExoPlayer + HLS) |
| 在线电视 | WebView + HLS 注入脚本（资源来自 utao） |
| 数据库 | Room |
| 网络 | OkHttp |
| 图片 | Coil |
| 取色 | Palette |
| 协程 | Kotlin Coroutines |
| 导航 | Navigation Compose |
| 构建 | Gradle Kotlin DSL |

---

## 🎨 界面预览

（可以在这里添加应用截图，效果更好）

> 建议截图：首页推荐、全屏播放器、歌单管理、在线电视、电视遥控、设置页

---

## 📋 更新日志

各版本的详细更新内容见 [CHANGELOG.md](CHANGELOG.md)。

---

## ⚠️ 免责声明

1. 本项目**仅供个人学习、研究使用**，请勿用于任何商业用途。
2. 软件中搜索和播放的音乐资源均来自第三方公开接口，本项目不存储、不制作、不分发任何音乐内容。
3. 用户应对自己的使用行为负责。如因使用本软件产生的任何法律纠纷，开发者不承担任何责任。
4. 若第三方接口方有异议，请通过 Issues 联系，我们会及时处理。
5. 请支持正版音乐，尊重音乐人劳动成果。

---

## 📄 开源协议

本项目基于 **MIT License** 开源，详见 [LICENSE](LICENSE) 文件。

---

## 🙏 致谢

- [AndroidX Media3](https://github.com/androidx/media) - 强大的媒体播放框架
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - 现代声明式 UI 框架
- [Coil](https://github.com/coil-kt/coil) - Kotlin 优先的图片加载库
- [Room](https://developer.android.com/training/data-storage/room) - Android 官方 ORM 框架
- [utao / 油桃TV](https://github.com/VonChange/utao) - 在线电视功能的实现思路参考
- <https://www.yinyueku.cn/> - 音乐库网站的制作者，虽然我并不认识他
- 所有开源社区的贡献者
- 我家的果冻 - 早期 APP 图标贡献者，实在是不会作图 ：）

---

## 📮 反馈与交流

- **提交 Issue**：
  - Gitee：<https://gitee.com/chinut/free-android-music-app/issues>
  - GitHub：<https://github.com/chinut/Free-android-music-app/issues>
- 有问题或建议，欢迎在 Issues 里提出
- 也欢迎 Fork 和 Pull Request

---

## ⭐ Star 历史

如果这个项目对你有帮助，欢迎给个 ⭐ Star 支持一下！
