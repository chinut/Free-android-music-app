# 音乐库 (Free Android Music App)

一个基于 Kotlin + Jetpack Compose 开发的 Android 音乐播放器，界面简洁现代，功能完整，支持在线听歌、离线缓存、歌单管理、情景模式等多种实用功能。

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

### 🎬 情景模式

根据不同场景自动匹配歌曲，共 8 种：

| 场景 | 说明 |
| --- | --- |
| 🚗 驾驶 | 动力十足，陪你上路 |
| 🌧 忧郁 | 让音乐陪你沉淀 |
| 🎉 聚会 | 嗨起来不解释 |
| 📚 学习 | 专注时刻，安静陪伴 |
| 💪 运动 | 燃烧卡路里 |
| 🌙 睡前 | 睡前放松一下 |
| 💕 恋爱 | 甜蜜浪漫时刻 |
| 📼 怀念 | 回忆当年的歌 |

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

- Android Studio Hedgehog (2023.1) 或更新版本
- JDK 17
- Android SDK API 35

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
│   ├── SceneMode.kt             # 情景模式定义
│   ├── RecommendEngine.kt       # 推荐引擎
│   ├── UserPreferencesStore.kt  # 用户偏好存储
│   ├── api/                     # 网络接口
│   ├── cache/                   # 缓存管理
│   ├── db/                      # Room 数据库（歌单）
│   ├── share/                   # 分享码编解码
│   └── update/                  # 自动更新
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
        ├── SceneScreen.kt       # 情景模式
        ├── PlaylistScreen.kt    # 歌单管理
        ├── FullPlayerScreen.kt  # 全屏播放器
        ├── SettingsScreen.kt    # 设置
        └── ...
```

---

## 🧰 技术栈

| 类别 | 技术 |
| --- | --- |
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 播放 | AndroidX Media3 (ExoPlayer) |
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

> 建议截图：首页推荐、全屏播放器、歌单管理、情景模式、设置页

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
- <https://www.yinyueku.cn/> -音乐库网站的制作者，虽然我并不认是他
- 所有开源社区的贡献者
- 我家的果冻 - APP的图标贡献者，实在是不会作图  ：）

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
