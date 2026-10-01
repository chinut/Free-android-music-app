window.$$=Zepto;
if(typeof _tvIsApp === "undefined"){
    _tvIsApp=false;
}
if(typeof _tvIsGecko === "undefined"){
    _tvIsGecko=false;
}
var _tvFunc={
    // 获取url请求参数
    getQueryParamsXX() {
      var query = location.search.substring(1)
      var arr = query.split('&')
      var params = {}
      for (var i = 0; i < arr.length; i++) {
          var pair = arr[i].split('=')
         params[pair[0]] = decodeURI(pair[1]);
      }
     console.log(params)
     return params
   },
    getQueryParamsNew() {
    return Object.fromEntries(
        new URLSearchParams(window.location.search)
    );
   },
    getQueryParams() {
    const queryString = window.location.search;
    const params = new URLSearchParams(queryString);

    // 使用兼容性最好的reduce方法
    return Array.from(params).reduce((acc, [key, value]) => {
        acc[key] = value;
        return acc;
    }, {});
   },
    isApp(){
        return _tvIsApp;
    },
    isGecko(){
        return _tvIsGecko;
    },
    url(url){
      if(url.startsWith("http")){
          return url;
      }
     let index= window.location.href.lastIndexOf("/")
        return window.location.href.substring(0,index+1)+url;
    },
    link(url){
        if(url.startsWith("http")){
            return url;
        }
        if(url.startsWith("//")){
            return "https:"+url;
        }
        return url;
    },
    getVideoQuality(videoElement) {
    // 确保视频元数据已加载
   /* if (videoElement.readyState < 1) {
        console.warn("Video metadata not loaded. Listen for 'loadedmetadata' event.");
        return "未知";
    }*/
    const width = videoElement.videoWidth;
    const height = videoElement.videoHeight;
    const maxDimension = Math.max(width, height);
    // 主流通用分辨率标准判断 [6,7,8](@ref)
    if (maxDimension >= 3840) return "4K";       // 4K标准：3840x2160或更高
    else if (maxDimension >= 1920) return "1080P"; // 全高清：1920x1080
    else if (maxDimension >= 1280) return "720P";  // 高清：1280x720
    else if (maxDimension >= 640) return "480P";   // 标清：640x480（部分场景归为360P）
    else return "360P";                            // 低清：如640x360
   },
   loadCssCode(code) {
      var style = document.createElement('style')
    // style.type = 'text/css'
      style.rel = 'stylesheet'
      style.appendChild(document.createTextNode(code))
      var head = document.getElementsByTagName('head')[0]
      head.appendChild(style);
    },
    fullscreenQQ(id){
        var css=`
   ${id}{
        position: fixed !important;
       z-index: 99990 !important;
       width: 100% !important;
       height: 100% !important;
      top: 0 !important;
      left: 0 !important;
      right:0 !important;
      bottom: 0 !important;
          background-color: rgb(0, 0, 0); 
    }
 `;
        this.loadCssCode(css);
    },
    fullscreen(id) {
        var css = `${id}{
            position: fixed !important;
            z-index: 99990 !important;
            width: 100% !important;
            height: 100% !important;
            top: 0 !important;
            left: 0 !important;
            right:0 !important;
            background-color: rgb(0, 0, 0);
            bottom: 0 !important;}
            video::-webkit-media-controls {display: none !important;}
video::-webkit-media-controls-enclosure {display: none !important;}
video::-webkit-media-controls-panel {display: none !important;}
video::-webkit-media-controls-play-button {display: none !important;}
video::-webkit-media-controls-start-playback-button {display: none !important;}
video::-moz-media-controls {display: none !important;}`;
        this.loadCssCode(css);
    },
    fixedW(id){
        var css=`
   ${id}{
        position: fixed !important;
       }
 `;
        this.loadCssCode(css);
    },
    fullscreenW(id){
        var css=`
   ${id}{
        position: fixed !important;
       z-index: 99990 !important;
       width: 100vw !important;
       height: 100vh !important;
      top: 0 !important;
      left: 0 !important;
      right:0 !important;
      margin-left:0 !important;
          background-color: rgb(0, 0, 0); 
      bottom: 0 !important;
    }
 `;
        this.loadCssCode(css);
    },
    fullscreenWW(id){
        var css=`
   ${id}{
        position: fixed !important;
       z-index: 99990 !important;
       width: 100vw !important;
       height: 100vh !important;
      top: 0 !important;
      left: 0 !important;
      right:0 !important;
         background-color: rgb(0, 0, 0); 
      bottom: 0 !important;
    }
 `;
        this.loadCssCode(css);
    },
    toggleFullScreen(elem) {
        if (!document.fullscreenElement) {
            if (elem.requestFullscreen) {
                elem.requestFullscreen();
            }
        } else {
            if (document.exitFullscreen) {
                document.exitFullscreen();
            }
        }
    },
    addKeyFullScreen(elem){
        document.addEventListener('keydown', function(event) {
            console.log("addKeyFullScreen",event.keyCode);
            //f
            if (event.keyCode === 70) {
                _tvFunc.toggleFullScreen(elem);
            }
        });
    },
    title(title){
          if(isNaN(title)){
              return title;
          }
          return `第${title}集`
    },
    titleQ(titleInt,title){
        if(isNaN(titleInt)){
            return title;
        }
        if(titleInt>2000){
            return title;
        }
        return `第${titleInt}集`
    },
    userAgent(isH5){
        let userAgent="Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36 Edg/128.0.0.0";
        if(isH5){
            userAgent="Mozilla/5.0 (iPhone; CPU iPhone OS 17_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Cronet Mobile/15E148 Safari/604.1";
        }
        return userAgent;
    },
    video:null,
    volume100(ready){
       this.videoReady(function (video){
           video.volume = 1;
           console.log("video volume set ",video.volume);
       })
    },
    getVideo(){
        if(null!=this.video){
            return this.video;
        }
        let videos = document.getElementsByTagName("video");
            //document.getElementsByTagName("video");
        if(videos.length > 0){
            this.video=videos[0];
        }
        return this.video;
    },
    videoTrueReady(ready,time){
        let video = this.getVideo();
        if(null==video){
            this.check(function (){return document.getElementsByTagName("video").length>0;},function (){
                video=_tvFunc.getVideo();
                _tvFunc.check(function (){return video.readyState>2&& video.duration>time;},function (){
                    console.log("video found ",video.readyState);
                    if(ready){
                        ready(video);
                    }
                })
            })
        }
        if(null!=video){
            this.check(function (){return video.readyState>2 && video.duration>time;},function (){
                console.log("video found ",video.readyState);
                if(ready){
                    ready(video);
                }
            })
        }
    },
    videoPlay(){
        let video = this.getVideo();
        if(video.paused){
            video.play();
        }else{
            video.pause();
        }
    },
    videoReady(ready){
        let video = this.getVideo();
        if(null==video){
            this.check(function (){return document.getElementsByTagName("video").length>0;},function (){
                video=_tvFunc.getVideo();
                _tvFunc.check(function (){return video.readyState>2;},function (){
                    console.log("video found ",video.readyState);
                    if(ready){
                        ready(video);
                    }
                })
            })
        }
        if(null!=video){
            this.check(function (){return video.readyState>2;},function (){
                console.log("video found ",video.readyState);
                if(ready){
                    ready(video);
                }
            })
        }
    },
    currentXj(item){
        //title,site,vodId,url
        _layer.notify("当前"+item.title);
        console.log("currentXj "+item.vodId+"  "+item.site);
        //记录当前
        if(null!=item.vodId&&""!==item.site){
            let remark= item.title;
            _apiX.msg("history.update",
                {site:item.site,vodId:item.vodId,url:item.url,name:item.name,remark:remark});
        }
    },
    hzLevel(name,type){
       if(type===1){
         if(name.includes("4K")){
            return 4096;
         }
         if(name.includes("1080")){
            return 1080;
         }
         if(name.includes("720")){
            return 720;
         }
         return 480;
       }
       if(type===2){
          if(name.includes("超清")){
            return 1080;
         }
         if(name.includes("高清")){
            return 720;
         }
         return 480;
       }

    },
    paramStr(data,start){
        let param="";
        Object.keys(data).forEach(key => {
            param+=`&${key}=${data[key]}`
        });
        if(start){
            return "?"+param.substring(1);
        }
        return  param;
    },
   image(url){
     if(url.includes("?")){
        return url+"&tvImg=1";
     }
    return url+"?tvImg=1";
   },
   show(){
    document.body.style.visibility="visible";
   },
   maxCheck(check,callback,maxNum){
     let num=0;
     let index=setInterval(function(){
        if(check()){
            clearInterval(index);
            callback(index);
        }
        num++;
        if(num>maxNum){
          clearInterval(index);
          callback(index);
        }
     },1000);
   },
   check(check,callback,time,maxNumOrg){
    if(!time){
        time=500;
    }
    let num=0;
    let maxNum=50;
    if(maxNumOrg&&maxNumOrg>0){
       maxNum=maxNumOrg;
    }
    let index=setInterval(function(){
        try{
            if(check()){
                clearInterval(index);
                callback(index);
            }
            console.log("num {} maxNum {}",num,maxNum)
            num++;
            if(num>maxNum){
                clearInterval(index);
            }
        }catch (e){
            clearInterval(index);
        }
    },time);
    return index;
   },
   sessionStorageCheckTime(key){
     let time= sessionStorage.getItem(key+"Time");
     if(time){
         let now=  new Date().getTime();
         if(now<(Number(time)+5000)){
            return true;
         }
     }
     return false;
   },
    //网友改
    waitForVideoElement() {
        const timeout = 1000 * 35;  //************************
        return new Promise((resolve) => {
                // 优先查找已存在的 video 元素
                const existing = document.querySelector('video');
                if (existing) {
                    //_apiX.videoFind("ok");
                    resolve(existing);
                    return;
                }

                const startTime = Date.now();
                let intervalId = null;

                // 使用 MutationObserver 监听 DOM 变化
                const observer = new MutationObserver(() => {
                        const video = document.querySelector('video');
                        if (video) {
                            observer.disconnect();
                            clearInterval(intervalId);
                            //_apiX.videoFind("ok");
                            resolve(video);
                        }
                    }
                );

                observer.observe(document.body, {
                    childList: true,
                    subtree: true
                });

                // 每隔 200ms 轮询一次作为兜底
                intervalId = setInterval(() => {
                    const video = document.querySelector('video');
                    if (video) {
                        observer.disconnect();
                        clearInterval(intervalId);
                        //_apiX.videoFind("ok");
                        resolve(video);
                    } else if (Date.now() - startTime >= timeout) {
                        observer.disconnect();
                        clearInterval(intervalId);
                        //_apiX.toast(timeout / 1000 + "秒，未找到资源。系统重新加载！");
                        resolve(null);
                    }
                }, 200);
            }
        );
    },
    async waitForVideoPlay(video, timeout) {
        if (!video)
            video = await this.waitForVideoElement();
        if (!video) {
            return Promise.resolve(false);
        }

        if (this.isVideoPlaying(video)) {
            return Promise.resolve(true);
        }

        return new Promise((resolve) => {
                const startTime = Date.now();
                timeout = timeout || 1000 * 35; //************************
                const check = () => {
                    if (this.isVideoPlaying(video)) {
                        resolve(true);

                        // 注册卡顿回调
                        const stallDetector = new VideoStallDetector(video);
                        stallDetector.onStall = (reason) => {
                            if (reason.toLowerCase() != 'buffering' && reason.toLowerCase() != 'readystate') {
                                _apiX.toast('卡顿原因：' + reason + "。系统重新加载！");
                                _apiX.msg("tobackup", "{}");
                            } else {//_apiX.toast('卡顿原因：' + reason);
                            }
                        };

                    } else if (Date.now() - startTime >= timeout) {
                        //_apiX.toast(timeout / 1000 + "秒，视频未播放。系统重新加载！");
                        resolve(false);
                    } else {
                        setTimeout(check, 200); // 每 200ms 检查一次
                    }
                };
                check();
            }
        );
    },
    isVideoPlaying(video) {
        return video.readyState > 2 && !video.paused && !video.ended && video.currentTime > 0;
    }
};
var _apiX={
    userAgent(isH5){
        let userAgent="Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36 Edg/128.0.0.0";
        if(isH5){
            userAgent="Mozilla/5.0 (iPhone; CPU iPhone OS 17_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Cronet Mobile/15E148 Safari/604.1";
        }
        return userAgent;
    },
    toast(message){
        _api.toast(message);
    },
    msg(service,data){
        let dataStr=  JSON.stringify(data);
        _api.message(service,dataStr);
    },
    msgStr(service,dataStr){
        console.log("msgStr",service,dataStr)
        _api.message(service,dataStr);
    },
    queryByService:async function (service,param,callback){
        if(null!=param){
            param=  JSON.stringify(param);
        }
        let  result= await  _api.queryByService(service,param);
        callback(result);
    },
    message(service,data,msgId,callback){
        data.msgId=msgId;
        let dataStr=  JSON.stringify(data);
        _api.message(service,dataStr);
        let index = setInterval(function (){
                            let data=  sessionStorage.getItem(msgId);
                            if(data){
                                clearInterval(index);
                               callback(data);
                            }},200)
    },
    postJson:async function (reqUrl, header, data, callback,errBack) {
        let headerStr=  JSON.stringify(header);
        let dataStr=  JSON.stringify(data);
        let result ="505";
        try{
             result= _api.postJson(reqUrl,headerStr,dataStr);
        }catch (e){
            console.error(e);
        }
        let isError=this.errorWork(result,errBack);
        if(isError){
            return;
        }
       callback(result);
    },
    getHtml: async function(reqUrl, header, callback,errBack) {
        let headerStr=  JSON.stringify(header);
        let result="505";
        try{
            result= _api.getHtml(reqUrl,headerStr);
        }catch (e){
            console.error(e);
        }
        let isError=this.errorWork(result,errBack);
        if(isError){
            return;
        }
        callback(result);
    },
    getJson: async function (reqUrl, header, callback,errBack) {
        let headerStr=  JSON.stringify(header);
        let result="505";
        try{
             result= _api.getJson(reqUrl,headerStr);
        }catch (e){
            console.error(e);
        }
        let isError=this.errorWork(result,errBack);
        if(isError){
            return;
        }
        callback(result);
    },
    errorWork(result,errBack){
        if(result==="500"||result==="400"||result==="505"){
            if(errBack){
                errBack(result);
                let  extMsg="";
                if(result==="505"){
                    extMsg="请检查网络";
                }
                _layer.notify("请求接口失败 "+extMsg)
            }
            return true;
        }
        return false;
    }
};

var _layer={
    init(id,html,index,idName,...classNames){
        var myDiv = document.createElement("div");
        if(!idName){
            idName="tv-index-"+index;
        }
        myDiv.id = idName;
        myDiv.style.zIndex=index;
        myDiv.style.visibility="hidden";//hidden  visible
        myDiv.style.maxWidth="100vw";
        myDiv.style.overflowY="auto";
        if(classNames.length==0){
            myDiv.className="tv-index";
        }else{
            myDiv.className=classNames.join(" ");
        }
        myDiv.innerHTML = html;
        if(null==id){
            document.body.appendChild(myDiv);
        }else{
            let chooseId=id.substring(1);
            if(id.startsWith("#")){
                document.getElementById(chooseId).appendChild(myDiv);
            }
            if(id.startsWith(".")){
                document.getElementsByClassName(chooseId)[0].appendChild(myDiv);
            }
            if(id.startsWith("&")){
                document.getElementsByTagName(chooseId)[0].appendChild(myDiv);
            }
        }
        return idName;
    },
    initMenu(id,html,index,idName,...classNames){
        let menuId=this.init(id,html,index,idName,...classNames);
        window._tv_menuId=menuId;
        return menuId;
    },
    toggle(id){
        let elem=document.getElementById(id);
        if(elem){
            if(elem.style.visibility=="hidden"){
                elem.style.visibility="visible";
                return true;
            }
            elem.style.visibility="hidden";
        }
        return false;
    },
    isShow(id){
        if(typeof(id)=='undefined'||null==id){
            return false;
        }
        if(null!=document.getElementById(id)){
            return document.getElementById(id).style.visibility==="visible";
        }
        return false;
    },
    show(id){
        if(document.getElementById(id)){
            document.getElementById(id).style.visibility="visible";
        }
    },
    hide(id){
        if(document.getElementById(id)){
            document.getElementById(id).style.visibility="hidden";
        }
    },
    open(html,index,idName,...classNames){
        var myDiv = document.createElement("div");
        if(!idName){
            idName="tv-index-"+index;
        }
        myDiv.id = idName;
        myDiv.style.zIndex=index;
        if(classNames.length==0){
            myDiv.className="tv-index";
        }else{
            myDiv.className=classNames.join(" ");
        }
        myDiv.innerHTML = html;
        document.body.appendChild(myDiv);
        return idName;
    },
    openById(id,html,index,idName,className){
        var myDiv = document.createElement("div");
        if(!idName){
            idName="tv-index-"+index;
        }
        myDiv.id = idName;
        myDiv.style.zIndex=index;
        if(!className){
            className="tv-index";
        }
        myDiv.className=className;
        myDiv.innerHTML = html;
        document.getElementById(id).appendChild(myDiv);
        return idName;
    },
    notify(text,index,time){
        if(!time){
            time=2.5;
        }
        if(!index){
            index=9999999;
        }
        let html='<div>'+text+'</div>';
        let id= this.open(html,index,null,"tv-notify");
        setTimeout(function(){
            _layer.close(id);
        },time*1000);
    },
    notifyLess(text,index,time){
        if(!time){
            time=1;
        }
        if(!index){
            index=9999998;
        }
        let html='<div style="color: white;">'+text+'</div>';
        let lastId="tv-index-"+index;
        this.close(lastId);
        let id= this.open(html,index,null,"notify-less");
        setTimeout(function(){
            _layer.close(id);
        },time*1000);
    },
    wait(text,index){
        if(!index){
            index=9999999;
        }
        let html='<div style="color: white;">'+text+'</div>';
        let id= this.open(html,index,null,"tv-wait");
        return id;
    },
    close(id){
        if(document.getElementById(id)){
            document.getElementById(id).remove();
        }
    }
}
var _tvMsg={
    notVip:" 建议在拼多多/抖音/淘宝等购物软件里搜索购买"
}
function extractDomain(url) {
    const match = url.match(/^(https?:\/\/[^/?#]+)/i);
    return match ? match[1] : null;
}
function decodeUnicodeBase64(base64Str) {
    return decodeURIComponent(escape(atob(base64Str)));
}
_apiX.msg("videoQuality",[]);
class VideoStallDetector {
    constructor(videoElement, options = {}) {
        this.video = videoElement;
        this.checkInterval = options.checkInterval || 1000 * 60;
        // 默认60秒检查一次
        this.stallTimeThreshold = options.stallTimeThreshold || 60;
        // 默认60秒未更新时间视为卡顿
        this.lastTimeUpdate = 0;
        this.isStalled = false;

        this.init();
    }

    init() {
        this.bindEvents();
        this.startPeriodicCheck();
    }

    bindEvents() {
        this.video.addEventListener('waiting', this.handleWaiting.bind(this));
        this.video.addEventListener('timeupdate', this.handleTimeUpdate.bind(this));
    }

    handleWaiting() {
        //console.log('视频正在缓冲，可能卡住了');
        //if (this.onStall) this.onStall('网络掉线，正在缓冲');
        if (this.onStall)
            this.onStall('Buffering');
    }

    handleTimeUpdate() {
        const currentTime = this.video.currentTime;
        if (currentTime === this.lastTimeUpdate) {
            // 如果连续两次时间未更新，可能卡住了
            if (!this.isStalled) {
                this.isStalled = true;
                setTimeout(() => {
                    if (this.video.currentTime === currentTime) {
                        //console.log(`视频卡住超过 ${this.stallTimeThreshold} 秒`);
                        //if (this.onStall) this.onStall('timeupdate');
                        if (this.onStall)
                            this.onStall(`视频进度更新超 ${this.stallTimeThreshold} 秒`);
                    }
                    this.isStalled = false;
                }, this.stallTimeThreshold * 1000);
            }
        } else {
            this.isStalled = false;
        }
        this.lastTimeUpdate = currentTime;
    }

    startPeriodicCheck() {
        setInterval(() => {
            if (!this.video.paused && this.video.readyState < this.video.HAVE_FUTURE_DATA) {
                //console.log('视频可能卡住了：没有足够的数据可以播放');
                //if (this.onStall) this.onStall('没有足够的数据可以播放');
                if (this.onStall)
                    this.onStall('readyState');
            }
        }, this.checkInterval);
    }

    // 设置回调函数
    onStall(callback) {
        this.onStall = callback;
    }
}