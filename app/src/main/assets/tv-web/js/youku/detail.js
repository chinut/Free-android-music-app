const  _ctrlx={
    ok(){
        $$(".kui-play-icon-0").click();
        _layer.notifyLess("OK键暂停或播放")
        //_apiX.msgStr("key","SPACE");
        setTimeout(function(){
              $$("#youku-pause-container").remove();
        },5000);
    },
    fullscreen() {
       // _tvFunc.fullscreen("#ykPlayer");
      /*  _tvFunc.check(function (){return $$("#ykPlayer").length>0},function (){
            $$("#ykPlayer").addClass("utv-video-full");
        });*/
    }
};

(function(){
    const _app={
        init(){
            _detailInit(null,9999);
        }
    };
 
    const _login={
        init(){
            //crmusercenter_avatar
            $$("[data-spm='usercenter']").find("img").trigger("click");
           $$(".crmusercenter_avatar img").click();
           //return document.getElementById("alibaba-login-box")&&sessionStorage.getItem("youkuQr");
         /*  _tvFunc.check(function(){return _tvFunc.sessionStorageCheckTime("youkuQr");},function(){
                    let qrUrl =   sessionStorage.getItem("youkuQr");
                    console.log("qrUrl "+qrUrl);
                    $$("#YT-loginFramePop").css({"z-index":999});
                    //document.getElementById("YT-loginFramePop").zIndex=999; alibaba-login-box
                    let menuId= _layer.open(_login.loginHtml(qrUrl), 99999, null, "tv-index", "tv-bg-yellow");


           });*/
        },
        loginQr(qrUrl){
           // let qrUrl =   sessionStorage.getItem("youkuQr");
            console.log("qrUrl "+qrUrl);
            $$("#YT-loginFramePop").css({"z-index":999});
            //document.getElementById("YT-loginFramePop").zIndex=999; alibaba-login-box
            let menuId= _layer.open(_login.loginHtml(qrUrl), 99999999, null, "tv-index", "tv-bg-yellow");
            TvFocus.init();
        },
        loginHtml(qrUrl) {
            //    <svg  id="tv-qrCode" height="500" viewBox="0 0 37 37" width="500" xmlns="http://www.w3.org/2000/svg">
            return `
            <div style="font-size:2vw;line-height:3vw; color: black;text-align: center;padding:1vh 1vw">该平台需要登录和最好有普通VIP(除超前点播无需电视端VIP)<br/>
            请打开手机端优酷扫一扫登录</div>
            <div class="tv-header tv-flex-around">
               <div class="tv-btn tv-focus"  id="tv-reload" onclick="window.location.reload();">刷新</div>
            </div>
            <div>
            <div  id="_tv_login" class="tv-tab tv-tab-flex" style="justify-content:center;">
                <img src="${qrUrl}" width="200" height="200"/>      
            <div>
            </div>
           `;
        }
    }
    window._loginQr=function(url){
        _login.loginQr(url);
    }
    $$(function(){
        _tvFunc.check(function(){
            console.log("usercenter "+$$(".crmusercenter_user_center_box").length);
            return $$(".crmusercenter_user_center_box").length>0;},function(){
            setTimeout(function (){
                let loginTitle = $$(".crmusercenter_user_center_box").find("img").attr("title");
                //yklogininstance.isLoginStatus;
                console.log("isLogin:: "+loginTitle)
                if(""===loginTitle){
                    console.log("login not");
                    _login.init();
                }else{
                    _app.init();
                }
            },3000);
        });
        _ctrlx.fullscreen();
    });
})();
const _data={
    vue:null,
    initData(vue,hzCallback){
       this.vue=vue;
       _tvFunc.check(function(){return $$("[com='quality']").length>0},function(){
        //网页全屏
        _data.fullscreen();
        _data.hzList(hzCallback);
        _data.rateList();
        _data.xjList();
       });
    },
    fullscreen(){
        $$("#webfullscreen-icon").click();
        //$$("#fullscreen-icon").click();
        //$$("#ykPlayer").addClass("utv-video-full");
       // _ctrlx.fullscreen();
          //音量100
        _tvFunc.volume100();
        //vip
        let isVip= $$(".usercenter_vipborder").length>0;
        console.log("isVip "+isVip)
        this.vue.isVip=isVip;
        //关闭弹幕
        console.log("XX "+$$(".turn-on_3h6RT").length+"XX "+ $$("#youkubarrage").length)
        $$(".turn-on_3h6RT").click();
        $$("#youkubarrage").remove();
        //跳过片头片尾 #youku-pause-container

    },
    xjList(){
       let nowId = __INITIAL_DATA__.videoId;
        let vodId= __INITIAL_DATA__.showId;
       if(!nowId){
           nowId=__INITIAL_DATA__.pageMap.extra.videoId;
           vodId=__INITIAL_DATA__.pageMap.extra.showId;
       }
       //请求手机端html 获取数据
       let requestUrl=`https://m.youku.com/video/id_${nowId}`;
       console.log("requestUrl::"+requestUrl);
       _apiX.getHtml(requestUrl,   { "User-Agent": _apiX.userAgent(true), "tv-ref": "https://m.youku.com/" },function(html){
            console.log("htmlOrg::"+html);
            let  dataListStrs=html.match(/__INITIAL_DATA__([\S\s]+)window.toFind/);//选集 \"选集\"
            console.log("htmlJs::"+dataListStrs);
            if(dataListStrs){
                let newStr= dataListStrs[0].replace("__INITIAL_DATA__","window._tv_data");
                eval(newStr);
                let xjItem=null;
                $$.each(_tv_data.componentList,function (i,item){
                    if(item.componentId&&item.componentId==="h5-detail-anthology"){
                        xjItem=item;
                        return false;
                    }
                });
                if(null==xjItem){
                    return;
                }
               // let itemStr=dataListStrs[0];
               // let dataListStr = itemStr.substring(18,itemStr.length-13);
              //  console.log("html::"+dataListStr);
                let list=  xjItem.dataNode;
                $$.each(list,function(index,item){
                    //console.log("item",item);
                    let title= _tvFunc.titleQ(item.data.stage,item.data.title);
                        //`第${item.data.stage}集`;
                    let id= item.data.action.value;
                    let url=`https://v.youku.com/v_show/id_${id}.html`;
                    let remark="";
                    if(item.data.videoType==="预告片"){
                        remark="预告";
                    }
                    let itemData={vodId:vodId,id:index,url:url,isVip:false,name:item.data.title,remark:remark,title:title,index:index,"site":"youku"};
                    if(nowId===id){
                        _data.vue.now.xj=itemData;
                        _tvFunc.currentXj(itemData)
                    }
                    _data.vue.xjs.push(itemData);
                });
            }
       });
    },
/*    rateList(){
      
        $$("[data-spm='speed']").each(function(index,item){
            let id=index.toString();
            $$(item).attr("id","xrate-"+id);
            let name= $$(item).text();
            let current=false;
            if($$(item).attr("style").includes('#00A9F5')){
                current=true;
            }
            let itemData={id:id,name:name,isCurrent:current};
            if(current){
                _data.vue.now.rate=itemData;
            }
            _data.vue.rates.push(itemData);
         });
    },*/
    rateList(){
        _data.vue.rates.push({id:"10X",name:"1.0",isCurrent:true,videoDo:true});
        _data.vue.rates.push({id:"10X",name:"1.25",isCurrent:true,videoDo:true});
        _data.vue.rates.push({id:"15X",name:"1.5",isCurrent:false,videoDo:true});
        _data.vue.rates.push({id:"20X",name:"2.0",isCurrent:false,videoDo:true});
        _data.vue.rates.push({id:"20X",name:"3.0",isCurrent:false,videoDo:true});
    },
    hzList(hzCallback){
            let hzNow= $$("[com='quality']").find(".control-label").text();
            let newIndex=0;
            console.log("hzName::: "+hzNow);
            $$(".kui-quality-quality-item").each(function(index,item){
               let hzName = $$(item).text();
               let isVip=false;
               console.log("hzName::: "+hzName);
               if(hzName.includes("客户端")){
                    return;
               }
               if(hzName.includes("VIP")){
                  isVip=true;
                  hzName=hzName.replace("VIP","");
               }
               let id=newIndex.toString();
               $$(item).attr("id","xhz-"+id);
               let itemData={id:id,name:hzName,isVip:isVip,level:_tvFunc.hzLevel(hzName,1)};
               if(hzName.includes(hzNow)){
                  _data.vue.now.hz=itemData;
              }
              _data.vue.hzs.push(itemData);
              newIndex++;
            });
            hzCallback();
            if($$(".kui-language-language-container").find(".control-item").length>1){
                $$(".kui-language-language-container").find(".control-item").each(function(index,item){
                    let hzName = $$(item).text();
                    let id=newIndex.toString();
                    $$(item).attr("id","xhz-"+id);
                    let itemData={id:id,name:hzName,isVip:false,level:null};
                    _data.vue.hzs.push(itemData);
                    newIndex++;
                });
            }

    }
};

