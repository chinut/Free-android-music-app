let _data={
    vue:null,
    initData(vue){
       this.vue=vue;
       this.channels();
       this.channelPage();
    },
    channels(){
        _data.vue.channels.push({id:1,tag:"tv",name:"电视剧",sort:7,loading:false,filter:Filter.default,pageNum:0,vods:[]});
        _data.vue.channels.push({id:2,tag:"zy",name:"综艺",loading:false,filter:Filter.default,pageNum:0,vods:[]});
        _data.vue.channels.push({id:3,tag:"dm",name:"动漫",loading:false,filter:Filter.default,pageNum:0,vods:[]});
        _data.vue.channels.push({id:4,tag:"se",name:"少儿",loading:false,filter:Filter.default,pageNum:0,vods:[]});
        _data.vue.channels.push({id:5,tag:"movie",name:"电影",loading:false,filter:Filter.hot,pageNum:0,vods:[]});
        _data.vue.channels.push({id:6,tag:"zs",name:"教育",loading:false,filter:Filter.default,pageNum:0,vods:[]});
        _data.vue.channels.push({id:7,tag:"wh",name:"文化",loading:false,filter:Filter.default,pageNum:0,vods:[]});
        _data.vue.channels.push({id:8,tag:"jlp",name:"纪录片",loading:false,filter:Filter.default,pageNum:0,vods:[]});
        _data.vue.channels.push({id:9,tag:"music",name:"音乐",loading:false,filter:Filter.default,pageNum:0,vods:[]});
        _data.vue.filters=[Filter.default,Filter.hot,Filter.new,Filter.best];
    },
    genSort(filter){
        if(filter===Filter.default){
            return "";
        }
        if(filter===Filter.hot){
            return `,"sort":7`;
        }
        if(filter===Filter.new){
            return `,"sort":1`;
        }
        if(filter===Filter.best){
            return `,"sort":3`;
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
        let pageNum=channelItem.pageNum+1;
        let sort= this.genSort(channelItem.filter);
        let filter= encodeURIComponent(`{"type":"${channelItem.name}"${sort}}`)
        //{"scene":"search_component_paging","id":227939}
        let session= encodeURIComponent(`{"scene":"search_component_paging","id":227939}`);
        //https://mesh.if.iqiyi.com/portal/lw/videolib/data?version=12.94.20247&channel_id=${channelItem.id}&page_id=${pageNum}&filter=%7B%22mode%22%3A%2211%22%7D
        let requestUrl=`https://www.youku.com/category/data?session=${session}&params=${filter}&pageNo=${pageNum}`;
        //_tvFunc.paramStr({p:channelItem.pageNum+1,fc:channelItem.name}); %7B%22mode%22%3A%2211%22%7D %7B%22mode%22%3A%224%22%7D

        _apiX.getJson(requestUrl,
            { "User-Agent": _apiX.userAgent(false), "tv-ref": "https://www.youku.com/" },
            function(text){
            //console.log(""+text);
                channelItem.loading=false;
                if(null==text){
                    return;
                }
                let data = JSON.parse(text);
                channelItem.pageNum++;
                data.data.filterData.listData.forEach(item => {
                    let imageUrl=_tvFunc.image(item.img+"?x-oss-process=image/resize,w_315/interlace,1/quality,Q_80");
                    let remark=_data.remark(item);
                    let url=item.videoLink;
                    if(url.startsWith("//")){
                        url="https:"+url;
                    }
                    let vodId=_data.getVodId(url);
                    channelItem.vods.push({id:vodId,name:item.title,pic:imageUrl,url:url,remark:remark,"site":"youku"});
                });
            },function () {
                channelItem.loading=false;
            }
        )
    },
    getVodId(link){
        //videoLink/v.youku.com/v_nextstage/id_bdceb5419210420dabdc.html?s=bdceb5419210420dabdc
        let ids= link.split("?s=");
        if(ids.length>1){
            return ids[1];
        }
        return null;
    },
    remark(item){
        let remark=" "+item.summary.replace("更新至","今");
        return remark.replace("全","");
    }
};
 $(function(){
    _tvHtmlInit();
    _tvFunc.check(function(){return $("#full_mask_layer_id").length>0;},function(){
        $("#full_mask_layer_id").remove();
    });
  
 });