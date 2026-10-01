let  _ctrlx={
    ok(){
        $$(".play-pause").click();
        _layer.notifyLess("OK键暂停或播放")
       // _apiX.msgStr("key","SPACE");
    },
    fullscreen() {
        _tvFunc.fullscreen("#player_default")
        _tvFunc.check(function (){return $$("#player_default").length>0},function (){
           // $$("#player_default").addClass("utv-video-full");
            $$("#player_default").css("background-color","black");
            $$(".xgplayer-controls").css("position","fixed");
        });
    }
};
(function(){
    const _app={
        init(){
            _detailInit(null,999999);
        }
    };
    $$(function(){
        _app.init();
        _ctrlx.fullscreen();
    });
})();
const _data={
    vue:null,
    initData(vue,hzCallback){
       this.vue=vue;

       _tvFunc.check(function(){return $$('.control_definition [role="menuitemradio"]').length>0},function(){
        //网页全屏
         _data.fullscreen();
         _data.hzList(hzCallback);
         _data.rateList();
         /*_tvFunc.videoReady(function (){
             _data.xjList();
         });*/
         _tvFunc.check(function(){return $$(".playlist__panel__selectItem").filter(".active").length>0
             ||$$(".richPlayCard--active").length>0;},function(){
            _data.xjList();
         });
         
       });
    },
    fullscreen(){
          //音量100
        _tvFunc.volume100();
       // $$("#player_default").addClass("utv-video-full");
       // $$("#player_default").css("background-color","black");
       // $$(".xgplayer-controls").css("position","fixed");
        //关闭弹幕
        _tvFunc.check(function(){return $$(".new-danmaku-switch").length>0;},function(){
              if($$(".new-danmaku-switch-on").length>0){
                $$(".new-danmaku-switch").click();
              }
              setInterval(function(){
                $$(".xgplayer-danmu").remove();
              },2000);
        });
        this.vue.isVip=true;

    },
    getXjData(){
        let url="";
        if($$(".playlist__panel__selectItem").filter(".active").length>0){
            url= $$(".playlist__panel__selectItem").filter(".active").attr("href");
        }
        if($$(".richPlayCard--active").length>0){
            url= $$(".richPlayCard--active .richPlayCard__right").attr("href")
        }
        console.log("getXjData",url);
        let strs= url.split("?");
        let vodId=strs[0].substring(1);
        let params=  strs[1].split("&");
        let nowId=params[0].substring(3);
        return {vodId:vodId,nowId:nowId};
    },
    xjList(){
       let xjData=this.getXjData();
       let nowId = xjData.nowId;
       let vodId= xjData.vodId;
       //请求手机端html 获取数据
       let requestUrl=`https://www.ixigua.com/api/albumv2/details?albumId=${vodId}&episodeId=${nowId}`;
       console.log("requestUrl  "+requestUrl);
       _apiX.getJson(requestUrl,   { "User-Agent": _apiX.userAgent(false), "tv-ref": "https://www.ixigua.com/" },function(text){
            console.log("text "+text);
            let data = JSON.parse(text);
            let orderMap = new Map();
            $$.each(data.data.playlist,function(index,item){
                orderMap.set(item.rank,item);
            });
            let index=0;
            orderMap.forEach((item,k)=>{
                //console.log("item",item);
                let title= item.bottomLabel;
                let isVip=false;
                let id= item.episodeId;
                let url=`https://www.ixigua.com/${item.albumId}?id=${id}`;
                let remark="";
                let itemData={vodId:item.albumId,id:id,name:item.title,url:url,isVip:isVip,remark:remark,title:title,index:index,site:"xigua"};
                index++;
                if(nowId===id){
                    _data.vue.now.xj=itemData;
                    _tvFunc.currentXj(itemData);
                }
                _data.vue.xjs.push(itemData);
            });
    
       });
    },
    rateList(){
        //_PopoverMenuItem playRateBtn
        $$('.control_playbackrate [role="menuitemradio"]').each(function(index,item){
            let id=index.toString();
            $$(item).attr("id","xrate-"+id);
            let name= $$(item).text();
            let current=false;
            if($$(item).hasClass("isActive")){
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
            //let hzNow= $$(".clarityBtn").find("[class^='_Button']").text();
            let newIndex=0;
            $$('.control_definition [role="menuitemradio"]').each(function(index,item){
               let hzName = $$(item).text();
               console.log("hzName:::"+hzName);
               let isVip=false;
               if(!hzName){
                   return true;
               }
               if(hzName.includes("客户端")||hzName.trim()===""){
                    return true;
               }
               let id=newIndex.toString();
               $$(item).attr("id","xhz-"+id);
               let itemData={id:id,name:hzName,isVip:isVip,level:_tvFunc.hzLevel(hzName,1)};
               if($$(item).hasClass("isActive")){
                  _data.vue.now.hz=itemData;
              }
              _data.vue.hzs.push(itemData);
              newIndex++;
            });
            hzCallback();
    }
};
