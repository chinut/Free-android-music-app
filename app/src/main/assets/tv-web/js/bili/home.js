var _data={
    vue:null,
    initData(vue){
       this.vue=vue;
       this.channels();
       this.channelPage();
       //this.test();
    },
    channels(){
        _data.vue.channels.push({id:4,tag:"gc",name:"国创",loading:false,filter:Filter.hot,pageNum:0,vods:[]});
        _data.vue.channels.push({id:1,tag:"fj",name:"番剧",loading:false,filter:Filter.hot,pageNum:0,vods:[]});
        _data.vue.channels.push({id:5,tag:"tv",name:"电视剧",loading:false,filter:Filter.hot,pageNum:0,vods:[]});
        _data.vue.channels.push({id:7,tag:"zy",name:"综艺",loading:false,filter:Filter.hot,pageNum:0,vods:[]});
        _data.vue.channels.push({id:2,tag:"movie",name:"电影",loading:false,filter:Filter.hot,pageNum:0,vods:[]});
        //_data.vue.channels.push({id:115,tag:"zs",name:"教育",loading:false,pageNum:0,vods:[]});
        _data.vue.channels.push({id:3,tag:"jlp",name:"纪录片",loading:false,filter:Filter.hot,pageNum:0,vods:[]});
        _data.vue.filters=[Filter.default,Filter.hot,Filter.new,Filter.best];
    },
    test(){
        _apiX.getJson("https://api.bilibili.com/x/web-interface/history/cursor?max=0&view_at=0&business=",
            { "User-Agent": _apiX.userAgent(false), "tv-ref": "https://www.bilibili.com/" },
            function(text){
                console.log("history: "+text);
            });
    },
    genSort(filter){
        if(filter===Filter.default){
            return "2";
        }
        if(filter===Filter.hot){
            return "3";
        }
        if(filter===Filter.new){
            return "0";
        }
        if(filter===Filter.best){
            return "4";
        }
        return "2";
    },
    channelPage(channelItem){
        if(!channelItem){
            channelItem=this.vue.channels[0];
        }
        let channelName=channelItem.name;
        if (channelItem.loading) {
            console.warn("已有数据真正加载中");
            return;
        }
        let sort= this.genSort(channelItem.filter);
        channelItem.loading=true;
        let pageNum=channelItem.pageNum+1;
        //https://www.youku.com/category/data?session=${session}&params=${filter}&pageNo=${pageNum}
        let requestUrl=`https://api.bilibili.com/pgc/season/index/result?order=${sort}&sort=0&page=${pageNum}&season_type=${channelItem.id}&pagesize=20&type=1`;
        console.log(requestUrl);
        _apiX.getJson(requestUrl,
            { "User-Agent": _apiX.userAgent(false), "tv-ref": "https://www.bilibili.com/" },
            function(text){
    
                channelItem.loading=false;
                if(null==text){
                    return;
                }
                let data = JSON.parse(text);
                console.log(data)
                channelItem.pageNum++;
                data.data.list.forEach(item => {
                  
                    let imageUrl=_tvFunc.image(item.cover+"@480w_640h.webp");
                    let remark=_data.remark(item);
                    let url=item.link;
                    channelItem.vods.push({id:item.season_id,name:item.title,pic:imageUrl,url:url,remark:remark,"site":"bili"});
                });
            },function () {
                channelItem.loading=false;
            }
        )
    },
    remark(item){
        let remark=" "+item.index_show.replace("更新至","今");
        return remark.replace("全","");
    }
};
 $(function(){
    _tvHtmlInit();
 });