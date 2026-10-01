(function(){
    $$(function (){
        _tvHtmlInit();
    });
})();
let _data={
    vue:null,
    initData(vue){
       this.vue=vue;
       //_layer.notifyLess("该平台在安卓端需要极高性能才可使用 如果电视老旧不推荐使用");
       this.vue.desc="该平台在安卓端需要较高性能才可使用(安卓手机千元机就可以 但电视设备就不一定) 如果电视老旧不推荐使用"
       this.channels();
       this.channelPage();
    },
    channels(){
       const channelListjson = '[{"_id":"27","channelId":100101,"channelName":"首页","channelUIMode":"1","channelTag":"index","externalLink":"","_sort":400,"_ctime":"2021-12-09T08:36:40.536Z","_mtime":"2023-08-18T02:53:33.008Z","id":27},{"_id":"12","channelId":100113,"channelName":"电视剧","channelUIMode":"1","channelTag":"tv","externalLink":"","_ctime":"2020-04-04T16:12:45.218Z","_mtime":"2021-12-09T09:06:32.115Z","id":12,"_sort":500},{"_id":"13","channelId":100137,"channelName":"VIP","channelUIMode":"1","channelTag":"vip","externalLink":"https://m.film.qq.com/x/vip-home/","_ctime":"2020-04-04T16:12:45.440Z","_mtime":"2021-12-09T09:06:32.118Z","id":13,"_sort":600},{"_id":"30","channelId":100176,"channelName":"NBA","channelUIMode":"1","channelTag":"nba","externalLink":"","_sort":700,"_ctime":"2022-01-06T06:32:58.932Z","_mtime":"2022-01-06T06:33:11.856Z","id":30},{"_id":"14","channelId":100173,"channelName":"电影","channelUIMode":"1","channelTag":"movie","externalLink":"","_ctime":"2020-04-04T16:12:45.747Z","_mtime":"2022-01-06T06:33:11.853Z","id":14,"_sort":900},{"_id":"15","channelId":100109,"channelName":"综艺","channelUIMode":"1","channelTag":"variety","externalLink":"","_ctime":"2020-04-04T16:12:46.047Z","_mtime":"2022-01-06T06:33:11.855Z","id":15,"_sort":1000},{"_id":"16","channelId":100119,"channelName":"动漫","channelUIMode":"1","channelTag":"cartoon","externalLink":"","_ctime":"2020-04-04T16:12:46.350Z","_mtime":"2022-01-06T06:33:11.865Z","id":16,"_sort":1100},{"_id":"17","channelId":100150,"channelName":"少儿","channelUIMode":"1","channelTag":"children","externalLink":"","_ctime":"2020-04-04T16:12:46.558Z","_mtime":"2022-01-06T06:33:11.857Z","id":17,"_sort":1300},{"_id":"31","channelId":110617,"channelName":"知识","channelUIMode":"1","channelTag":"knowledge","externalLink":"","_sort":1400,"_ctime":"2022-07-01T08:10:50.253Z","_mtime":"2022-07-01T08:15:39.721Z","id":31},{"_id":"32","channelId":100103,"channelName":"体育","channelUIMode":"1","channelTag":"sport","externalLink":"","_sort":1600,"_ctime":"2022-08-29T03:38:05.917Z","_mtime":"2022-08-29T03:38:05.917Z","id":32},{"_id":"34","channelId":110847,"channelName":"欧冠","channelUIMode":"1","channelTag":"ucl","externalLink":"","_sort":1800,"_ctime":"2022-09-05T02:26:24.242Z","_mtime":"2022-09-05T02:26:24.242Z","id":34},{"_id":"35","channelId":110846,"channelName":"德甲","channelUIMode":"1","channelTag":"bundesliga","externalLink":"","_sort":1900,"_ctime":"2022-09-05T02:26:24.529Z","_mtime":"2022-09-05T02:26:24.529Z","id":35}]';
       let channels = JSON.parse(channelListjson);
       let channelMap=new Map();
       channels.forEach((item,index)=>{
          channelMap.set(item.channelTag, item);
       });
       let channelStr = ["tv", "variety", "movie","cartoon","children"];
       let pageContext=null;
       //{"page_index":"1","data_src_647bd63b21ef4b64b50fe65201d89c6e_page":"1","view_ad_ssp_netmovie_flush_num":"1","view_ad_ssp_ad_count_send":"0","view_ad_ssp_minigame_flush_num":"1","sdk_page_ctx":"{\"page_offset\":1,\"page_size\":2,\"used_module_num\":2}","view_ad_ssp_minigame_cards_consumed":"0","view_ad_ssp_cards_consumed":"0","view_ad_ssp_ctx_version":"1","view_ad_ssp_remaining":"0","view_ad_ssp_netmovie_ad_count_send":"0","data_src_647bd63b21ef4b64b50fe65201d89c6e_data_version":"","view_ad_ssp_netmovie_remaining":"0","view_ad_ssp_minigame_ctx_version":"1","view_ad_ssp_netmovie_cards_consumed":"0","view_ad_ssp_flush_num":"1","view_ad_ssp_minigame_ad_count_send":"0","view_ad_ssp_netmovie_ctx_version":"1","view_ad_ssp_minigame_remaining":"0"};
       channelStr.forEach((key,index)=>{
          let item = channelMap.get(key);
          _data.vue.channels.push({id:item.channelId,tag:item.channelTag,name:item.channelName,filter:Filter.hot,pageContext:pageContext,loading:false,pageNum:0,vods:[]});
       });
        _data.vue.filters=[Filter.hot,Filter.new,Filter.best];
    },
    genSort(filter){
        if(filter===Filter.hot){
            return "75";
        }
        if(filter===Filter.new){
            return "79";
        }
        if(filter===Filter.best){
            return "85";
        }
        return "75";
    },
    channelPage(channelItem) {
        let channelId="100101";
        if(!channelItem){
            channelItem=this.vue.channels[0];
        }
        channelId=channelItem.id.toString();
       
        if (channelItem.loading) {
            console.warn("已有数据真正加载中");
            return;
        }
        let sort= this.genSort(channelItem.filter);
        channelItem.loading=true;
        console.log(`channelId ${channelId} pageNum ${channelItem.pageNum} loading: ${channelItem.loading}`);
        //{"channel_id":channelId,"filter_params":"sort=75","page_type":"channel_operation","page_id":"channel_list_second_page"}
        _apiX.postJson("https://pbaccess.video.qq.com/trpc.universal_backend_service.page_server_rpc.PageServer/GetPageData?video_appid=1000005&vplatform=2&vversion_name=8.9.10&new_mark_label_enabled=1",
            { "User-Agent": _apiX.userAgent(true), "tv-ref": "https://m.v.qq.com/" },
            {
                "page_params": {"channel_id":channelId,"filter_params":`sort=${sort}`,"page_type":"channel_operation","page_id":"channel_list_second_page"},
                "page_context": channelItem.pageContext
            },
            function (text) {
                channelItem.loading=false;
                let data = JSON.parse(text);
                if(data.ret!='0'){
                    return;
                }
                channelItem.pageNum++;
                channelItem.pageContext=data.data.next_page_context;
                $.each(data.data.module_list_datas, function (index, pItem) {
                    $.each(pItem.module_datas[0].item_data_lists.item_datas, function (i, itemOrg) {
                        let item = itemOrg.item_params;
                        if (itemOrg.item_type == "10"||!item.cid) {
                            return;
                        }
                        let imageUrl=_tvFunc.image(item.new_pic_vt);
                        let url=`https://v.qq.com/x/cover/${item.cid}.html`;
                        channelItem.vods.push({id:item.cid,name:item.title,pic:imageUrl,url:url,remark:_data.remark(item),"site":"qq"});
                        //vodId;vodName;vodPic;vodRemarks; new VodData(item.cid, item.title, item.image_url, item.episode_updated));
                    });
                });
            }, function () {
                channelItem.loading=false;
            }
        );
    },
    remark(item){
        let  remark="";
        if(item.timelong){
            let timelong=item.timelong.replace("更新至","今");
            timelong= timelong.replace("全","");
            remark=`(${timelong})`;
        }
         return remark;
    }

};