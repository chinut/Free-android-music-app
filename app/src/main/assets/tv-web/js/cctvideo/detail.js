const _ctrlx={
    ok(){
        $$(".vjs-play-control").click();
        console.log("play");
        _layer.notifyLess("OK键暂停或播放")
    },
    fullscreen() {
        _tvFunc.check(function (){return null!=document.getElementById("_video_player")},function (){
             document.getElementById("_video_player").classList.add("utv-video-full");
            //_apiX.msgStr("key","F");
        });
    }
};
(function(){
   const _app={
        init(){
            _tvFunc.check(function(){return $$(".vjs-play-control").length>0;},function(){
                 _detailInit(null,9990);
            }); 
            //$$(".XUQIU18897_fuceng").remove()
            _tvFunc.check(function(){return $$(".XUQIU18897_fuceng").length>0;},function(){
                $$(".XUQIU18897_fuceng").hide();
            });
        }
    }
   $$(function(){
     _tvFunc.check(function(){return $$(".ljgk").length>0;},function(){
         let link= $$(".ljgk").find("a").attr("href");
         window.location.href=link;
     });
     _app.init();
      _ctrlx.fullscreen();
   });
})();
let _data={
    vue:null,
    initData(vue,hzCallback){
        this.vue=vue;
        _tvFunc.check(function (){return $$(".vjs-fullscreen-control").length>0;},function (){
            _data.fullscreen();
            _data.hzList(hzCallback);
            _data.rateList();
            _data.xjList();
        })
    },
    fullscreen(){
      /*  setTimeout(function (){
        },5000);*/
        //_apiX.msgStr("js","$$(\".vjs-fullscreen-control\").click();");
        //_apiX.msgStr("key","F");
        //$$(".vjs-webfullscreen-control").click();
        //$$(".vjs-fullscreen-control").click();
        $$(".nav_wrapper_bg").hide();
        $$(".header_nav").hide();
        $$(".retrieve").hide();
        $$(".kj").hide();
        $$(".gwA18043_ind01").hide();
        //$$("body").css({"min-width":"100%","max-width":"100%"})
    },
    rateList(){
     //let rateNow=$$(".").text();
     $$(".vjs-playback-rate").find(".vjs-menu-item").each(function(index,item){
        let id=index.toString();
        $$(item).attr("id","xrate-"+id);
        let name= $$(item).find(".vjs-menu-item-text").text();
        let current=false;
        if($$(item).hasClass("vjs-selected")){
            current=true;
        }
        let itemData={id:id,name:name,isCurrent:current};
        if(current){
            _data.vue.now.rate=itemData;
        }
        _data.vue.rates.push(itemData);
     });
    },
    hzList(hzCallback){
        let hzNow=$$(".vjs-playback-quality-value").text();
        let currentIndex=0;
        $$(".vjs-playback-quality").find(".vjs-menu-item").each(function(index,item){
            let id=index.toString();
            let hzName = $$(item).find(".vjs-menu-item-text").text();
            $$(item).attr("id","xhz-"+id);
            let itemData={id:id,name:hzName,isVip:false,level:_tvFunc.hzLevel(hzName,2)};
            if(hzNow===hzName){
                _data.vue.now.hz=itemData;
            }
            _data.vue.hzs.push(itemData);
        });
        hzCallback();
    },
    xjList(){
        //let id=next_id;
        if(typeof(next_id)=='undefined'){
            this.xjDetail(function(){
               _data.xjListDo();
            })
        }else{
            _data.xjListDo();
        }
    },
    xjListDo(){
        let mode=0;
        if($$("script[src*='index_dhp.js']").length>0){
            mode=1;
        }
        let requestUrl=`https://api.cntv.cn/NewVideo/getVideoListByAlbumIdNew?id=${next_id}&serviceId=tvcctv&pub=1&mode=${mode}&part=0&n=100&sort=asc`;
        console.log(requestUrl);
        _apiX.getJson(requestUrl,   { "User-Agent": _apiX.userAgent(false), "tv-ref": "https://tv.cctv.com/" },function(text){
           console.log("text:: "+text);
           let data = JSON.parse(text);
           if(data.data.list.length===0){
               return;
           }
           data.data.list.forEach((item,index)=>{
            let title=`第${index+1}集`;
            //item.sc
            let itemData={vodId:next_id,id:item.id,url:item.url,isVip:false,name:item.title,remark:"",title:title,index:index,site:"cctv"};
            if(itemData.id===itemid1){
                _data.vue.now.xj=itemData;
                _tvFunc.currentXj(itemData);
            }
            _data.vue.xjs.push(itemData);
           });
        });
    },
    xjDetail(callback){
        _apiX.getJson(`https://api.cntv.cn/NewVideoset/getVideoAlbumInfoByVideoId?id=${itemid1}&serviceId=tvcctv`,
            { "User-Agent": _apiX.userAgent(false), "tv-ref": "https://tv.cctv.com/" },function(text){
                let data = JSON.parse(text);
                window.next_id=data.data.id;
                callback();
            }
        );
    }
};
