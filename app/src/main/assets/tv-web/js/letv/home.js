let _data={
    vue:null,
    initData(vue){
        this.vue=vue;
        this.channels();
        this.channelPage();
    },
    channels(){
        _data.vue.channels.push({id:"dsj",tag:"2",name:"电视剧",loading:false,filter:Filter.hot,pageNum:1,vods:[]});
        _data.vue.channels.push({id:"dy",tag:"1",name:"电影",loading:false,filter:Filter.hot,pageNum:1,vods:[]});
        _data.vue.channels.push({id:"dm",tag:"5",name:"动漫",loading:false,filter:Filter.hot,pageNum:1,vods:[]});
        _data.vue.filters=[Filter.hot,Filter.new];
    },
    genSort(filter){
        if(filter===Filter.hot){
            return "4";
        }
        if(filter===Filter.new){
            return "1";
        }
        return "4";
    },
    channelPage(channelItem){
        if(!channelItem){
            channelItem=this.vue.channels[0];
        }
        if (channelItem.loading) {
            console.warn("已有数据真正加载中");
            return;
        }
        let sort= this.genSort(channelItem.filter);
        channelItem.loading=true;
        let requestUrl=`https://list.le.com/getLesoData?from=pc&src=1&stype=1&ps=30&pn=${channelItem.pageNum}&ph=420001&dt=1&cg=${channelItem.tag}&or=${sort}&stt=1&vt=180001&payPlatform=-141001&s=1`;
        _apiX.getJson(requestUrl,
            { "User-Agent": _apiX.userAgent(false), "tv-ref": "https://www.le.com/" },
            function(text){
                console.log("text::: "+text)
                channelItem.loading=false;
                let data = JSON.parse(text);
                channelItem.pageNum++;
                data.data.arr.forEach(item => {
                    let imageUrl=_tvFunc.image(item.imgUrl);
                    let url = _tvFunc.link(item.urlLink);
                    channelItem.vods.push({id:item.aid,name:item.name,pic:imageUrl,url:url,remark:item.updataInfo,"site":"letv"});
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