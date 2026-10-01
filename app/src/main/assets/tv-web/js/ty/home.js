let _data={
    vue:null,
    initData(vue){
        this.vue=vue;
        this.channels();
        this.channelPage();
    },
    channels(){
        _data.vue.channels.push({id:"zb",tag:"1",name:"直播",loading:false,pageNum:1,vods:[]});
        _data.vue.channels.push({id:"ls",tag:"2",name:"历史",loading:false,pageNum:1,vods:[]});
        _data.vue.channels.push({id:"wl",tag:"0",name:"未来",loading:false,pageNum:1,vods:[]});
        _data.vue.filters=[{name:"咪咕体育",id:"mg"}];
    },
    mgs:[],
    queryMgs(channelItem,callback){
        if (channelItem.loading) {
            console.warn("已有数据真正加载中");
            return;
        }
        channelItem.loading=true;
        let requestUrl=`https://vms-sc.miguvideo.com/vms-match/v6/staticcache/basic/match-list/normal-match-list/0/all/default/1/miguvideo`;
        _apiX.getJson(requestUrl,
            { "User-Agent": _apiX.userAgent(false), "tv-ref": "https://www.miguvideo.com/" },
            function(text){
                console.log("text::: "+text)
                channelItem.loading=false;
                let data = JSON.parse(text);
                channelItem.pageNum++;

                $$.each(data.body.matchList,function (k,v){
                    $$.each(v,function (index,item){
                        let title = item.keyword + "    "+item.title;
                        console.log("matchList",item);
                        if(item.confrontTeams){
                            let a1=item.confrontTeams[0];
                            let a2=item.confrontTeams[1];
                            title+="    "+a1.name
                            if(a1.score>=0){
                                    title+=" "+a1.score;
                            }
                            if(a2.score>=0){
                                title+=":"+a2.score
                            }
                            title+=" "+a2.name;

                        }
                        let url= `https://www.miguvideo.com/p/live/${item.mgdbId}`;
                        _data.mgs.push({id:item.mgdbId,name:title,pic:null,url:url,remark:null,matchStatus:item.matchStatus});
                    });
                });
                callback(_data.mgs);
                /*  data.body.matchList.forEach(item => {
                      let imageUrl=_tvFunc.image(item.Vimage);
                      let url = "https://www.bestv.com.cn/web/play/"+item.Vid;
                      if(item.badge===0){
                          channelItem.vods.push({id:item.Vid,name:item.Title,pic:imageUrl,url:url,remark:item.Pubdate,"site":"bestv"});
                      }
                  });*/
            },function () {
                channelItem.loading=false;
            }
        )
    },
    filterMg(channelItem,mgs){
        mgs.forEach(item=>{
            console.log("item",item)
            if(channelItem.tag===item.matchStatus){
                channelItem.vods.push(item);
            }
        })
    },
    channelPage(channelItem){
        if(!channelItem){
            channelItem=this.vue.channels[0];
        }
        if(channelItem.pageNum>1){
            return;
        }
        if(this.mgs.length>0){
            this.filterMg(channelItem,this.mgs);
        }else{
            this.queryMgs(channelItem,function (mgs){
                _data.filterMg(channelItem,mgs);
            });
        }


    }
};
$(function(){
    _tvHtmlInit();
});