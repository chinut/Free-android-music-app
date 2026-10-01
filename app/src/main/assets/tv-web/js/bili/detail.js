const  _ctrlx={
    ok(){
        $$(".bpx-player-ctrl-play").click();
      //  _apiX.msgStr("key","SPACE");
        _layer.notifyLess("OK键暂停或播放")
    },
    menu(){
      _data.reload();
    },
    fullscreen() {
        // 全屏
        _tvFunc.fullscreen("#bilibili-player-wrap");
        _tvFunc.check(function (){return $$("#bilibili-player").length>0},function (){
            $$(".bpx-player-sending-bar").css("display","none");
            //$$("#bilibili-player-wrap").addClass("utv-video-full");
            $$("#bilibili-player-wrap").css("padding-right","0");
            // _apiX.msgStr("key","F");
        });
    }
};
(function(){
    const _app={
        init(){
            _detailInit(null,999999);
        }
    };
    const _login={
        init(){
            _tvFunc.check(function(){return $$(".go-login-btn").length>0;},function(){
                $$(".go-login-btn").click();
                _tvFunc.check(function(){return $$(".login-scan-box").find("img").length>0&&""!==$$(".login-scan-box").find("img").attr("src");},function(){
                    let img=  $$(".login-scan-box").find("img").attr("src");
                    console.log("login-scan "+img);
                    console.log("login-scan "+_login.loginHtml(img));
                    _layer.open(_login.loginHtml(img), 99999, null, "tv-index", "tv-bg-yellow");
                    TvFocus.init();
                });
            });
          
        },
        loginHtml(html) {
            return `
            <div style="font-size:2vw;line-height:3vw; color: black;text-align: center;padding:1vh 1vw">该平台需要登录才可有高清1080P 大多无需vip 但还是建议开通<br/>
            请打开手机端哔哩哔哩扫一扫登录</div>
            <div class="tv-header tv-flex-around">
               <div class="tv-btn tv-focus"  id="tv-reload" onclick="window.location.reload();">刷新</div>
            </div>
            <div>
            <div  id="_tv_login" class="tv-tab tv-tab-flex scan-code" style="justify-content:center;">
                 <img src="${html}" height=250 width=250 />
            <div>
            </div>
           `;
        },
    }
    $$(function(){
        //__BiliUser__.isLogin
        _tvFunc.check(function(){return __playinfo__;},function(){
            let isLogin = __playinfo__.result.user_status.is_login;
            console.log("isLogin:: "+isLogin)
            if(isLogin===false){
                console.log("login not");
                _login.init();
            }else{
                 _app.init();
            }
        });
        _ctrlx.fullscreen();
    });
})();
const _data={
    vue:null,
    reload(){
        console.log("reloadreloadreloadreload")
        let newIndex=0;
        $$(".bpx-player-ctrl-quality-menu-item").each(function(index,item){
           let hzName = $$(item).find(".bpx-player-ctrl-quality-text").text();
           if(hzName&&hzName.includes("客户端")){
              return true;
           }
           let id=newIndex.toString();
           $$(item).attr("id","xhz-"+id);
           newIndex++;
        });
    },
    initData(vue,hzCallback){
       this.vue=vue;

       _tvFunc.check(function(){return $$(".bpx-player-ctrl-quality-menu-item").length>0},function(){
        //网页全屏
        _data.fullscreen();
         _data.hzList(hzCallback);
        _data.rateList();
        _data.xjList();
       });
    },
    fullscreen(){
        //$$(".bpx-player-ctrl-web").click();
        //$$(".bpx-player-sending-bar").css("display","none");
        //$$("#bilibili-player").addClass("utv-video-full");
       // _apiX.msgStr("key","F");
          //音量100
        _tvFunc.volume100();
        //关闭弹幕
       if(!$$(".bpx-player-dm-setting").hasClass("disabled")){
         $$(".bui-danmaku-switch-input").click();
       }
       $$(".bpx-player-row-dm-wrap").remove();
       let isVip=__playinfo__.result.user_status.vip_info.status==1;
        this.vue.isVip=isVip;
        console.log("isVip::: "+isVip);
        setInterval(function(){
            $$(".main-container").find("[class^='dialogcoin_coin_dialog_mask']").remove();
        },2000);

    },
    xjList(){
       let nowId = __playinfo__.result.supplement.ogv_episode_info.episode_id;
       let vodId= __playinfo__.result.supplement.ogv_season_info.season_id;
       //请求手机端html 获取数据
       let requestUrl=`https://api.bilibili.com/pgc/view/web/ep/list?ep_id=${nowId}`;
       _apiX.getJson(requestUrl,   { "User-Agent": _apiX.userAgent(false), "tv-ref": "https://www.bilibili.com/" },function(text){
            let data = JSON.parse(text);
             //console.log("data",data);
            let orderMap = new Map();
            $$.each(data.result.episodes,function(index,item){
                orderMap.set(item.title,item);
            });
            let index=0;
            orderMap.forEach((item,k)=>{
                //console.log("orderMap",item);
                let title= _tvFunc.title(item.title);
                    //`第${item.title}集`;
                let isVip=false;
                let id= item.ep_id;
                let url=`https://www.bilibili.com/bangumi/play/ep${id}`;
                let remark=item.badge;
                if(item.badge==="会员"){
                    isVip=true;
                }
                let itemData={vodId:vodId,id:id,url:url,name:item.show_title,isVip:isVip,remark:remark,title:title,index:index,site:"bili"};
                index++;
                if(nowId===id){
                    _tvFunc.currentXj(itemData);
                    _data.vue.now.xj=itemData;
                }
                _data.vue.xjs.push(itemData);
            });
    
       });
    },
    rateList(){
        //_PopoverMenuItem playRateBtn
        $$(".bpx-player-ctrl-playbackrate-menu-item").each(function(index,item){
            let id=index.toString();
            $$(item).attr("id","xrate-"+id);
            let name= $$(item).text();
            let current=false;
            if($$(item).attr("class").includes('bpx-state-active')){
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
            $$(".bpx-player-ctrl-quality-menu-item").each(function(index,item){
               let hzName = $$(item).find(".bpx-player-ctrl-quality-text").text();
               let isVip=false;
               //@TODO ？
               if(hzName&&(hzName.includes("客户端")||hzName.includes("高码率"))){
                    return true;
               }
               if( $$(item).find(".bpx-player-ctrl-quality-badge-bigvip").length>0){
                  isVip=true;
               }
               let id=newIndex.toString();
               $$(item).attr("id","xhz-"+id);
               let itemData={id:id,name:hzName,isVip:isVip,level:_tvFunc.hzLevel(hzName,1)};
               if($$(item).hasClass("bpx-state-active")){
                  _data.vue.now.hz=itemData;
              }
              _data.vue.hzs.push(itemData);
              newIndex++;
            });
            hzCallback();
    }
};
