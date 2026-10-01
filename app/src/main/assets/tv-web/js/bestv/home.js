let _data={
    vue:null,
    initData(vue){
        this.vue=vue;
        this.channels();
        this.channelPage();
    },
    channels(){
        _data.vue.channels.push({id:"xm",tag:"免费",name:"限免",loading:false,pageNum:1,vods:[]});
        _data.vue.channels.push({id:"dy",tag:"电影",name:"电影",loading:false,pageNum:1,vods:[]});
        _data.vue.channels.push({id:"dsj",tag:"电视剧",name:"电视剧",loading:false,pageNum:1,vods:[]});
        _data.vue.channels.push({id:"se",tag:"少儿",name:"少儿",loading:false,pageNum:1,vods:[]});
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
        let tagStr="&tags[]=免费";
        if(channelItem.id==='dsj'){
            tagStr="";
        }
        let requestUrl=`https://www.bestv.com.cn/api/videos/q?page=${channelItem.pageNum}&size=36&badge=1&tags[]=${channelItem.tag}`;
        _apiX.getJson(requestUrl,
            { "User-Agent": _apiX.userAgent(false), "tv-ref": "https://www.bestv.com.cn/" },
            function(text){
                console.log("text::: "+text)
                channelItem.loading=false;
                let data = JSON.parse(text);
                channelItem.pageNum++;
                data.Data.Items.forEach(item => {
                    let imageUrl=_tvFunc.image(item.Vimage);
                    let url = "https://www.bestv.com.cn/web/play/"+item.Vid;
                    if(item.badge===0){
                        channelItem.vods.push({id:item.Vid,name:item.Title,pic:imageUrl,url:url,remark:item.Pubdate,"site":"bestv"});
                    }
                });
            },function () {
                channelItem.loading=false;
            }
        )
    }
};
$(function(){
    _tvHtmlInit();
});