
(function(){
    _tvFunc.fixedW("body");
    _tvFunc.check(function (){return $$(".channel-list .item").length>0},function (){
        let url = window.location.href;
         let index= url.indexOf("tag=");
         let tag = url.substring(index+4,url.length);
         if(tag.includes("#")){
             tag=tag.substring(0,tag.indexOf("#"));
         }
         console.log("tag",tag);
         let currentTag=0;
        $$(".channel-list .item").each(function (i,item){
             let active=  $$(item).find(".watching").length>0;
             if(active){
                 currentTag=i;
             }
        });
        console.log("currentTag",tag);
        if(Number(tag)!==currentTag){
            $$(".channel-list .item")[Number(tag)].click();
        }
        // $("#programMain .title")[1].click()
        _tvFunc.check(function (){return document.getElementsByTagName("video").length>0;},function (){
            // _tvFunc.addKeyFullScreen(document.getElementsByTagName("video")[0]);
            // _apiX.msgStr("key","F");
            _tvFunc.fullscreenW("#liveplayer");
            //$$(".xb-hiden-video").css("position","fixed !important");
            //$$("video").css("position","fixed !important");
            // document.getElementsByTagName("video")[0].classList.add("utv-video-full");
        });
    });
    _tvFunc.volume100();


})();
