TvFocus.up=function (){
    console.log("UPUP")
    //_apiX.msgStr("keyNum","19");
    //return true;
}
TvFocus.down=function (){
    console.log("down")
    //_apiX.msgStr("keyNum","20");
    //return true;
}
const  _ctrlx={
    ok(){
        _tvFunc.videoPlay();
        //  _apiX.msgStr("key","SPACE");
        _layer.notifyLess("OK键暂停或播放")
    },
    fullscreen() {
        // 全屏
        _tvFunc.fullscreenW("#slidelist");
        //H
       // _apiX.msgStr("keyNum","36");
       // $$("video").css("position","fixed !important")
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
            //slidelist
            _tvFunc.check(function(){return $$("#animate_qrcode_container").find("img").attr("src");},function(){
                let qr = $$("#animate_qrcode_container").find("img").attr("src");
                console.log(qr);
                _layer.open(_login.loginHtml(qr), 99999, null, "tv-index", "tv-bg-yellow");
                TvFocus.init();
            });
            _tvFunc.check(function(){return $$(".semi-button-content").length===0},function(){
                window.location.reload();
            });

        },
        loginHtml(html) {
            return `
            <div style="font-size:2vw;line-height:3vw; color: black;text-align: center;padding:1vh 1vw">该平台需要登录才可观看高清视频<br/>
            打开「抖音APP」点击左上角菜单进行扫一扫</div>
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
        console.log("isReady isReady");
        _tvFunc.check(function(){return $$("#animate_qrcode_container").length>0;},function(){
            let qrCodeLength=$$("#animate_qrcode_container").length;
            console.log("isLogin:: "+qrCodeLength)
            if(qrCodeLength>0){
                //登录
                //_login.init();
            }
        });
        _ctrlx.fullscreen();
        _app.init();
    });
})();
const _data={
    vue:null,
    initData(vue,hzCallback){
        this.vue=vue;
        _tvFunc.videoReady(function (){
            _data.fullscreen();
            _data.rateList();
        });
    },
    fullscreen(){
        //音量100
        _tvFunc.volume100();
        //
        console.log("auto auto",$$("[data-e2e=video-player-auto-play]").find("button").hasClass("xg-switch-checked"));
        if(!$$("[data-e2e=video-player-auto-play]").find("button").hasClass("xg-switch-checked")){
            console.log("auto auto");
            $$("[data-e2e=video-player-auto-play]").click();
        }
        //onsole.log("xgplayer volume",$$(".xgplayer-volume").attr("data-state"));
        if($$(".xgplayer-volume").attr("data-state")==="mute"){
            //console.log("xgplayer volume");
            $$(".xg-volume-mute").click()
        }
       // $$("[data-e2e=video-player-auto-play]")
    },
    rateList(){
        _data.vue.rates.push({id:"10X",name:"1.0",isCurrent:true,videoDo:true});
        _data.vue.rates.push({id:"10X",name:"1.25",isCurrent:true,videoDo:true});
        _data.vue.rates.push({id:"15X",name:"1.5",isCurrent:false,videoDo:true});
        _data.vue.rates.push({id:"20X",name:"2.0",isCurrent:false,videoDo:true});
        _data.vue.rates.push({id:"20X",name:"3.0",isCurrent:false,videoDo:true});
    }
};
