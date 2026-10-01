const  _ctrlx={
    ok(){
       console.log("playplayplay");
       $$("[class^=player-buttons_resumeBtn]").click();
        $$("[class^=player-buttons_pauseBtn]").click();
       $$(".iqp-btn-pause").click();
       //player-buttons_resumeBtn__diOgO
        //player-buttons_pauseBtn__dnTvv
        _layer.notifyLess("OK键暂停或播放")
       // _apiX.msgStr("key","SPACE");
       //全屏
       setTimeout(function(){
          $$(".iqp-player-videolayer").css({"left":0,"top":0,"width":"100%","height":"100%"});
          $$(".statustips").hide();
         // $$(".flash-max1").remove();
       },5000);
    },
    fullscreen() {
        // 全屏
        //弹幕
   /*     _tvFunc.check(function (){
            if($$("#barrage_off").length > 0&&$$("#barrage_off").hasClass("dn")){
                console.log("弹幕关");
                $$("#barrage_switch").click();
            }
            return  $$("#barrage_off").length > 0&&!$$("#barrage_off").hasClass("dn");
        },function (){

        },2000);*/
        setInterval(function (){
            if($$("#pcaBarrageContainer").length > 0){
                $$("#pcaBarrageContainer").remove();
            }
        },2000);
        _tvFunc.fullscreenW("#video");
   /*     _tvFunc.check(function (){return $$("#video").length>0},function (){
            $$("#video").addClass("utv-video-full");
        });*/
    }
};
(function(){
    const _app={
        init(){
           // let isVip =  QiyiPlayerProphetData.v.isVIP;
           _detailInit(null,99999);
        }
    };
    const _login={
        init(){
            $$("#btn_user").click();
            $$(".simple-buttons_check_off__ivYWI").click();
            let viewBox= $$(".qrCode_qrBox__cGzov").find("svg").attr("viewBox");
            console.log("viewBox "+viewBox);
           // $$(".qrCode_qrBox__cGzov").find("svg").html("");
            _tvFunc.check(function(){return $$(".qrCode_qrBox__cGzov").find("svg").attr("viewBox").includes("41");},function(){
                let html= $$(".qrCode_qrBox__cGzov").find("svg").html();
                let menuId= _layer.open(_login.loginHtml(html), 99999, null, "tv-index", "tv-bg-yellow");
                TvFocus.init();
            });

            setInterval(function(){
                if($$(".qrCode_retryButton__TddKK").length>0){
                    $$(".qrCode_retryButton__TddKK").click();
                    setTimeout(function(){
                        let html= $$(".qrCode_qrBox__cGzov").find("svg").html();
                        $$("#tv-qrCode").html(html);
                    },2000);
                }
            },2000);
        },
        loginHtml(loginHtml) {
            //    <svg  id="tv-qrCode" height="500" viewBox="0 0 37 37" width="500" xmlns="http://www.w3.org/2000/svg">
            return `
            <div style="font-size:2vw; color: black;text-align: center;padding:1vh 1vw">该平台需要登录和最好有普通VIP(除超前点播无需电视端VIP)<br/>
             请打开手机端爱奇艺扫一扫登录</div>
            <div class="tv-header tv-flex-around">
               <div class="tv-btn tv-focus"  id="tv-reload" onclick="window.location.reload();">刷新</div>
            </div>
            <div>
            <div  id="_tv_login" class="tv-tab tv-tab-flex" style="justify-content:center;">
                <svg id="tv-qrCode"  height="200" viewBox="0 0 41 41" width="200" xmlns="http://www.w3.org/2000/svg">
                  ${loginHtml}
                </svg>         
            <div>
            </div>
           `;
        },
    }
    $$(function(){
        _tvFunc.check(function(){return $$("#btn_user").length>0;},function (){
            let loginText = $$("#btn_user").text();
            //QiyiPlayerProphetData.v.isVIP;
            if(loginText=="登录"){
                _login.init();
            }else{
                _app.init();
            }
            _ctrlx.fullscreen();
        });
    });
})();
const _data={
    vue:null,
    initData(vue,hzCallback){
       this.vue=vue;
        //#video  .iqp-txt-stream
       _tvFunc.check(function(){return $$("#right").length>0},function(){
        //网页全屏
        _data.fullscreen();
        _data.hzList(hzCallback);
        _data.rateList();
        _data.xjList();
       });
    },
    fullscreen(){
        //$$("[data-player-hook='webfullscreen']").click();
          //音量100 webfullscreen fullscreen
        //$$("#video").addClass("utv-video-full");
        _tvFunc.volume100();
        //vip
         let isVip= $$("#btn_user").parent().attr("class").includes("topbar_vipBox");
         console.log("isVip "+isVip)
         this.vue.isVip=isVip;
        //关闭弹幕 iqp-barrage-stage
        //$$("#pcaBarrageContainer").remove();
        // 悬浮图片
        //data-player-hook="plgcontainer"
       //$$("[data-player-hook='plgcontainer']").remove();//1
        //$$(".iqp-logo-box").remove();
        //跳过片头片尾
        let closed= $$("[data-player-hook='skipheadertailerbox']").hasClass("iqp-set-close");
        if(closed){
            $$("[data-player-hook='skipheadertailer']").click();
        }
        //logo
        $$(".iqp-logo-box").remove();


    },
    xjList(){
       let requestUrl=sessionStorage.getItem("iqiyiXj");
        console.log("requestUrl:: "+requestUrl);
       _apiX.getJson(requestUrl,   { "User-Agent": _apiX.userAgent(false), "tv-ref": "https://www.iqiyi.com/" },function(text){
           console.log("data:: "+text);
        let data = JSON.parse(text);
        if(data.status_code!==0){
             return;
        }
        let blocks=data.data.template.tabs[0].blocks;
        let vodId=data.data.base_data.qipu_id;
        let nowId=QiyiPlayerProphetData.tvId;
        console.log(`vodId ${vodId} nowId ${nowId}`);
        let orderMap = new Map();
        $$.each(blocks,function(index,item){
            //"bk_type": "album_episodes",
            if(item.bk_type=="album_episodes"||item.bk_id==="source_selector_bk"){
                let datas=item.data.data;
                $$.each(datas,function(i,dataItem){
                    if(typeof(dataItem.videos) == "string"){
                        return true;
                    }
                    let page=dataItem.videos.feature_paged;
                    if(page){
                        $$.each(page,function(k,v){
                            $$.each(v,function(index,item){
                                item.title=item.album_order;
                                orderMap.set(item.album_order,item);
                            });
                        })
                    }else{
                        if(Array.isArray(dataItem.videos)){
                            $$.each(dataItem.videos,function (i,itema){
                                $$.each(itema.data,function(index,itemb){
                                    if(itemb.album_order>0){
                                        itemb.title=itemb.album_order;
                                        orderMap.set(itemb.album_order,itemb);
                                    }else{
                                        orderMap.set(index,itemb);
                                    }
                                });
                            });
                        }
                    }

                });
               // return  false;
            }
        });
        let index=0;
        orderMap.forEach((item,k)=>{
            let title= _tvFunc.title(item.title);
                //`第${item.album_order}集`;
            let remark="";
            if(item.content_type===3){
                remark="预告";
            }
            console.log("item",item);
            //item.album_order+"."+item.subtitle;
            let itemData={vodId:vodId,id:item.qipu_id,url:item.page_url,name:item.short_display_name,isVip:false,remark:remark,title:title,index:index,"site":"iqiyi.html"};
            if(nowId===itemData.id){
                _data.vue.now.xj=itemData;
                _tvFunc.currentXj(itemData);
            }
            _data.vue.xjs.push(itemData);
            index++;
        });
        

        });
    },
    rateList(){
        console.log("iqp-txt-speed "+$$(".iqp-txt-speed").length)
        $$(".iqp-txt-speed").each(function(index,item){
            let id=index.toString();
            $$(item).attr("id","xrate-"+id);
            let name= $$(item).text().trim();
            let current=false;
            if($$(item).hasClass("selected")){
                current=true;
            }
            let itemData={id:id,name:name,isCurrent:current};
            if(current){
                _data.vue.now.rate=itemData;
            }
            _data.vue.rates.push(itemData);
         });
    },
    //iqp-player-videolayer 暂停广告
    hzList(hzCallback){
          //$$(".iqp-txt-stream").each(function(index,item){console.log($$(item).text());})
          //$$(".iqp-stream").each(function(index,item){console.log($$(item).text());})  iqp-txt-selected
            let hzNow= $$(".iqp-btn-definition").find(".sub").text();
            let newIndex=0;
            console.log("hzName::: "+hzNow);
            $$(".iqp-txt-stream").each(function(index,item){
               let hzName = $$(item).find(".iqp-stream").text();
               console.log("hzName::: "+hzName);
               if(hzName.includes("客户端")){
                    return;
               }
               let id=newIndex.toString();
               $$(item).attr("id","xhz-"+id);
               let itemData={id:id,name:hzName,isVip:false,level:_tvFunc.hzLevel(hzName,1)};
               if(hzName.includes(hzNow)){
                  _data.vue.now.hz=itemData;
              }
              _data.vue.hzs.push(itemData);
              newIndex++;
            });
            hzCallback();
    }
};

