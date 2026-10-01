
const _ctrlx={
    ok(){
        console.log("playplayplay");
        //_apiX.msgStr("key","SPACE");
        $$(".txp_btn_play").click();
        _layer.notifyLess("OK键暂停或播放")
        //暂停广告 全屏
        setTimeout(function(){
            $$(".txp_videos_container").css({"left":0,"top":0,"width":"100%","height":"100%"});
            $$(".txp_full_screen_pause-player-mask").css({"left":0,"top":0,"width":"100%","height":"100%"});
            $$(".txp_full_screen_pause-bg").remove();
            $$("[data-role='creative-player-pause-layer']").remove();//data-role="creative-player-pause-layer"
        },5000);
    },
    menu(){
      _data.reload();
    },
    fullscreen() {
       // _tvFunc.fullscreen("#player-container");
       // _tvFunc.addKeyFullScreen(document.getElementById("player-container"));
      /*  _tvFunc.check(function (){return $$("#player-container").length>0},function (){
            $$("#ssi-header").hide();
           // $$("#player-container").addClass("utv-video-full");
        });*/
    }
};
(function(){  
  let   _login={
        wxLoginUrl:null,
        check(callback){
            _tvFunc.check(function(){return typeof(txv)!='undefined';},function(){
                let isLogin = txv.login.isLogin();
                callback(isLogin);
            });
        },
        init(){
            $$(".btn_pop_link").click();
            _tvFunc.check(function(){return $$(".policy-module_select-box__-61C9").length>0},function(){
                $$(".policy-module_select-box__-61C9").click();
                $$(".login-button-module_wx-buttton-container__FE7En").click();
            });
        },
        loginQr(qrUrl,type){
            console.log("qrUrl "+qrUrl);
            if(null!=_loginx.qrUrl&&qrUrl===_loginx.qrUrl){
                return;
            }
            _loginx.qrUrl=qrUrl;
            _layer.close(_loginx.menuId);
            _loginx.menuId=_layer.open(_login.wxLoginIframe(qrUrl, type), 99999, null, "tv-index", "tv-bg-yellow");
            TvFocus.init();
        },
        wxLoginIframe(qrUrl,type) {
            let qqBtn=` <div class="tv-btn" id="tv-qq" onclick="_loginx.qqlogin()">切换qq扫码</div>`;
            if(type.includes("qq")){
                qqBtn="";
            }
            return `
            <div style="font-size:2vw; color: black;text-align: center;padding:1vh 1vw">该平台需要登录和最好有普通VIP(除超前点播无需电视端VIP)<br/>
            请打开${type}扫一扫登录</div>
            <div class="tv-header tv-flex-around">
               <div class="tv-btn tv-focus"  id="tv-reload" onclick="window.location.reload();">刷新</div>
               ${qqBtn}
            </div>
            <div class="tv-tab tv-tab-flex" style="justify-content:center;">
                     <img src="${qrUrl}" width="250" height="250"/>      
            </div>`;
        }
    };
    _app={
        init(){
           _detailInit(null,999999);
        }
    }
    window._loginQr=function(url,type){
        _login.loginQr(url,type);
    }
    $$(function(){
        //是否登录
        _login.check(function(isLogin){
            if(isLogin){
                _app.init();
            }else{
                _login.init();
            }
        });
        _ctrlx.fullscreen();
    });
   
})();
let  _loginx={
    menuId:null,
    qrUrl:null,
    qqlogin(){
        //login-button-module_qq-button-container__c7odm
        _layer.close(this.menuId);
        _layer.notify("切换qq登录中 请稍等。。。。");
        $$("[class^='main-login-wnd-module_back-button']").trigger("click");
        _tvFunc.check(function(){return $$(".login-button-module_qq-button-container__c7odm").length>0;},function(){
            $$(".login-button-module_qq-button-container__c7odm").trigger("click");
        });
       /* _tvFunc.check(function(){return _tvFunc.sessionStorageCheckTime("qqQr");},function(){
            let qrUrl =   sessionStorage.getItem("qqQr");
            console.log("qrUrl "+qrUrl);
            _layer.open(_login.wxLoginIframe(qrUrl,"手机端qq"), 99999, null, "tv-index", "tv-bg-yellow");
        });*/
    },
};
let _data={
    vue:null,
    reload(){
        console.log("reloadreloadreloadreload")
        $$(".txp_btn_definition").find(".txp_menuitem").each(function(index,item){
            if($$(this).text().includes("客户端")){
                return;
            }
            let hzId= $$(this).attr("data-value");
           $$(item).attr("id","xhz-"+hzId);
       });
    },
    initData(vue,hzCallback){
       this.vue=vue;
       _tvFunc.check(function(){return $$(".txp_btn_definition").find(".txp_menuitem").length>0;},function(){
        _data.fullscreen();
        _data.hzList(hzCallback);
        _data.rateList();
       // _data.dm();
       },1000,200);
        _data.xjList();
    },
    fullscreen(){
      //全屏
       //$$("#ssi-header").hide();
        //网页全屏
       $$(".txp_btn_fake").click();
       // $$(".txp_btn_fullscreen").click();
      //  $$("#player-container").addClass("utv-video-full");
      //  _apiX.msgStr("key","F");
        _tvFunc.volume100();
/*       _apiX.msgStr("key","SPACE");
       setTimeout(function (){
           _apiX.msgStr("key","SPACE");
       },1000);*/
       _tvFunc.check(function(){return $$(".barrage-switch").length>0;},function(){
        let barrageTitle =  $$(".barrage-switch").attr("title");
        if(barrageTitle==="关闭弹幕"){
            $$(".barrage-switch").click();
        }
        $$(".player-play-title").hide();
        //$$("#GameDiv").remove();
        //写当前数据
       });
    },
    getVodData(){
        let id=__PINIA__.global.currentVid;
        let nextId=null;
        let hasNext=false;
        if(__PINIA__.epiosode.nextVideoInfo){
            nextId=__PINIA__.epiosode.nextVideoInfo.vid;
            hasNext=true;
        }
        let vodId=__PINIA__.global.currentCid;
        let isVip=__PINIA__.payVipStatus.newPayStatus.isVip;
        return {id:id,nextId:nextId,vodId:vodId,isVip:isVip,hasNext:hasNext};
    },
    xjList(){
        let vodId=__PINIA__.global.currentCid;
        let vId=__PINIA__.global.currentVid;
        _apiX.postJson("https://pbaccess.video.qq.com/trpc.universal_backend_service.page_server_rpc.PageServer/GetPageData?vplatform=2&vversion_name=8.2.96",
            {"User-Agent": _tvFunc.userAgent(true),"tv-ref":"https://m.v.qq.com/"},
            {"page_params":{"req_from":"web_mobile","page_id":"vsite_episode_list","page_type":"detail_operation","id_type":"1","page_size":"100",
                "cid":vodId,"detail_page_type":"1"},"has_cache":1},
            function(text){
             console.log("text"+text); 
             let data=JSON.parse(text);
             if(data.ret!=0){
                return;
             }
            let vodItems= data.data.module_list_datas[0].module_datas[0].item_data_lists.item_datas;
            let orderMap = new Map();
            $$.each(vodItems,function(i,value){
                let item= value.item_params;
                orderMap.set(item.title,item);
            });
            let itemNum=0;
            orderMap.forEach((item,k)=>{
                let remark="";
                let isVip = false;
                if(item.uni_imgtag){
                   let imgTag=  JSON.parse(item.uni_imgtag);
                   let text= imgTag.tag_2.text;
                   if(text!=""){
                      remark= text;
                   }
                   if(text=='VIP'){
                     isVip=true;
                   }
                }
                let title=_tvFunc.title(item.title);
                    //`第${item.title}集`;
                if(title.includes("采访")||title.includes("彩蛋")){
                    title=item.title;
                }
                //console.log("item",item);
                let url =`https://v.qq.com/x/cover/${item.cid}/${item.vid}.html`;
                let itemData={vodId:item.cid,id:item.vid,name:item.play_title,url:url,isVip:isVip,remark:remark,title:title,index:itemNum,site:"qq"};
                itemNum++;
                if(itemData.id===vId){
                    _tvFunc.currentXj(itemData);
                    _data.vue.now.xj=itemData;
                }
                _data.vue.xjs.push(itemData);
            });
        });    
    },
    hzList(hzCallback){
        //fhd
        $$(".txp_btn_definition").find(".txp_menuitem").each(function(index,item){
                if($$(this).text().includes("客户端")){
                    return;
                }
               let hzName= $$(this).attr("data-payload");
               let hzId= $$(this).attr("data-value");
               let current = $$(this).hasClass("txp_current");
               let hzVip=$$(this).text().includes("VIP");
               $$(item).attr("id","xhz-"+hzId);
               let itemData={id:hzId,name:hzName,isVip:hzVip,level:_tvFunc.hzLevel(hzName,1)};
               if(current){
                  _data.vue.now.hz=itemData;
               }
               _data.vue.hzs.push(itemData);
               //画质低于1080 默认1080
        });
        hzCallback();
    },
    rateList(){
        $$(".txp_btn_playrate").find(".txp_menuitem").each(function(index,item){
            let text=$$(this).attr("data-label");
            let id = text.replace(".","");
            $$(item).attr("id","xrate-"+id);
            let itemData={id:id,name:text};
            _data.vue.rates.push(itemData);
            if($$(this).hasClass("txp_current")){
                _data.vue.now.rate=itemData;
            }
        });
    },
    dm(){
        _tvFunc.check(function(){return $$(".barrage-switch").length>0;},function(){
            let barrageTitle =  $$(".barrage-switch").attr("title");
            if(barrageTitle=="关闭弹幕"){
                barrageTitle="开启弹幕";
                $$(".barrage-switch").click();
            }
            _data.vue.now.dm=barrageTitle;
            //写当前数据
        });
    }
};
