
function setupVideo(video) {
    const container = document.createElement('div');
    container.style.position = 'fixed';
    container.style.top = '0';
    container.style.left = '0';
    container.style.width = '100vw';
    container.style.height = '100vh';
    container.style.zIndex = '2147483647';
    container.style.backgroundColor = 'black';
    video.style.width = '100%';
    video.style.height = '100%';
    video.style.objectFit = 'contain';
    video.style.transform = 'translateZ(0)';
    container.appendChild(video);
    document.body.appendChild(container);
    document.body.style.overflow = 'hidden';
    document.documentElement.style.overflow = 'hidden';

}
// 这里已经是文档加载后的了
(function(){
    _tvFunc.waitForVideoElement().then(video => {
        let param=_tvFunc.getQueryParams();
        let type="0";
        if(param["utaot"]){
            type=param["utaot"];
        }
        console.log("type::",type);
        if(type==="0"){
            setupVideo(video);
        }
        if(type==="1"){
            _tvFunc.fixedW("body");
            _tvFunc.fullscreen("video");
            $$("video").css("position","fixed !important");
        }
        video.muted = false;
        video.volume = 1;
        video.playsInline = false;
        video.setAttribute('playsinline', 'false');
        try {
            video.play();
        } catch (e) {
        }
        $$("body").css("min-width","100%");
        $$("html").css("min-width","100%");
        _tvFunc.check(function (){
            let videoPlay=_tvFunc.isVideoPlaying(video);
            if(!videoPlay){
                try{
                    _tvFunc.getVideo().play();
                }catch(e){}
            }
            return videoPlay},function (){_data.hzList(video);},1000,5);

    });
})();
$$(function (){
    let url = window.location.href;
    if(url.indexOf("u-link=1")>0){
        _tvFunc.check(function (){
            let utaoLoc =  sessionStorage.getItem("u-loc");
            if(utaoLoc){
                console.log("utaoLoc",utaoLoc,url);
                if(url==utaoLoc){
                    window.location.href=extractDomain(utaoLoc)+"/tv-web/live.html?url="+sessionStorage.getItem("u-m3u8");
                    return true
                }
            }
            return false},function (){},1000,10);
    }
  /*  let param=_tvFunc.getQueryParams();
    if(param["ujs"]){
        let ujs = param["ujs"];
        ujs=decodeURIComponent(ujs);
        console.log("ujs",ujs);
    }*/
    let ujs=   _tvFunc.getQueryParams()["ujs"];
        //url.indexOf("ujs=");
    if(ujs){
        let ujsContent=decodeUnicodeBase64(ujs.replace(/ /g, '+'));
        console.log("ujsContent",ujsContent);
        eval(ujsContent);
        //$$(tag).click();
    }
    //viewport
 /*   let viewport = document.getElementById("viewport");
    console.log("viewport::",viewport);
    if(viewport){
        viewport.content = "width=device-width, initial-scale=1";
    }*/
    const viewportMeta = document.querySelector('meta[name="viewport"]');
    console.log("viewportMeta::",viewportMeta);
    if (viewportMeta) {
        viewportMeta.setAttribute('content', `width=device-width, initial-scale=1`);
    }
    //

// _tvFunc.videoReady(function (video)

});



