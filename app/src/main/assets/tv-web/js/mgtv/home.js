let _data={
    vue:null,
    initData(vue){
       this.vue=vue;
       this.channels();
       this.channelPage();
    },
    channels(){
        _data.vue.channels.push({id:2,tag:"tv",name:"电视剧",sort:7,loading:false,filter:Filter.hot,pageNum:0,vods:[]});
        _data.vue.channels.push({id:1,tag:"zy",name:"综艺",loading:false,filter:Filter.hot,pageNum:0,vods:[]});
       // _data.vue.channels.push({id:50,tag:"dm",name:"动漫",loading:false,pageNum:0,vods:[]});
        _data.vue.channels.push({id:10,tag:"se",name:"少儿",loading:false,filter:Filter.hot,pageNum:0,vods:[]});
        _data.vue.channels.push({id:3,tag:"movie",name:"电影",loading:false,filter:Filter.hot,pageNum:0,vods:[]});
        _data.vue.channels.push({id:115,tag:"zs",name:"教育",loading:false,filter:Filter.hot,pageNum:0,vods:[]});
        _data.vue.channels.push({id:51,tag:"jlp",name:"纪录片",loading:false,filter:Filter.hot,pageNum:0,vods:[]});
        _data.vue.filters=[Filter.hot,Filter.new];
    },
    genSort(filter){
        if(filter===Filter.hot){
            return "c2";
        }
        if(filter===Filter.new){
            return "c1";
        }
        return "c2";
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
        let requestUrl=`https://pianku.api.mgtv.com/rider/list/pcweb/v3?platform=pcweb&channelId=${channelItem.id}&pn=${pageNum}&sort=${sort}`;
        _apiX.getJson(requestUrl,
            { "User-Agent": _apiX.userAgent(false), "tv-ref": "https://www.mgtv.com/" },
            function(text){
            console.log(text);
                channelItem.loading=false;
                if(null==text){
                    return;
                }
                let data = JSON.parse(text);
                channelItem.pageNum++;
                data.data.hitDocs.forEach(item => {
                    //console.log(item.image_url_normal)
                    let imageUrl=_tvFunc.image(item.img);
                    let remark=_data.remark(item);
                    ///${item.playPartId}.html?lastp=list_index
                    let url=`https://www.mgtv.com/b/${item.clipId}`;
                    channelItem.vods.push({id:item.clipId,name:item.title,pic:imageUrl,url:url,remark:remark,site:"mgtv"});
                });
            },function () {
                channelItem.loading=false;
            }
        )
    },
    remark(item){
        let remark=" "+item.updateInfo.replace("更新至","今");
        return remark.replace("全","");
    }
};
 $$(function(){
    _tvHtmlInit();
    document.body.style.paddingTop=0;
 });