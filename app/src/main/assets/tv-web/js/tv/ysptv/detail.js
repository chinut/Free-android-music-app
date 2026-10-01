_data={
    hzList(){
        let hzList=[];
        let currentLevelHz=null;
        $$(".bei-list-inner").find(".item").each(function(i,item){
            let id=""+i;
            $$(item).attr("id","xhz-"+id);
            let isCurrent=$$(item).hasClass("active");
            let hzName= $$(item).text().trim().replace(/\s*/g,"");
            if(hzName.includes("VIP")){
                return;
            }
            let itemData={id:id,name:hzName,level:_tvFunc.hzLevel(hzName,2),
                action:`_data.hzChoose("xhz-${id}","${hzName}")`};
            if(isCurrent){
                currentLevelHz=itemData;
            }
            //`$$("#xhz-${id}").click()`
            hzList.push(itemData);
            //_data.vue.data.hz.splice(index,1,itemData);
        });
        let chooseHzId= localStorage.getItem("chooseHz");
        if(chooseHzId&&"xhz-"+currentLevelHz.id!==chooseHzId){
            _data.hzChoose(chooseHzId,localStorage.getItem("chooseHzName"));
        }
        _apiX.msg("videoQuality",hzList);
    },
    hzChoose(id,name){
        $$("#"+id).click();
        //_layer.notifyLess("画质切换到 "+name);
        _apiX.toast("画质切换到 "+name);
        localStorage.setItem("chooseHz",id);
        localStorage.setItem("chooseHzName",name);
    }
};
function setupVideo(video) {
    const container = document.createElement('div');
    container.style.position = 'fixed';
    container.style.top = '0';
    container.style.left = '0';
    container.style.width = '100vw';
    container.style.height = '100vh';
    container.style.zIndex = '2147483647';
    container.style.backgroundColor = 'black';
    video.style.width = '100%';
    video.style.height = '100%';
    video.style.objectFit = 'contain';
    video.style.transform = 'translateZ(0)';
    container.appendChild(video);
    document.body.appendChild(container);
    document.body.style.overflow = 'hidden';
    document.documentElement.style.overflow = 'hidden';

}
// 这里已经是文档加载后的了
(function(){
    _tvFunc.waitForVideoElement().then(video => {
        let param=_tvFunc.getQueryParams();
        setupVideo(video);
        video.muted = false;
        video.volume = 1;
        video.playsInline = false;
        video.setAttribute('playsinline', 'false');
        try {
            video.play();
        } catch (e) {
        }
        _data.hzList(video);
        _tvFunc.check(function (){
            let videoPlay=_tvFunc.isVideoPlaying(video);
            if(!videoPlay){
                _tvFunc.getVideo().play();
            }
            return videoPlay},function (){_data.hzList(video);},1000,5);

    });
})();



