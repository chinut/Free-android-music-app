const  _ctrlx={
    ok(){
        _tvFunc.videoPlay();
        //  _apiX.msgStr("key","SPACE");
        _layer.notifyLess("OK键暂停或播放")
    },
    fullscreen() {
        // 全屏
        _tvFunc.fullscreen("#mod-player");
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
            $$(".header-user").click();
            //$$("#qrccode").click() $$(".qr-code-img").attr("src")
            _tvFunc.check(function(){return $$("#login_frame").length>0;},function(){
                let url=  $$("#login_frame").attr("src");
                window.location.href=url;
            });
         /*   _tvFunc.check(function(){return $$("[class^='play_btn']").length===0;},function(){
                window.location.reload();
            });*/

        }
    }
    $$(function(){
        console.log("isReady isReady");
        _tvFunc.check(function(){return mgUserInfo;},function(){
            let isLogin=mgUserInfo.info.isLogin;
            console.log("isLogin:: "+isLogin)
            if(!isLogin){
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
            _data.hzList(hzCallback);
            //_data.xjList();
        });
    },
    fullscreen(){
        //音量100
        _tvFunc.volume100();
    },
    hzList(hzCallback){
        //let hzNow=$$(".vjs-playback-quality-value").text();
       // let currentIndex=0;
        $$(".item-rt").each(function(index,item){
            let id=index.toString();
            let hzName = $$(item).text().trim();
            $$(item).attr("id","xhz-"+id);
            let itemData={id:id,name:hzName,isVip:false,level:_tvFunc.hzLevel(hzName,1)};
            if($$(item).hasClass("cur")){
                _data.vue.now.hz=itemData;
            }
            _data.vue.hzs.push(itemData);
        });
        hzCallback();
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
