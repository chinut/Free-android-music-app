var _data={
    vue:null,
    initData(vue){
        this.vue=vue;
        this.channels();
        this.channelPage();
    },
    channels(){
        _data.vue.channels.push({id:1,tag:"tv",name:"电视剧",loading:false,filter:Filter.new,pageNum:0,vods:[]});
        _data.vue.channels.push({id:1,tag:"zy",name:"综艺",loading:false,filter:Filter.new,pageNum:0,vods:[]});
        _data.vue.channels.push({id:5,tag:"movie",name:"电影",loading:false,filter:Filter.default,pageNum:0,vods:[]});
        //_data.vue.channels.push({id:7,tag:"zy",name:"综艺",loading:false,pageNum:0,vods:[]});
        _data.vue.filters=[Filter.default,Filter.hot,Filter.new,Filter.best];
    },
    genSort(filter){
        if(filter===Filter.default){
            return "";
        }
        if(filter===Filter.hot){
            return "&sort=U";
        }
        if(filter===Filter.new){
            return "&sort=R";
        }
        if(filter===Filter.best){
            return "&sort=S";
        }
        return "";
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
        channelItem.loading=true;
        let pageSize=20;
        let startNum=pageSize*channelItem.pageNum;
        let sort= this.genSort(channelItem.filter);
        let type="tv";
        if(channelItem.tag==="movie"){
            type="movie";
        }
        //https://www.youku.com/category/data?session=${session}&params=${filter}&pageNo=${pageNum}
        let requestUrl=`https://m.douban.com/rexxar/api/v2/${type}/recommend?start=${startNum}&count=${pageSize}${sort}&playable=true&tags=${channelName}`;
        console.log(requestUrl);
        _apiX.getJson(requestUrl,
            { "User-Agent": _apiX.userAgent(false), "tv-ref": "https://movie.douban.com" },
            function(text){
                channelItem.loading=false;
                if(null==text){
                    return;
                }
                let data = JSON.parse(text);
                channelItem.pageNum++;
                data.items.forEach(item => {
                    let imageUrl=_tvFunc.image(item.pic.normal);
                    let remark=_data.remark(item);
                    let url=`https://movie.douban.com/subject/${item.id}/`;//36534750
                    channelItem.vods.push({id:item.id,name:item.title,pic:imageUrl,url:url,remark:remark,"site":"@douban"});
                });
            },function () {
                channelItem.loading=false;
            }
        )
    },
    remark(item){
        let remark=" "+item.episodes_info.replace("更新至","今");
        return remark.replace("全","");
    }
};
$(function(){
    _tvHtmlInit();
   // _layer.notify("该平台汇总了全网视频 但部分视频是不可以播放的 还请见谅",null,1.5)
});