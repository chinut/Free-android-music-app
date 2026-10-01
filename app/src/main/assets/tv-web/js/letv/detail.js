const  _ctrlx={
    ok(){
        _tvFunc.videoPlay();
        //  _apiX.msgStr("key","SPACE");
        _layer.notifyLess("OK键暂停或播放")
    },
    fullscreen() {
        // 全屏
        $$(".le_head").hide();
        $$(".hv_wm_logo").hide();
        _tvFunc.fullscreen("#video");

    }
};
(function(){
    const _app={
        init(){
            _detailInit(null,999999);
        }
    };
    $$(function(){
        console.log("isReady isReady");
        if($$("#top_play").length>0){
           let url=  $$("#top_play").attr("href");
            window.location.href=url;
            return;
        }
        _ctrlx.fullscreen();
        _app.init();
    });
})();
const _data={
    vue:null,
    initData(vue,hzCallback){
        this.vue=vue;
        this.vue.isVip=false;
        _tvFunc.videoTrueReady(function (){
            _data.fullscreen();
            _data.rateList();
            _data.hzList(hzCallback);
            _data.xjList();
        },40);
    },
    fullscreen(){
        //音量100
        _tvFunc.volume100();
    },
    hzList(hzCallback){
        //let hzNow=$$(".vjs-playback-quality-value").text();
        // let currentIndex=0;
        $$(".hv_clear_panel li").each(function(index,item){
            let id=index.toString();
            let hzName = $$(item).text().trim();
            if(hzName.includes("APP")){
                return true;
            }
            $$(item).attr("id","xhz-"+id);
            let itemData={id:id,name:hzName,isVip:false,level:_tvFunc.hzLevel(hzName,1)};
            if($$(item).hasClass("hover")){
                _data.vue.now.hz=itemData;
            }
            _data.vue.hzs.push(itemData);
        });
        hzCallback();
    },
    xjList(){
        let nowId=__INFO__.vid;
        let pid=__INFO__.pid;
        console.log("nowId",nowId);
        //请求手机端html 获取数据
        let requestUrl=`https://d-api-m.le.com/card/dynamic?platform=pc&vid=${nowId}&pagesize=100&type=episode&page=1`;
        _apiX.getJson(requestUrl,   { "User-Agent": _apiX.userAgent(false), "tv-ref": "https://www.le.com/" },function(text){
            let data = JSON.parse(text);
            console.log(data);
            if(null!=data.data.episode){
                let index=0;
                if(nowId===""){
                    nowId=data.Data.Episodes[0].ID;
                }
                data.data.episode.videolist.forEach((item,k)=>{
                    //console.log("item",item);
                    let id= item.vid;
                    let vid= pid;
                    let title= _tvFunc.title(item.key);
                    let url=item.url;
                    let itemData={vodId:vid,id:item.vid,url:url,name:item.title,isVip:false,remark:"",title:title,index:index,site:"letv"};
                    index++;
                    if(nowId===id){//  必须的
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
