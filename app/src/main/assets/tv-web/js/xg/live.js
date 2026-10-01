let url = window.location.href;
let index= url.indexOf("url=");
let link = url.substring(index+4,url.length);
link=link.replace("&usave=1","")
/*_tvFunc= {
    // 获取url请求参数
    getQueryParams() {
        const queryString = window.location.search;
        const params = new URLSearchParams(queryString);

        // 使用兼容性最好的reduce方法
        return Array.from(params).reduce((acc, [key, value]) => {
            acc[key] = value;
            return acc;
        }, {});
    }
}
let link =  _tvFunc.getQueryParams()["url"];*/
const config = {
    "id": "mse",
    "url": link,
    "playsinline": true,
    "plugins": [],
    "isLive": true,
    "autoplay": true,
    volume: 1,
    "width": "100%",
    "height": "100%"
}
//config.plugins.push(HlsPlayer);
//config.plugins.push(FlvPlayer)
let player = new HlsJsPlayer(config);


