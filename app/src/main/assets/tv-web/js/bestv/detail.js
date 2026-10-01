const  _ctrlx={
    ok(){
        _tvFunc.videoPlay();
        //  _apiX.msgStr("key","SPACE");
        _layer.notifyLess("OK键暂停或播放")
    },
    fullscreen() {
        // 全屏
        _tvFunc.fullscreen("video-js");
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
            $$(".play_btn__qXeL_").trigger("click");
            _tvFunc.check(function(){return $$("[class^='login-modal-content_agree']").length>0;},function(){
                $$("[class^='login-modal-content_agree']").click();
                _tvFunc.check(function(){return $$("[class^='login-modal-content_code']").length>0;},function(){
                    let qr = $$("[class^='login-modal-content_code']").attr("src");
                    _layer.open(_login.loginHtml(qr), 99999, null, "tv-index", "tv-bg-yellow");
                    TvFocus.init();
                });
            });
            _tvFunc.check(function(){return $$("[class^='play_btn']").length===0;},function(){
                window.location.reload();
            });

        },
        loginHtml(html) {
            return `
            <div style="font-size:2vw;line-height:3vw; color: black;text-align: center;padding:1vh 1vw">该平台需要登录才可观看视频<br/>
            请打开手机端微信扫一扫登录</div>
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
        _tvFunc.check(function(){return $$("[class^='play_btn']").length>0;},function(){
            let btnTxt=$$("[class^='play_btn']").text();
            console.log("isLogin:: "+btnTxt)
            if(btnTxt.trim()==="登录"){
                //登录
                _login.init();
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
            _data.xjList();
        });
    },
    fullscreen(){
        //音量100
        _tvFunc.volume100();
    },
    xjList(){
        let url = window.location.href;
        let strs= url.split("web/play/");
        let ids=strs[1].split("/");
        let vid= ids[0];
        let nowId="";
        if(ids.length>1){
            nowId=ids[1];
        }
        console.log("nowId",nowId);
        //请求手机端html 获取数据
        let requestUrl=`https://www.bestv.com.cn/api/v/${vid}`;
        _apiX.getJson(requestUrl,   { "User-Agent": _apiX.userAgent(false), "tv-ref": "https://www.bestv.com.cn/" },function(text){
            let data = JSON.parse(text);
            console.log(data);
            if(null!=data.Data.Episodes){
                let index=0;
                if(nowId===""){
                    nowId=data.Data.Episodes[0].ID;
                }
                data.Data.Episodes.forEach((item,k)=>{
                    //console.log("item",item);
                    let id= item.ID;
                    let vid= item.Vid;
                    let title= _tvFunc.title(item.Num);
                    let url=`https://www.bestv.com.cn/web/play/${vid}/${id}`;
                    let itemData={vodId:vid,id:id,url:url,name:item.Title,isVip:true,remark:"",title:title,index:index,site:"bestv"};
                    index++;
                    if(nowId==id){//  必须的
                        _tvFunc.currentXj(itemData);
                        _data.vue.now.xj=itemData;
                    }
                    _data.vue.xjs.push(itemData);
                });
            }
        });
    },
    rateList(){
        _data.vue.rates.push({id:"10X",name:"1.0",isCurrent:true,videoDo:true});
        _data.vue.rates.push({id:"10X",name:"1.25",isCurrent:true,videoDo:true});
        _data.vue.rates.push({id:"15X",name:"1.5",isCurrent:false,videoDo:true});
        _data.vue.rates.push({id:"20X",name:"2.0",isCurrent:false,videoDo:true});
        _data.vue.rates.push({id:"20X",name:"3.0",isCurrent:false,videoDo:true});
    }
};
