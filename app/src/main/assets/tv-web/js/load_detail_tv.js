if(typeof _tvload == "undefined"){
     _tvload=false;
}
function extractDomain(url) {
    const match = url.match(/^(https?:\/\/[^/?#]+)/i);
    return match ? match[1] : null;
}
function decodeUnicodeBase64(base64Str) {
    return decodeURIComponent(escape(atob(base64Str)));
}
_data={
    hzList(video){
        let hzName =  _tvFunc.getVideoQuality(video);
        let hzList=[];
        let itemData={name:hzName,level:_tvFunc.hzLevel(hzName,1)};
        hzList.push(itemData);
        _apiX.msg("videoQuality",hzList);
        return hzList;
    }
};
(function(){
    if(_tvload){
        return;
    }
    _tvload=true;
  /*  if(window.location.href.startsWith("https://v.qq.com/x/cover/")){
        return ;
    }*/
    function loadDetailByUrl(url){
       /* if(url.startsWith("https://www.yangshipin.cn/tv/home")){
            return "cctv";
        }*/
        if(url.startsWith("https://tv.cctv.com/live")){
            return "tv/cctv";
        }
        if(url.startsWith("https://www.yangshipin.cn")){
            return "tv/ysptv"
        }
        //各大tv
        if(url.startsWith("https://live.jstv.com")){
            return "tv/jstv"
        }

        if(url.startsWith("https://www.btime.com")){
            return "tv/bjtv"
        }
        if(url.startsWith("https://www.jlntv.cn/")){
            return "tv/jltv"
        }
        if(url.startsWith("https://www.lcxw.cn/")){
            _tvLoadRes.js("https://cdn.bootcdn.net/ajax/libs/hls.js/1.5.13/hls.js");
            return "tv/lctv"
        }
        if(url.startsWith("https://www.fengshows.com/")){
            return "tv/fengshows"
        }
        if(url.startsWith("https://www.nmtv.cn")){
            return "tv/nmtv"
        }
        if(url.startsWith("https://www.mgtv.com/live")){
            return "tv/hntv"
        }
        if(url.startsWith("https://web.guangdianyun.tv")){
            return "tv/gdytv"
        }
        return "tv/common"
    }
    let detailPath=loadDetailByUrl(window.location.href);
    console.log("detailPath:: "+detailPath);
    let fullUrl=window.location.href;
    let domain=extractDomain(fullUrl);
    _tvLoadRes.css(domain+`/tv-web/css/my.css`);
    _tvLoadRes.js(domain+`/tv-web/js/zepto.min.js?v=x`);
    _tvLoadRes.js(domain+`/tv-web/js/common.js?v=x`);
    _tvLoadRes.js(domain+`/tv-web/js/${detailPath}/detail.js`);
/*    if(domain==="https://www.huya.com"||domain==="https://www.ntjoy.com"||domain==="https://www.tcsrm.cn"){
        _tvLoadRes.css(domain+`/tv-web/css/my.css`);
        _tvLoadRes.js(domain+`/tv-web/js/zepto.min.js?v=x`);
        _tvLoadRes.js(domain+`/tv-web/js/common.js?v=x`);
        _tvLoadRes.js(domain+`/tv-web/js/${detailPath}/detail.js`);
        return;
    }
    _tvLoadRes.css(_browser.getURL("css/my.css?v=x"));
    _tvLoadRes.js(_browser.getURL("js/zepto.min.js?v=x"));
    _tvLoadRes.js(_browser.getURL("js/common.js?v=x"));
    _tvLoadRes.js(_browser.getURL(`js/${detailPath}/detail.js?v=x`));//"js/youku/detail.js?v=x"*/

})();