# 焰火TV 遥控协议 v2 — 电视端移交说明

> 给 **AndroidTV（焰火TV）** 项目的开发者/agent。
> 手机端（焰火音乐 App）**已经改好并兼容本协议**，电视端改完即可生效。

---

## 一、要解决什么问题

手机 App 里有个「电视遥控」功能。目前它靠**扫描局域网 + 试错**来找电视，因为协议不给任何身份信息：

| 现状 | 造成的后果 |
|---|---|
| `ping` 里 `app` 字段**写死** `"焰火TV"` | 家里两台电视在手机列表里**名字完全一样**，用户分不清哪台是客厅 |
| `ping` **不返回实际端口** | 电视端 8899 被占会自动退让到 8900，手机只能把 `8899~8918` 全扫一遍（253 个地址 × 20 个端口 = 5060 次连接，约 28 秒） |
| 没有任何广播/通告 | 手机每次都要全量扫描 |

**目标**：让电视**自报身份**（名字 + 实际端口），手机端就能从"猜"变成"读"。

---

## 二、改动清单（共 4 个文件，约 40 行）

### 1. `core/AppPrefs.kt`

**① 加一个字段**（照抄 `debugPort` 的写法，位置就在它旁边）：

```kotlin
/** 电视名称。手机遥控列表里显示这个名字，用来区分家里多台电视。留空时用机型名。 */
var deviceName: String
    get() = sp.getString(KEY_DEVICE_NAME, "") ?: ""
    set(v) = sp.edit().putString(KEY_DEVICE_NAME, v).apply().also { touch() }
```

**② 加常量**（和其它 `KEY_*` 放一起）：

```kotlin
private const val KEY_DEVICE_NAME = "device_name"
```

**③ `snapshot()` 里加一行**（约 342 行，`KEY_PORT to debugPort.toString(),` 附近）：

```kotlin
KEY_DEVICE_NAME to deviceName,
```

**④ `applyRemote()` 里加一段**（约 385 行，`map[KEY_PORT]` 附近），让手机网页也能改：

```kotlin
map[KEY_DEVICE_NAME]?.let { e.putString(KEY_DEVICE_NAME, it.trim().take(24)) }
// 兼容网页表单用短名 device_name
map["device_name"]?.let { e.putString(KEY_DEVICE_NAME, it.trim().take(24)) }
```

> `take(24)` 是防御：手机列表一行放不下太长的名字。

### 2. `core/DebugWebServer.kt` — 改 `ping` 响应（**核心**）

找到约 **321 行**的：

```kotlin
path == "/api/remote/ping" && method == "GET" ->
    respond(
        out, 200,
        """{"ok":true,"app":"焰火TV","protocol":1,"screen":"${RemoteBus.screen()}"}""",
        "application/json; charset=utf-8",
    )
```

改成：

```kotlin
path == "/api/remote/ping" && method == "GET" -> {
    // 电视名：用户设过就用用户的，否则用机型名（如 "MiTV-4A"），
    // 保证手机端永远不会显示空白
    val rawName = BawanApp.prefs.deviceName.trim()
    val name = rawName.ifBlank { (Build.MODEL ?: "").trim().ifBlank { "焰火TV" } }
    // _port 是实际绑定成功的端口（8899 被占用时会退让到 8900/8901…）
    val realPort = _port.value
    respond(
        out, 200,
        // name / port 必须用 JSONObject 拼，别手写字符串：
        // 用户起的中文名或带引号的名字会把手写 JSON 弄坏，手机端会解析失败当成"没找到电视"
        org.json.JSONObject().apply {
            put("ok", true)
            put("app", "焰火TV")        // 保留：老手机端只读这个字段
            put("name", name)            // 新增：手机端优先读这个
            put("protocol", 2)           // 1 → 2
            put("port", realPort)        // 新增：实际监听端口
            put("screen", RemoteBus.screen())
        }.toString(),
        "application/json; charset=utf-8",
    )
}
```

需要 `import android.os.Build`（若尚未导入）和 `import com.chinut.bawantv.BawanApp`（多半已有）。

> ⚠️ **`app` 字段一定要保留**。老版本手机 App 只读 `app`，删掉它会让老用户找不到电视。

### 3. `ui/screens/SettingsScreen.kt` — 加「电视名称」设置项

**① `EditTarget` 枚举加一项**（约 895 行）：

```kotlin
private enum class EditTarget(val title: String) {
    Domain("低端影视域名"),
    LiveSource("直播源地址"),
    Port("调试端口"),
    Token("手机调试口令"),
    DeviceName("电视名称");        // ← 新增

    fun current(prefs: AppPrefs): String = when (this) {
        // …原有分支不动…
        DeviceName -> prefs.deviceName
    }

    fun apply(prefs: AppPrefs, value: String) {
        when (this) {
            // …原有分支不动…
            DeviceName -> prefs.deviceName = value.trim().take(24)
        }
    }
}
```

**② 在「手机网页调试」那一节的 `KeyValueRow` 之间插一行**（约 293~301 行，`调试端口` 与 `手机口令` 之间）：

```kotlin
KeyValueRow(
    label = "电视名称",
    value = prefs.deviceName.ifBlank { "（未设置，显示机型名）" },
    onEdit = { editing = EditTarget.DeviceName },
)
```

### 4. （可选但推荐）`docs/网络遥控协议.md`

把接口文档里的 ping 响应示例和字段表更新一下，说明新增的 `name` / `port`。

---

## 三、⚠️ 向后兼容要求（最重要）

手机 App 和电视 App 是**独立发版**的，用户不可能同时升级。两边会自由组合：

| 手机端 | 电视端 | 必须的表现 |
|---|---|---|
| 老（只读 `app`） | **新**（带 `name`/`port`） | 老手机仍能连上（所以 `app` 必须保留） |
| 新（优先 `name`，读 `port`） | **老**（只有 `app`） | 新手机仍能连上（`name` 缺失时回退读 `app`） |
| 新 | 新 | 显示用户设的名字；只扫 8899 就能找到（因为自报端口） |

**硬性约束**：

1. **不能删 `app` 字段** —— 老手机端只认它
2. **不能改 `protocol` 的语义**，只能从 1 递增到 2（手机端不靠它分支，仅记录）
3. **响应必须是合法 JSON** —— 用 `JSONObject` 拼，不要手写字符串拼接
4. **`name` 字段缺失或为空串时，新手机端会回退读 `app`**（已有测试覆盖）
5. **`port` 字段缺失时，新手机端会回退到"探测到的端口"**（已有测试覆盖）

---

## 四、验收标准

改完后按顺序验收：

### 1. 协议自测（不用手机）

在电脑上执行（把 IP 换成电视的）：

```bash
curl -s "http://192.168.31.233:8899/api/remote/ping"
```

期望输出（字段顺序无所谓）：

```json
{"ok":true,"app":"焰火TV","name":"客厅电视","protocol":2,"port":8899,"screen":"home"}
```

- [ ] `name` 是设置里填的名字；没填时是机型名（如 `MiTV-4A`）
- [ ] `port` **等于实际监听端口**。验证方法：先在电视上开别的程序占住 8899，
      让焰火TV 退让到 8900，再 `curl http://<ip>:8900/api/remote/ping`，
      此时 `port` 必须是 **8900**（不是 8899）
- [ ] `app` 仍然是 `"焰火TV"`
- [ ] `protocol` 是 `2`

### 2. 设置项自测

- [ ] 设置 →「手机网页调试」里能看到「电视名称」
- [ ] 点进去能改，改完返回列表显示新名字
- [ ] 名字里输入引号 `"` 和中文，保存后 `curl` 仍返回合法 JSON（不报错）
- [ ] 输入超长名字（50 字），保存后截断到 24 字

### 3. 手机端联调

- [ ] 手机进「设置 → 电视遥控」，列表里显示的**是你设的名字**
- [ ] 家里两台电视都开着、设成不同名字 → 列表里是**两个不同的名字**，一眼能区分
- [ ] 扫描明显变快（手机端在新协议下只需扫默认端口）

### 4. 兼容性自测（关键）

- [ ] 用**老版本手机 App**（v1.5.0 及以前）连**新电视端** → 仍然能连上（因为保留了 `app`）
- [ ] 用**新手机 App** 连**老电视端**（未升级） → 仍然能连上（回退读 `app`、回退用探测端口）

---

## 五、可选进阶：UDP 广播发现（本次可不做）

做完上面第 2 节后，扫描已经够快了。若想再进一步（秒级发现、无视端口变化），可以让电视定期广播：

```
UDP 广播到 255.255.255.255:8898（或子网广播地址），每 3~5 秒一次
内容： {"v":2,"app":"焰火TV","name":"客厅电视","port":8899}
```

手机端收到就立刻知道"谁在哪、叫什么"，**完全不用扫描**。

**但有两个风险**，所以列为可选：

- 有些路由器开了 **AP 隔离 / 屏蔽广播**，广播发不出去（所以手机端必须保留扫描兜底）
- 电视端需要持有 `MulticastLock`（`WifiManager.createMulticastLock`）才能收广播，
  发广播一般不需要，但不同机型行为不一致

**建议**：先把第 1~4 步做完、验证稳定，再考虑加广播。

---

## 六、参考：手机端是怎么处理你的响应的

手机端解析逻辑（[YanhuoRemote.kt](../app/src/main/java/com/example/music/data/tv/YanhuoRemote.kt) `pingInfo()`）：

```kotlin
val o = JSONObject(body)
// name 优先（新协议），退回 app（老协议）
val name = o.optString("name", "").ifBlank { o.optString("app", "") }
PingInfo(
    name = name,
    protocol = o.optInt("protocol", 0),
    screen = o.optString("screen", ""),
    port = o.optInt("port", 0),      // 0 或缺省 = 未提供
)
```

- `port` 为 `0` 或超出 `1..65535` → 视为未提供，回退到探测到的端口
- `port` 即使是字符串 `"8900"` 也能读出来（`optInt` 容错）

**契约测试已覆盖以上全部情况**，见
[PingResponseTest.kt](../app/src/test/java/com/example/music/PingResponseTest.kt)（10 个用例）。
电视端改完后，建议把这 10 个用例跑一遍确认没破坏旧行为。

---

## 七、改动影响面

| 项目 | 是否受影响 |
|---|---|
| 手机网页调试（`/api/state`、配置页） | 不受影响（只是多了一个可配置项） |
| 按键 / 音量 / 静音 / 文字接口 | **完全不动** |
| 老版本手机 App | 不受影响（`app` 保留、`protocol` 只递增） |
| 电视端设置存储 | 多一个 `device_name` 键，老数据不受影响 |
| 二维码 / 调试地址显示 | 不受影响 |

**只新增字段，不改任何已有行为。**
