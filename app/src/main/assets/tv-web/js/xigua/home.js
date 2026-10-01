let _data={
    vue:null,
    initData(vue){
       this.vue=vue;
       this.channels();
       this.channelPage();
    },
    channels(){
        //{"pinyin":"dianshiju","filters":{"type":"电视剧","area":"全部地区","tag":"全部类型","sort":"最热","paid":"免费"},"offset":0,"limit":18}
        _data.vue.channels.push({id:"tv",tag:"dianshiju",name:"电视剧",name2:"电视剧",sort:"综合排序",filter:Filter.default,loading:false,pageNum:0,vods:[]});
        _data.vue.channels.push({id:"dy",tag:"dianying",name:"电影",name2:"电影",sort:"综合排序",filter:Filter.default,loading:false,pageNum:0,vods:[]});
        _data.vue.channels.push({id:"zy",tag:"zongyi",name:"综艺",name2:"分类",sort:"综合排序",filter:Filter.default,loading:false,pageNum:0,vods:[]});
        _data.vue.channels.push({id:"se",tag:"shaoer",name:"少儿",name2:"儿童",sort:"综合排序",filter:Filter.default,loading:false,pageNum:0,vods:[]});
        _data.vue.channels.push({id:"dm",tag:"dongman",name:"动漫",name2:"动漫",sort:"综合排序",filter:Filter.default,loading:false,pageNum:0,vods:[]});
        _data.vue.channels.push({id:"jlp",tag:"jilupian",name:"纪录片",name2:"纪录片",sort:"综合排序",filter:Filter.default,loading:false,pageNum:0,vods:[]});
        _data.vue.filters=[Filter.default,Filter.hot,Filter.new,Filter.best];
    },
    genSort(filter){
        if(filter===Filter.default){
            return "综合排序";
        }
        if(filter===Filter.hot){
            return "最热";
        }
        if(filter===Filter.new){
            return "最新";
        }
        if(filter===Filter.best){
            return "高分";
        }
        return "综合排序";
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
        let requestUrl="https://www.ixigua.com/api/cinema/filterv2/albums";
        let offset = channelItem.pageNum*18;
        let sort= this.genSort(channelItem.filter);
        _apiX.postJson(requestUrl,
            { "User-Agent": _apiX.userAgent(false), "tv-ref": "https://www.ixigua.com/","tv-ori":"https://www.ixigua.com/" },
            {"pinyin":channelItem.tag,"filters":{"type":channelItem.name2,"area":"全部地区","tag":"全部类型","sort":sort,"paid":"免费"},
            "offset":offset,"limit":18},
            function(text){
                channelItem.loading=false;
                console.log(text);
                let data = JSON.parse(text);
                channelItem.pageNum++;
                data.data.albumList.forEach(item => {
                    let imageUrl=_tvFunc.image(_data.getImage(item.coverList));
                    let remark = _data.remark(item);
                    let url="https://www.ixigua.com/"+item.albumId;
                    channelItem.vods.push({id:item.albumId,name:item.title,pic:imageUrl,url:url,remark:remark,site:"xigua"});
                });
            },function () {
                channelItem.loading=false;
            }
        )
    },
    getImage(list){
        let chooseItem=list[0];
        list.forEach(item=>{
           if(item.imageStyle===2){
             chooseItem=item;
           }
        });
        return chooseItem.thumbUrlList[0];
    },
    remark(item){
        if(!item.bottomLabel){
           return "";
        }
        let remark=" "+item.bottomLabel.replace("更新至","今");
        return remark.replace("全","");
    }
};
 $(function(){
    _tvHtmlInit();
 });