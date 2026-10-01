let _data={
    vue:null,
    initData(vue){
       this.vue=vue;
       this.channels();
       this.channelPage();
    },
    channels(){
        _data.vue.channels.push({id:"tv",tag:"tv",name:"电视剧",loading:false,pageNum:0,vods:[]});
        _data.vue.channels.push({id:"dhp",tag:"dhp",name:"动画片",loading:false,pageNum:0,vods:[]});
        _data.vue.channels.push({id:"jlp",tag:"jlp",name:"纪录片",loading:false,pageNum:0,vods:[]});
        _data.vue.channels.push({id:"tbjm",tag:"tbjm",name:"特别节目",loading:false,pageNum:0,vods:[]});
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
        let requestUrl="https://api.cntv.cn/list/getVideoAlbumList?serviceId=tvcctv&n=24"+
        _tvFunc.paramStr({p:channelItem.pageNum+1,fc:channelItem.name});
        _apiX.getJson(requestUrl,
            { "User-Agent": _apiX.userAgent(false), "tv-ref": "https://tv.cctv.com/" },
            function(text){
                console.log("text::: "+text)
                channelItem.loading=false;
                let data = JSON.parse(text);
                channelItem.pageNum++;
                if(channelItem.pageNum===1&&channelItem.tag==="tv"){
                    //新增热门
                    _data.hotList(channelItem.vods);
                }
                data.data.list.forEach(item => {
                    let imageUrl=_tvFunc.image(item.image);
                    channelItem.vods.push({id:item.id,name:item.title,pic:imageUrl,url:item.url,remark:item.sc,"site":"cctv"});
                });
            },function () {
                channelItem.loading=false;
            }
        )
    },
    hotList(vods){
        console.log("hotList");
        vods.push({id:"VIDA1389081608990301",name:"父母爱情",pic:"https://p2.img.cctvpic.com/photoAlbum/page/performance/img/2024/4/1/1711942736387_70.jpg?tvImg=1",
            url:"https://tv.cctv.com/2024/01/04/VIDEmMKK2OVtm2pMSQiAEhCs240104.shtml?spm=C55853485115.P6UrzpiudtDc.0.0",remark:"全44集","site":"cctv"});
        vods.push({id:"VIDA1354531513618469",name:"武林外传",pic:"https://p4.img.cctvpic.com/photoAlbum/vms/standard/img/2023/9/21/VSETMEtC9nNzXx7iy6JJYCwa230921.jpg?tvImg=1",
            url:"https://tv.cctv.com/2014/07/27/VIDE1406399770522952.shtml?spm=C55853485115.PN6hjciJxJ1y.0.0",remark:"全80集","site":"cctv"});
        vods.push({id:"VIDA1354512311612143",name:"亮剑",pic:"https://p5.img.cctvpic.com/photoAlbum/vms/standard/img/2020/5/20/VSETYigTldWkcHwocINdAFuf200520.jpg?tvImg=1",
            url:"https://tv.cctv.com/2015/08/27/VIDE1440638136134698.shtml",remark:"全26集","site":"cctv"});
        vods.push({id:"VIDAgebq1gBc7EB3jtZwdFo7201130",name:"大秦赋",pic:"https://p4.img.cctvpic.com/photoAlbum/page/performance/img/2024/5/28/1716874699072_401.jpg?tvImg=1",
            url:"https://tv.cctv.com/2024/10/18/VIDEKWXuGU1GXpdIut6CmEtH241018.shtml?spm=C55853485115.PhPcBCVf4H0G.0.0",remark:"全78集","site":"cctv"});
        vods.push({id:"VIDAJNNHiuXByYkLTOs0zo2k210202",name:"觉醒年代",pic:"https://p1.img.cctvpic.com/fmspic/vms/image/1612151682835.jpg?tvImg=1",
            url:"https://tv.cctv.com/2023/07/10/VIDEamWUkj0LyiDSzquOL66q230710.shtml?spm=C28340.Pu9TN9YUsfNZ.S93183.454",remark:"全43集","site":"cctv"});
        vods.push({id:"VIDA1380440349816906",name:"打狗棍",pic:"https://p2.img.cctvpic.com/fmspic/vms/image/2013/09/29/VSET_1380439539940665.jpg?tvImg=1",
            url:"https://tv.cctv.com/2022/10/26/VIDETeLtJa1OePcjGPUI2K3b221026.shtml?spm=C55853485115.Pbqb0ldQ5nlz.0.0",remark:"全70集","site":"cctv"});

    }
};
 $(function(){
    _tvHtmlInit();
 });