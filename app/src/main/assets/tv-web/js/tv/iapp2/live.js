// 从 URL 参数获取 orgid 和 id
let queryParams = _tvFunc.getQueryParams();
let orgId = queryParams["orgid"] || "113";  // 默认值 113
let id = queryParams["id"] || "131";        // 默认值 131

console.log("orgId:", orgId, "id:", id);

let playUrl = null;

/**
 * 初始化播放器
 * 请求 API 获取直播流地址并播放
 */
let initPlayer = function() {
    // 构建 API 请求 URL（只带 orgid 参数，不带 id）
    let apiUrl = `https://app.litenews.cn/v1/app/play/tv/live?orgid=${orgId}`;
    console.log("请求 API:", apiUrl);
    
    // 请求 JSON API
    _apiX.getJson(
        apiUrl,
        { 
            "User-Agent": _apiX.userAgent(false)
        },
        function(text) {
            console.log("API 返回数据:", text);
            
            try {
                // 解析 JSON 数据
                let result = JSON.parse(text);
                
                // 检查返回格式
                if (!result || !result.data || !Array.isArray(result.data)) {
                    console.error("API 返回数据格式错误:", result);
                    _layer.notify("获取直播流失败：数据格式错误");
                    return;
                }
                
                // 从 data 数组中查找 id 匹配的项
                // 将 id 转换为数字进行比较（因为 URL 参数是字符串）
                let targetId = parseInt(id, 10);
                let matchedItem = result.data.find(function(item) {
                    return item.id === targetId;
                });
                
                if (!matchedItem) {
                    console.error("未找到匹配的频道，id:", targetId);
                    _layer.notify("未找到匹配的直播频道");
                    return;
                }
                
                // 提取 stream 字段
                playUrl = matchedItem.stream;
                console.log("提取的播放地址:", playUrl);
                
                if (!playUrl || playUrl.trim() === "") {
                    console.error("播放地址为空");
                    _layer.notify("播放地址为空");
                    return;
                }
                
                // 初始化播放器配置
                const config = {
                    "id": "mse",
                    "url": playUrl,
                    "hlsOpts": {
                        xhrSetup: function(xhr, url) {
                            // 可以在这里设置请求头
                        }
                    },
                    "playsinline": true,
                    "plugins": [],
                    "isLive": true,
                    "autoplay": true,
                    volume: 1,
                    "width": "100%",
                    "height": "100%"
                };
                
                // 创建播放器实例
                player = new HlsJsPlayer(config);
                
                // 初始化播放列表
                _data.hzList(_tvFunc.getVideo());
                
            } catch (e) {
                console.error("解析 JSON 数据失败:", e);
                _layer.notify("解析数据失败");
            }
        },
        function(error) {
            console.error("API 请求失败:", error);
            _layer.notify("请求直播流失败");
        }
    );
};

// 初始化播放器
initPlayer();

