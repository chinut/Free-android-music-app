let _data={
    vue:null,
    initData(vue){
       this.vue=vue;
       this.channels();
       this.channelPage();
    },
    channels(){
        _data.vue.channels.push({id:2,tag:"tv",name:"电视剧",loading:false,filter:Filter.default,pageNum:0,vods:[]});
        _data.vue.channels.push({id:6,tag:"zy",name:"综艺",loading:false,filter:Filter.default,pageNum:0,vods:[]});
        _data.vue.channels.push({id:4,tag:"dm",name:"动漫",loading:false,filter:Filter.default,pageNum:0,vods:[]});
        _data.vue.channels.push({id:15,tag:"se",name:"少儿",loading:false,filter:Filter.default,pageNum:0,vods:[]});
        _data.vue.channels.push({id:12,tag:"zs",name:"知识",loading:false,filter:Filter.default,pageNum:0,vods:[]});
        _data.vue.channels.push({id:1,tag:"movie",name:"电影",loading:false,filter:Filter.default,pageNum:0,vods:[]});
        _data.vue.channels.push({id:3,tag:"jlp",name:"纪录片",loading:false,filter:Filter.default,pageNum:0,vods:[]});
        _data.vue.filters=[Filter.default,Filter.hot,Filter.new,Filter.best];
    },
    genSort(filter){
        if(filter===Filter.default){
            return "24";
        }
        if(filter===Filter.hot){
            return "11";
        }
        if(filter===Filter.new){
            return "4";
        }
        if(filter===Filter.best){
            return "8";
        }
        return "11";
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
        //version=12.94.20247 uid=2031491668 &dfp=a0084c0da76be643b0a05372146eea18b798c84cee1ed81771b4319b9851e353f2
        let requestUrl=`https://pub.m.iqiyi.com/h5/main/recVideos/lib/?page_id=${pageNum}&mode=${sort}&channel_id=${channelItem.id}&post=list&from=mobile_videolib&is_unified_interface=1&version=1.0.0&play_platform=H5_QIYI`;
            //`https://mesh.if.iqiyi.com/portal/lw/videolib/data?uid=&device_id=6d737a6d3077a5ffff16e45e6c970467&channel_id=${channelItem.id}&page_id=${pageNum}&filter=%7B%22mode%22%3A%22${sort}%22%7D`;
        //_tvFunc.paramStr({p:channelItem.pageNum+1,fc:channelItem.name}); %7B%22mode%22%3A%2211%22%7D %7B%22mode%22%3A%224%22%7D
        _apiX.getJson(requestUrl,
            { "User-Agent": _apiX.userAgent(false), "tv-ref": "https://www.iqiyi.com/" },
            function(text){
               console.log("text:: ",text);
                channelItem.loading=false;
                let data = JSON.parse(text);
                channelItem.pageNum++;
                data.data.videos.forEach(item => {
                  /*  if(!item.image_url_normal){
                        return true;
                    }*/
                    let imageUrl=_tvFunc.image(_tvFunc.link(item.imageUrl));
                    let remark=_data.remark(item);
                    let title=item.albumName;
                    if(!title&&""===title){
                        title=item.shortTitle;
                    }
                    channelItem.vods.push({id:item.albumId,name:title,pic:imageUrl,
                        url:_tvFunc.link(item.pageUrl.replace("m.iqiyi.com","www.iqiyi.com")),remark:remark,"site":"iqiyi.html"});
                });
            },function () {
                channelItem.loading=false;
            }
        )
    },
    remark(item){
        let remark=" "+item.lowerRightCorner.replace("更新至","今");
        return remark.replace("全","");
    }
};
 $(function(){
    _tvHtmlInit();
    _tvFunc.check(function(){return $("#full_mask_layer_id").length>0;},function(){
        $("#full_mask_layer_id").remove();
    });
 });