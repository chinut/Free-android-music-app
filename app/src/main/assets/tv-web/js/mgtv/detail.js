const  _ctrlx={
    ok(){
       $$(".icon-play").click();
       $$(".icon-paused").click();
        _layer.notifyLess("OK键暂停或播放")
       // _apiX.msgStr("key","SPACE");
       setTimeout(function(){
           $$(".mango-control-bar .ControlBar").removeClass("active");
           $$(".as-pause-full_snapshot").removeClass("small");
       },3000);
    },
    fullscreen() {
         _tvFunc.addKeyFullScreen(document.getElementById("mgtv-player-wrap"));
        //_tvFunc.fullscreen("#mgtv-player-wrap");
        $$("body").css({"min-width":"100%"});
        _tvFunc.check(function (){return $$("#mgtv-player-wrap").length>0},function (){
            $$("#mgtv-player-wrap").addClass("utv-video-full");
            $$(".m-playwrap").css({"min-width":"100%"});
            $$(".m-topheader").css({"min-width":"100%"});
            $$(".g-container-star").css({"min-width":"100%"});
            $$(".m-playwrap").css({"min-width":"100%"});
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
           $$(".user img").click();
           setTimeout(function(){
            //.scan-code .code-img
              $$(".protocol-agree").click();
             //let html= $$(".scan-code .code-img").html();
             let menuId= _layer.open(_login.loginHtml(""), 99999, null, "tv-index", "tv-bg-yellow");
               TvFocus.init();
             $$("#_tv_login").append($$(".scan-code .code-img"));
             _tvFunc.check(function(){return $$(".code-success-img").length>0;},function(){
                window.location.reload();
             },1000,600);
           },2000);
        },
        loginHtml(html) {
            return `
            <div style="font-size:2vw;line-height:3vw; color: black;text-align: center;padding:1vh 1vw">该平台需要登录和最好有普通VIP(除超前点播无需电视端VIP)<br/>
            请打开手机端芒果TV扫一扫登录</div>
            <div class="tv-header tv-flex-around">
               <div class="tv-btn tv-focus"  id="tv-reload" onclick="window.location.reload();">刷新</div>
            </div>
            <div>
            <div  id="_tv_login" class="tv-tab tv-tab-flex scan-code" style="justify-content:center;">
                 
            <div>
            </div>
           `;
        },
    }
    $$(function(){
        console.log("isReady:: ")
        _tvFunc.check(function(){return $$("#m-topheader .user").length>0;},function(){
            //<li class="user"><a href="javascript:;" mg-stat-click="" mg-stat-custom="cont=登录&amp;direct=cntp&amp;addr=c_personal" title="个人信息" class="navbtn"><img src="https://img.mgtv.com/imgotv-channel/6.0.5/pcweb-header/user/login-1s.jpg" alt="默认头像"></a></li>
            let loginNum = $$("#m-topheader .user").find(".vip").length; 
            //yklogininstance.isLoginStatus;
            console.log("isLogin:: "+loginNum)
            if(loginNum===0){
                console.log("login not");
                //_app.init();
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
    initData(vue,hzCallback){
       this.vue=vue;
       _tvFunc.check(function(){return $$(".clarityBtn").length>0},function(){
        //网页全屏
        _data.fullscreen();
        _data.hzList(hzCallback);
        _data.rateList();
       });
        _data.xjList();
    },
    fullscreen(){
        //$$(".webfullscreenBtn .icon").click();
          //音量100
        //$$(".fullscreenBtn .icon").click();
       // _apiX.msgStr("key","F");
        $$("#mgtv-player-wrap").addClass("utv-video-full");
       // setTimeout(function(){$$("body").css({"width":"100vw"});},5000);
        _tvFunc.volume100();
        //关闭弹幕
         $$(".danmu_toolbar").find("[class^='_danmuSwitcher']").click();
        $$(".mango-danmu-layer").remove();
        //跳过片头片尾 #youku-pause-container
        //vip
        let isVip = $$(".user .vip").find("use").length>0;
        console.log("isVip "+isVip);
        this.vue.isVip=isVip;

    },
    totalPage:1,
    xjList(){
        _data.xjListPage(1);
    },

    xjListPage(pageNo){
       let nowId =mgtvPlayer.vid;
       let vodId= mgtvPlayer.config.cid;
       //请求手机端html 获取数据
       let requestUrl=`https://pcweb.api.mgtv.com/episode/list?video_id=${nowId}&page=${pageNo}&size=50&platform=4&src=mgtv`;
        console.log("requestUrl::"+requestUrl);
        _apiX.getJson(requestUrl,   { "User-Agent": _apiX.userAgent(false), "tv-ref": "https://www.mgtv.com/" },function(text){
         console.log("text::"+text);   
        let data = JSON.parse(text);
            let orderMap = new Map();
            $$.each(data.data.list,function(index,item){
                orderMap.set(item.t1,item);
            });

            let itemNum=0;
            orderMap.forEach((item,k)=>{
                let title= _tvFunc.title(item.t1);
                console.log("title :: "+title);
                let remark="";
                if(item.isIntact!=="1"){
                    title=item.t1;
                    if(item.isIntact==="3"){
                        remark="预告";
                    }
                }
                let isVip=true;
                let id= item.video_id;
                let url=`https://www.mgtv.com/b/${vodId}/${id}.html`;
                if(item.isvip==="1"){
                    isvip=true;
                }
                let itemData={vodId:vodId,id:id,url:url,isVip:isVip,name:item.t3,remark:remark,title:title,index:itemNum,site:"mgtv"};
                itemNum++;

                if(nowId===id){
                    console.log("nowId",nowId,id);
                    _data.vue.now.xj=itemData;
                    _tvFunc.currentXj(itemData);
                }
                _data.vue.xjs.push(itemData);
            });
            console.log("{{{{{}}}}}",data,data.total_page)
            if(data.data.current_page!=data.data.total_page){
                _data.xjListPage(pageNo+1);
            }
    
       });
    },
    rateList(){
        //_PopoverMenuItem playRateBtn
        $$(".playRateBtn").find("[class^='_PopoverMenuItem']").each(function(index,item){
            let id=index.toString();
            $$(item).attr("id","xrate-"+id);
            let name= $$(item).text();
            let current=false;
            if($$(item).attr("class").includes('PopoverMenuItemActive')){
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
            _tvFunc.check(function(){ return $$(".clarityBtn").find("[class^='_barName']").length>1},function(){
              
                let hzNow= $$(".clarityBtn").find("[class^='_Button']").text();
                hzNow=hzNow.replace("SDR","").trim();
                //$$("[com='quality']").find(".control-label").text();
                //$$(".iqp-btn-definition").find(".sub").text();
                let newIndex=0;
                $$(".clarityBtn").find("[class^='_barName']").each(function(index,item){
                   let hzName = $$(item).text();
                   let isVip=false;
                   if(hzName.includes("客户端")){
                        return;
                   }
                   if(hzName.includes("VIP")){
                      isVip=true;
                      hzName=hzName.replace("VIP","");
                   }
                   let id=newIndex.toString();
                   $$(item).attr("id","xhz-"+id);
                   let itemData={id:id,name:hzName,isVip:isVip,level:_tvFunc.hzLevel(hzName,1)};
                   if(hzName.includes(hzNow)){
                      _data.vue.now.hz=itemData;
                  }
                  _data.vue.hzs.push(itemData);
                  newIndex++;
                });
                hzCallback();
            },1000,150);
    }
};
