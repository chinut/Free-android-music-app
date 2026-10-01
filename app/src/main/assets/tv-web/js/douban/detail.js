//eval($("#episode-tmpl").prev().html().replace("var sources","window.sources"));

$$(function (){
    function  doUrl(url){
        let index= url.indexOf("url=");
        let link= decodeURIComponent(url.substring(index+4,url.length));
        if(link.startsWith("http://")){
            return  "https"+link.substring(4);
        }
        return link;
    }
    function chooseHtml(urls){
        let html = "";
        urls.forEach((item,index)=>{
            html+=`<div class="tv-btn tv-site" id="tv-${item.id}" data-url="${item.url}" >${item.name}</div>`;
        });
        return `
            <div class="tv-header tv-flex-around">
                <div class="tv-btn" >该剧可播放平台选择</div>
            </div>
             <div id="tv-content">
                <div id="tv-index-content" class="tv-content"">
                 ${html}
               </div>
                </div>
        `;
    }
    function siteName(url){
        if(url.includes("v.qq.com")){
            return "腾讯视频"
        }
        if(url.includes("iqiyi.com")){
            return "爱奇艺"
        }
        if(url.includes("bilibili.com")){
            return "哔哩哔哩"
        }
        if(url.includes("youku.com")){
            return "优酷"
        }
        if(url.includes("mgtv.com")){
            return "芒果TV"
        }
        return null;
    }
     let episode = document.getElementById("episode-tmpl");
     if(null==episode){
         _layer.notify("抱歉 该剧暂时无法播放 请按返回键返回")
         return;
     }
     eval($$("#episode-tmpl").prev().html().replace("var sources","window.sources"));
    console.log("sources",sources);
    let urls=[];
    let index=0;
    $$.each(sources,function(key,value){
        let link = doUrl(value[0].play_link);
        console.log("sources",key,link)
        let name = siteName(link);
        if(null==name){
            return true;
        }
        urls.push({url:link,"name":name,id:index+""});
        index++;
    })
    if(urls.length===1){
        console.log("only one",urls)
        window.location.href=urls[0].url;
        return;
    }
    _layer.open(chooseHtml(urls),99998,null,"tv-index", "tv-bg-yellow");
    TvFocus.init();
    $$("#tv-0").addClass("tv-focus");
    $$(".tv-site").on("click",function(){
        let url = $(this).attr("data-url");
        window.location.href=url;
    })
});