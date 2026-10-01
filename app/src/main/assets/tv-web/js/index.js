new FOCUS({
    event: {
        keyOkEvent: function () {
            console.log( "this.focusId length "+this.focusId+" "+$$(this.focusId).length+" "+$$(this.focusId).attr("id"));
            $$(this.focusId).trigger("click");
            //测试调出菜单
            //let json = `{"now":{"play":{"enabled":false,"play":true,"btn":"暂停"},"rate":{"id":"3","name":"正常","isCurrent":true},"hz":{"id":"0","name":"超清","isVip":false,"level":1080},"dm":{"enabled":false,"name":"弹幕开"},"xj":{"vodId":"VIDAbX80MYkBm26iUGxFbZiS240823","id":"VIDEFPawmPLPSzUQPK1EmpW4241004","url":"https://tv.cctv.com/2024/10/04/VIDEFPawmPLPSzUQPK1EmpW4241004.shtml","isVip":false,"remark":"","title":"第1集","index":0,"site":"cctv"}},"video":true,"hzs":[{"id":"0","name":"超清","isVip":false,"level":1080},{"id":"1","name":"高清","isVip":false,"level":720},{"id":"2","name":"标清","isVip":false,"level":480},{"id":"3","name":"流畅","isVip":false,"level":480}],"xjs":[{"vodId":"VIDAbX80MYkBm26iUGxFbZiS240823","id":"VIDEFPawmPLPSzUQPK1EmpW4241004","url":"https://tv.cctv.com/2024/10/04/VIDEFPawmPLPSzUQPK1EmpW4241004.shtml","isVip":false,"remark":"","title":"第1集","index":0,"site":"cctv"},{"vodId":"VIDAbX80MYkBm26iUGxFbZiS240823","id":"VIDENlVbFmFvGpcJZjM5ie9b241004","url":"https://tv.cctv.com/2024/10/04/VIDENlVbFmFvGpcJZjM5ie9b241004.shtml","isVip":false,"remark":"","title":"第2集","index":1,"site":"cctv"},{"vodId":"VIDAbX80MYkBm26iUGxFbZiS240823","id":"VIDEf0UNIWk0LDNRJKSXrKvv241004","url":"https://tv.cctv.com/2024/10/04/VIDEf0UNIWk0LDNRJKSXrKvv241004.shtml","isVip":false,"remark":"","title":"第3集","index":2,"site":"cctv"},{"vodId":"VIDAbX80MYkBm26iUGxFbZiS240823","id":"VIDEfOvUN4ByrzXCUnnCxJn4241004","url":"https://tv.cctv.com/2024/10/04/VIDEfOvUN4ByrzXCUnnCxJn4241004.shtml","isVip":false,"remark":"","title":"第4集","index":3,"site":"cctv"},{"vodId":"VIDAbX80MYkBm26iUGxFbZiS240823","id":"VIDEky737CJjkECt0QFypznF241004","url":"https://tv.cctv.com/2024/10/04/VIDEky737CJjkECt0QFypznF241004.shtml","isVip":false,"remark":"","title":"第5集","index":4,"site":"cctv"},{"vodId":"VIDAbX80MYkBm26iUGxFbZiS240823","id":"VIDEKjStsWu7GMjP0dXWlrIV241004","url":"https://tv.cctv.com/2024/10/04/VIDEKjStsWu7GMjP0dXWlrIV241004.shtml","isVip":false,"remark":"","title":"第6集","index":5,"site":"cctv"},{"vodId":"VIDAbX80MYkBm26iUGxFbZiS240823","id":"VIDErauRBBfbiFhCB5v8tKya241004","url":"https://tv.cctv.com/2024/10/04/VIDErauRBBfbiFhCB5v8tKya241004.shtml","isVip":false,"remark":"","title":"第7集","index":6,"site":"cctv"},{"vodId":"VIDAbX80MYkBm26iUGxFbZiS240823","id":"VIDEk5QNBZCrR6c3tUKQ5G0v241004","url":"https://tv.cctv.com/2024/10/04/VIDEk5QNBZCrR6c3tUKQ5G0v241004.shtml","isVip":false,"remark":"","title":"第8集","index":7,"site":"cctv"},{"vodId":"VIDAbX80MYkBm26iUGxFbZiS240823","id":"VIDE7Ax2EJBHIIv6NLkcLeQ1241004","url":"https://tv.cctv.com/2024/10/04/VIDE7Ax2EJBHIIv6NLkcLeQ1241004.shtml","isVip":false,"remark":"","title":"第9集","index":8,"site":"cctv"},{"vodId":"VIDAbX80MYkBm26iUGxFbZiS240823","id":"VIDE4tjHWxAz9hrJy1vvQIum241004","url":"https://tv.cctv.com/2024/10/04/VIDE4tjHWxAz9hrJy1vvQIum241004.shtml","isVip":false,"remark":"","title":"第10集","index":9,"site":"cctv"},{"vodId":"VIDAbX80MYkBm26iUGxFbZiS240823","id":"VIDECC5cQljnq3VJiNDVCIh2241004","url":"https://tv.cctv.com/2024/10/04/VIDECC5cQljnq3VJiNDVCIh2241004.shtml","isVip":false,"remark":"","title":"第11集","index":10,"site":"cctv"},{"vodId":"VIDAbX80MYkBm26iUGxFbZiS240823","id":"VIDErEGIBT80OQka4D4zzqyR241004","url":"https://tv.cctv.com/2024/10/04/VIDErEGIBT80OQka4D4zzqyR241004.shtml","isVip":false,"remark":"","title":"第12集","index":11,"site":"cctv"},{"vodId":"VIDAbX80MYkBm26iUGxFbZiS240823","id":"VIDEoxYPNHkw8H7tpaJbQ8Ez241004","url":"https://tv.cctv.com/2024/10/04/VIDEoxYPNHkw8H7tpaJbQ8Ez241004.shtml","isVip":false,"remark":"","title":"第13集","index":12,"site":"cctv"},{"vodId":"VIDAbX80MYkBm26iUGxFbZiS240823","id":"VIDEfNaEXnhDLGlHKnbSyIiA241004","url":"https://tv.cctv.com/2024/10/04/VIDEfNaEXnhDLGlHKnbSyIiA241004.shtml","isVip":false,"remark":"","title":"第14集","index":13,"site":"cctv"},{"vodId":"VIDAbX80MYkBm26iUGxFbZiS240823","id":"VIDEyszGt3kQcMqF0Ler3aqN241004","url":"https://tv.cctv.com/2024/10/04/VIDEyszGt3kQcMqF0Ler3aqN241004.shtml","isVip":false,"remark":"","title":"第15集","index":14,"site":"cctv"},{"vodId":"VIDAbX80MYkBm26iUGxFbZiS240823","id":"VIDEGAj71EmjkfJCE3HJrLT0241004","url":"https://tv.cctv.com/2024/10/04/VIDEGAj71EmjkfJCE3HJrLT0241004.shtml","isVip":false,"remark":"","title":"第16集","index":15,"site":"cctv"},{"vodId":"VIDAbX80MYkBm26iUGxFbZiS240823","id":"VIDEWrwLZ4ecCNheHxKkGagB241004","url":"https://tv.cctv.com/2024/10/04/VIDEWrwLZ4ecCNheHxKkGagB241004.shtml","isVip":false,"remark":"","title":"第17集","index":16,"site":"cctv"},{"vodId":"VIDAbX80MYkBm26iUGxFbZiS240823","id":"VIDEPnIaxsZfUwQrNtsGZPu1241004","url":"https://tv.cctv.com/2024/10/04/VIDEPnIaxsZfUwQrNtsGZPu1241004.shtml","isVip":false,"remark":"","title":"第18集","index":17,"site":"cctv"},{"vodId":"VIDAbX80MYkBm26iUGxFbZiS240823","id":"VIDE5yVCNlsqB66wGZeXQnXd241004","url":"https://tv.cctv.com/2024/10/04/VIDE5yVCNlsqB66wGZeXQnXd241004.shtml","isVip":false,"remark":"","title":"第19集","index":18,"site":"cctv"},{"vodId":"VIDAbX80MYkBm26iUGxFbZiS240823","id":"VIDEt9IO3Ef2XAUk9kJj5ckI241004","url":"https://tv.cctv.com/2024/10/04/VIDEt9IO3Ef2XAUk9kJj5ckI241004.shtml","isVip":false,"remark":"","title":"第20集","index":19,"site":"cctv"},{"vodId":"VIDAbX80MYkBm26iUGxFbZiS240823","id":"VIDE0teKgS3YSkLXdsMbU5e7241004","url":"https://tv.cctv.com/2024/10/04/VIDE0teKgS3YSkLXdsMbU5e7241004.shtml","isVip":false,"remark":"","title":"第21集","index":20,"site":"cctv"},{"vodId":"VIDAbX80MYkBm26iUGxFbZiS240823","id":"VIDET6ITKgxlsT90wnDHRphg241004","url":"https://tv.cctv.com/2024/10/04/VIDET6ITKgxlsT90wnDHRphg241004.shtml","isVip":false,"remark":"","title":"第22集","index":21,"site":"cctv"},{"vodId":"VIDAbX80MYkBm26iUGxFbZiS240823","id":"VIDEdSIeI5j8tboauPoHuxQ3241004","url":"https://tv.cctv.com/2024/10/04/VIDEdSIeI5j8tboauPoHuxQ3241004.shtml","isVip":false,"remark":"","title":"第23集","index":22,"site":"cctv"}],"rates":[{"id":"0","name":"2x","isCurrent":false},{"id":"1","name":"1.5x","isCurrent":false},{"id":"2","name":"1.25x","isCurrent":false},{"id":"3","name":"正常","isCurrent":true},{"id":"4","name":"0.5x","isCurrent":false}],"tab":"hz","focusId":"tv","isVip":true}`;
            //_api.message("menu",json);
            },
        focusEvent: function () {
            console.log("focusEvent")
            $$(this.focusId).trigger("focus");
        }
}});
const _appVersion={
    16:"4.1",
    17:"4.2",
    18:"4.3",
    19: "4.4",
    20:"4.4W",
    21:"5",
    22:"5.1",
    23:"6",
    24:"7",
    25:"7.1",
    26:"8",
    27:"8.1",
    28:"9",
    29:"10",
    30:"11",
    31:"12",
    32:"12L",
    33:"13",
    34:"14",
    35:"15"
}
function getAndroidName(version){
    if(version<16){
        return "小于安卓4.1"
    }
    if(version>35){
        return "大于安卓15"
    }
    return "安卓"+_appVersion[version];
}
const _html={
    init(){
        this.initApp();
    },
    initApp(){
        console.log("initAppinitApp")
        if (typeof PetiteVue === 'undefined') {
           // _api.message("activity","live");
            _api.toast("内置浏览器内核过低请返回键升级webview或者开启x5内核浏览器 也可官网utao.tv下载火狐版");
            return;
        }
        PetiteVue.createApp({
            channels:[],
            apps:[],
            focusId:"",
            qrCode:null,
            qrUrl:null,
            historys:[],
            tab: "index",
            info:{
              sys:"当前系统",
              version:"当前版本",
                x5Ok:true
            },
            initData(){
                _data.initData(this);
                this.$nextTick(()=>{
                    $$("#app-0").addClass("tv-focus");
                });
            },
            switchChannel(item){
                console.log("switchChannel"+item.tag);
                this.focusId=item.tag;
                this.tab=item.tag;
                if(item.tag==="history"){
                    this.historyFocus(item);
                }
                if(item.tag==="set"){
                    this.setFocus(item)
                }
                if(item.tag==="search"){
                    this.searchFocus(item)
                }
                let menuId="#tv-"+item.tag;
                let hasFocus= $$(menuId).hasClass("tv-focus");
                this.$nextTick(function (){
                    if(hasFocus){
                        $$(menuId).addClass("tv-focus");
                    }
                })
            },
            updateApk(){
               _apiX.msg("updateApk",null);
            },
            clearCache(){
                _apiX.msg("clearCache",null);
            },
            openX5(){
                _apiX.msg("openX5",null);
            },
            openOkMenu(){
                this.info.openOkMenu=!this.info.openOkMenu;
                _apiX.msgStr("openOkMenu",this.info.openOkMenu?"1":"0");

            },
            closeApp(){
                _apiX.msg("closeApp",null);
            },
            historyChoose(item){
                window.location.href=item.url;
            },
            searchFocus(item){
                let _this=this;
                _apiX.queryByService("queryIp",null,function (data){
                    console.log("queryIp"+data);
                    const text = "http://"+data+":10240/search.html";  // 要编码的内容
                   // const imageElement = document.getElementById('qrcodeImage');  // 获取 img 元素
                    _this.qrUrl=text;
                    QRCode.toDataURL(text, function(error, url) {
                        if (error) {
                            console.error(error);
                        } else {
                            _this.qrCode=url;
                            console.log("QR Code image successfully generated!");
                        }
                    });
                });
            },
            setFocus(item){
                let _this=this;
              //获取系统信息和 版本信息
                _apiX.queryByService("querySysInfo",null,function (data){
                    console.log("querySysInfo"+data);
                    let sysInfo=JSON.parse(data);
                    _this.info=sysInfo;
                   // _this.info.cacheSize=sysInfo.cacheSize;
                    let  version="当前版本:"+sysInfo.versionName;
                    if(sysInfo.haveNew){
                        version=version+" 发现新版 点击更新";
                    }
                    _this.info.version=version;
                    let sysDesc="当前系统:";
                    let androidVer=getAndroidName(sysInfo.versionCode);
                    sysDesc=sysDesc+androidVer;
                    if(sysInfo.sys64){
                        sysDesc+=" 64位"
                    }else{
                        sysDesc+=" 32位"
                    }
                    _this.info.sys=sysDesc;

                });
            },
            historyFocus(item){
                let _this=this;
                _apiX.queryByService("queryHistory",null,function (data){
                    console.log("history.all"+data);
                    let dataArr= JSON.parse(data);
                    let newArr=[];
                    dataArr.forEach(item=>{
                        if(item.site&&item.site.trim()!==""&&item.site.trim()!=="tv"){
                            newArr.push(item);
                        }
                    })
                    _this.historys=newArr;
                });
            },
            appChoose(item){
                console.log(`appChoose item ${item.name} ${item.url}`)
                let dataUrl = item.url;
                if(_utao_version&&(_utao_version==="{version}"||_utao_version<=20)){
                    _layer.wait("请耐心等待跳转。。。");
                    window.location.href = dataUrl;
                    return;
                }
                if(dataUrl==="tv.html"&& _tvFunc.isApp()){
                        _apiX.msgStr("activity","live");
                        return;
                }
                if(dataUrl.startsWith("https://www.douyin.com")&& _tvFunc.isApp()){
                    _apiX.msgStr("activity","douyin");
                    return;
                }
                _layer.wait("请耐心等待跳转。。。");
                window.location.href = dataUrl;
            },
            moveDown(id){
                return "#tvd-"+id+":.tv-item";
            },
            tvId(value,pre){
                if(pre){
                    return pre+value;
                }
                return 'tv-'+value;
            },
            goto(item){
                let isApp= _tvFunc.isApp();
                if(!isApp){
                    window.location.href = item.url;
                    return;
                }
                item.vodId=item.id;item.id=null;
                console.log("currentXjXX "+item.vodId+"  "+item.site);
                _apiX.msg("history.save",item);
            }
        }).mount('#tv-body');
    }
};
let _data={
    vue:null,
    initData(vue){
        this.vue=vue;
        this.channels();
        this.apps();
    },
    channels(){
        _data.vue.channels.push({id:0,tag:"see",name:"须看",vods:[]});
        _data.vue.channels.push({id:1,tag:"index",name:"&#xe604; 首页",vods:[]});
        _data.vue.channels.push({id:2,tag:"history",name:"&#xe604; 历史",vods:[]});
        _data.vue.channels.push({id:3,tag:"set",name:"设置",vods:[]});
    },
    apps(){
        let apps=[];
        let isGecko=_tvFunc.isGecko();
        console.log("isGecko",isGecko);
        apps.push({id:0,url:"tv.html",name:"电视频道",pic:"img/utao.jpg"});
        apps.push({id:0,url:"https://www.yangshipin.cn/tv/home?pid=600002475",name:"CCTV直播",pic:"img/cctv.jpg"});
        apps.push({id:0,url:"cctv.html",name:"央视片库",pic:"img/cctv-video.jpg"});
        apps.push({id:0,url:"https://www.vonchange.com",name:"土拨鼠大屏助手",pic:"img/tubo.png"});
        apps.push({id:0,url:"bestv.html",name:"百视通",pic:"img/bestv.png"});
        apps.push({id:0,url:"mgtv.html",name:"芒果TV",pic:"img/manguo.jpeg"});
        apps.push({id:0,url:"youku.html",name:"优酷",pic:"img/youku.jpg"});
        apps.forEach((item,index)=>{
            item.id=index;
            item.pic=_tvFunc.image(item.pic);
            _data.vue.apps.push(item);
        })
    }
}
$$(function() {
    _html.init();
    if(LA){
        LA.init({id:"3Kwwp7VWLvVgtOND",ck:"3Kwwp7VWLvVgtOND"});
    }
});

