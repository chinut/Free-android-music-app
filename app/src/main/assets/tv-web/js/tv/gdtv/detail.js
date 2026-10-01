
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
(function(){
    _tvFunc.videoReady(function (video){
        let param=_tvFunc.getQueryParams();
        let type=0;
        if(param["utaot"]){
            type=param["utaot"];
        }
        if(type==0){
            setupVideo(video);
        }
        if(type==1){
            let id="video";
            if(param["utaoId"]){
                id="#"+param["utaoId"];
            }
            _tvFunc.fullscreen(id);
            $$("video").css("position","fixed !important");
        }
        video.muted = false;
        video.volume = 1;
        video.playsInline = false;
        video.setAttribute('playsinline', 'false');
        try {
            video.play();
        } catch (e) {
        }

    });
    const viewportMeta = document.querySelector('meta[name="viewport"]');
    console.log("viewportMeta::",viewportMeta);
    if (viewportMeta) {
        viewportMeta.setAttribute('content', `width=device-width, initial-scale=1`);
    }
})();