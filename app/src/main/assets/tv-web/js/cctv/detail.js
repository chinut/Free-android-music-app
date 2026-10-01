const _ctrlx={
    ok(){
        //$$(".vjs-play-control").click();
        _menuCtrl.menu();
    }
};
(function(){
   let _app={
     init(){
           this.checkHistory();
           _tvFunc.check(function(){return  $$(".videoFull").length>0},function(index){
                 //全屏
               let menuId = _detailInit(null,999990,true);
           },1000);
        },
        historyUrlKey:"_tv_channel_url",
        checkHistory(){
            let nowValue= window.location.href;
            let key = this.historyUrlKey;
            let value = localStorage.getItem(key);
            if(value&&value!==nowValue){
                window.location.href=value;
                return;
            }
            localStorage.setItem(key,nowValue);
        }
    };
    _app.init();
  /*  _tvFunc.check(function (){return document.getElementsByTagName("video").length>0;},function (){
        document.getElementsByTagName("video")[0].classList.add("utv-video-full");
    });*/
})();

let _data={
    vue:null,
    initData(vue){
        this.vue=vue;
        this.vue.video=false;
        this.vue.tab="xj";
        this.fullscreen();
        this.tvListData();
        this.hzList();
    },
    fullscreen(){
        $$(".videoFull").trigger("click");
        $$(".y-full").hide();
   /*     $$(".video-con").click(function(){
            console.log("video click")
            _menuCtrl.menu();
        })*/
        $$("body").on("click", function(){
            console.log("video click")
            _menuCtrl.menu();
        });
        //音量100
        _tvFunc.volume100();
    },
    hzList(){
        _tvFunc.check(function(){return $$(".bei-list-inner").length>0},function(index){
            _data.hzListDo();
            $$("#_tv_begin").remove();
        },300);
    },
    hzListDo(){
        _data.vue.hzs.splice(0);
        $$(".bei-list-inner").find(".item").each(function(i,item){
            let id=""+i;
            $$(item).attr("id","xhz-"+id);
            let isCurrent=$$(item).hasClass("active");
            let hzName= $$(item).text().trim().replace(/\s*/g,"");
            if(hzName.includes("VIP")){
                return;
            }
            let itemData={id:id,name:hzName,isVip:false,level:_tvFunc.hzLevel(hzName,2)};
            if(isCurrent){
                _data.vue.now.hz=itemData;
            }
            _data.vue.hzs.push(itemData);
            //_data.vue.data.hz.splice(index,1,itemData);
        });
    },
    historyUrlKey:"_tv_channel_url",
    xjExt(item){
        //需要重新加载画质等
        setTimeout(function(){
            console.log("_app.historyUrlKey"+_data.historyUrlKey)
            localStorage.setItem(_data.historyUrlKey,window.location.href);
            _data.hzList();
        },2000);
    },
    xjIndex:0,
    tvListData(){
        $$(".tv-main-con-r-list-left-imga").each(function(i,item){
            _data.workChannelItem("a-",i,item);
        });
        $$(".tv-main-con-r-list-left-imgb").each(function(i,item){
            _data.workChannelItem("b-",i,item);
        });
    },
    workChannelItem(pre,i,item){
        let text= $$(item).text().trim().replace(/\s*/g,"");
        if(text.includes("VIP")){
            return;
        }
        if(text.includes("CGTN")){
            return;
        }
        let id = this.xjIndex+"";
        $$(item).attr("id","xxj-"+id);
        let isCurrent=$$(item).hasClass("tvSelect");
        text=this.doChannelName(text);
        //={vodId:next_id,id:item.id,url:item.url,isVip:false,remark:"",title:title,index:index,site:"cctv"};
        let itemData={vodId:null,id:id,url:"",title:text,isVip:false,remark:"",index:this.xjIndex};
        this.xjIndex++;
        if(isCurrent){
            _data.vue.now.xj=itemData;
        }
        //{"vodId":item.cid,"id":item.vid,url:_app.url(item.cid,item.vid),"isVip":isVip,"remark":remark,"title":title}
        _data.vue.xjs.push(itemData);
    },
    doChannelName(name){
        if(name==="CCTV16-HD"){
            return "CCTV16";
        }
        if(name==="福建东南卫视"){
            return "福建卫视";
        }
        name= name.replace("(限免)","")
            .replace("频道","");
        return name;
    }
};