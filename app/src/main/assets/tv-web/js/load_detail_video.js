if(typeof _tvload == "undefined"){
     _tvload=false;
}
(function(){
    if(_tvload){
        return;
    }
    _tvload=true;
    function loadDetailByUrl(url){
        if(url.startsWith("https://www.yangshipin.cn/tv/home")){
            return "cctv";
        }
        if(url.startsWith("https://tv.cctv.com/")){
            return "cctvideo";
        }
        if(url.startsWith("https://www.bestv.com.cn/web/play/")){
            return "bestv";
        }
        if(url.startsWith("https://www.ixigua.com/")){
            return "xigua";
        }
        if(url.startsWith("https://v.qq.com/x/cover/")){
            return "qq";
        }
        if(url.startsWith("https://v.youku.com/")){
            return "youku";
        }
        if(url.startsWith("https://www.iqiyi.com/")){
            return "iqiyi";
        }
        if(url.startsWith("https://www.miguvideo.com/p/live")){
            return "ty/mg";
        }
        if(url.startsWith("https://passport.migu.cn/login")){
            return "ty/mg/login";
        }
        if(url.startsWith("https://www.mgtv.com/b/")){
            return "mgtv";
        }
        if(url.startsWith("https://www.le.com/")){
            return "letv";
        }
        if(url.startsWith("https://www.bilibili.com/bangumi/play/")){
            return "bili";
        }
        if(url.startsWith("https://movie.douban.com/subject/")){
            return "douban";
        }
        if(url.startsWith("https://www.douyin.com/?recommend=1")){
            return "dytj";
        }
        return null;
    }
    let detailPath=loadDetailByUrl(window.location.href);
    console.log("detailPath:: "+detailPath);
    if(null!=detailPath){
        _tvLoadRes.css(_browser.getURL("css/my.css?v=x"));
        _tvLoadRes.js(_browser.getURL("js/zepto.min.js?v=x"));
        _tvLoadRes.js(_browser.getURL("js/common.js?v=x"));
        _tvLoadRes.js(_browser.getURL("js/myfocus.js?v=x"));
        _tvLoadRes.js(_browser.getURL("js/vuex.min.js?v=x"));
        _tvLoadRes.js(_browser.getURL("js/detailBase.js?v=x"));
        _tvLoadRes.js(_browser.getURL(`js/${detailPath}/detail.js?v=x`));//"js/youku/detail.js?v=x"
    }
})();