let _data={
    vue:null,
    initData(vue){
       this.vue=vue;
       this.vue.desc="南瓜电影 - 专为影视发烧友打造的精品电影平台";
       this.channels();
       this.channelPage();
    },
    channels(){
        // 南瓜电影的分类
        _data.vue.channels.push({id:"movie",tag:"movie",name:"电影",loading:false,filter:Filter.default,pageNum:0,vods:[]});
        _data.vue.channels.push({id:"tv",tag:"tv",name:"剧集",loading:false,filter:Filter.default,pageNum:0,vods:[]});
        _data.vue.channels.push({id:"documentary",tag:"documentary",name:"纪录片",loading:false,filter:Filter.default,pageNum:0,vods:[]});
        _data.vue.channels.push({id:"animation",tag:"animation",name:"动画",loading:false,filter:Filter.default,pageNum:0,vods:[]});
        _data.vue.channels.push({id:"short",tag:"short",name:"短片",loading:false,filter:Filter.default,pageNum:0,vods:[]});
        _data.vue.filters=[Filter.default,Filter.hot,Filter.new,Filter.best];
    },
    genSort(filter){
        // 南瓜电影的排序参数
        if(filter===Filter.default){
            return "default"; // 默认排序
        }
        if(filter===Filter.hot){
            return "hot"; // 热门
        }
        if(filter===Filter.new){
            return "newest"; // 最新
        }
        if(filter===Filter.best){
            return "rating"; // 评分
        }
        return "default";
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
        
        // 南瓜电影的API请求URL (基于观察到的网站结构构建)
        let requestUrl=`https://v.vcinema.cn/api/video/list?category=${channelItem.id}&page=${pageNum}&limit=20&sort=${sort}`;
        console.log("南瓜电影请求URL:", requestUrl);
        
        _apiX.getJson(requestUrl,
            { 
                "User-Agent": _apiX.userAgent(false), 
                "tv-ref": "https://v.vcinema.cn/",
                "Referer": "https://v.vcinema.cn/filtrate"
            },
            function(text){
                console.log("南瓜电影响应数据:", text);
                channelItem.loading=false;
                if(null==text){
                    return;
                }
                let data = JSON.parse(text);
                if(data.code === 0 && data.data && data.data.list){
                    channelItem.pageNum++;
                    data.data.list.forEach(item => {
                        let imageUrl = _tvFunc.image(item.poster || item.cover || item.image);
                        let remark = _data.remark(item);
                        let url = `https://v.vcinema.cn/video/${item.id}`;
                        
                        channelItem.vods.push({
                            id: item.id,
                            name: item.title || item.name,
                            pic: imageUrl,
                            url: url,
                            remark: remark,
                            site: "ngys"
                        });
                    });
                } else {
                    console.warn("南瓜电影数据格式异常:", data);
                }
            },
            function () {
                channelItem.loading=false;
                console.error("南瓜电影请求失败");
            }
        );
    },
    remark(item){
        let remark = "";
        if(item.year){
            remark += item.year + " ";
        }
        if(item.duration){
            remark += item.duration + " ";
        }
        if(item.rating){
            remark += "★" + item.rating;
        }
        if(item.episode_count && item.episode_count > 1){
            remark += " 共" + item.episode_count + "集";
        }
        if(item.status){
            remark += " " + item.status;
        }
        return remark.trim();
    }
};

$(function(){
    _tvHtmlInit();
    // 移除可能的弹窗或广告
    _tvFunc.check(function(){return $(".modal-mask").length>0;},function(){
        $(".modal-mask").remove();
    });
    _tvFunc.check(function(){return $(".login-modal").length>0;},function(){
        $(".login-modal").remove();
    });
});