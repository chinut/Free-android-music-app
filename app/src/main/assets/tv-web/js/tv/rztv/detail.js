
(function(){
    _tvFunc.fixedW("body");
    _tvFunc.check(function (){return $$(".channel_bar li").length>0},function (){
        //let url = window.location.href;
        //let index= url.indexOf("tag=");
        let tag =  _tvFunc.getQueryParams()["tag"];
        console.log(tag);
        let currentTag=0;
        $$(".channel_bar li").each(function (i,item){
            let active=  $$(item).hasClass("active");
            if(active){
                currentTag=i;
            }
        });
        console.log(currentTag);
        if(Number(tag)!==currentTag){
            $$(".channel_bar li")[Number(tag)].click();
        }
        _tvFunc.check(function (){return document.getElementsByTagName("video").length>0;},function (){
            //document.getElementsByTagName("video")[0].classList.add("utv-video-full");
            _tvFunc.fullscreen("#play_box");
        });
        // $("#programMain .title")[1].click()
    });
   /* _tvFunc.check(function (){return document.getElementsByTagName("video").length>0;},function (){
        console.log("video found")
        // document.getElementsByTagName("video")[0].classList.add("utv-video-full");
        _tvFunc.fullscreen("#play_box");
        $$("video").css("position","fixed !important");
    });*/

    _tvFunc.volume100();
    _tvFunc.videoReady(function (video){
        if(video.paused){
            video.play();
        }
    })

})();
