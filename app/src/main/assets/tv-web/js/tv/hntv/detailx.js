
(function(){
    _tvFunc.fixedW("body");
    _tvFunc.check(function (){return $$(".channels .channel").length>0},function (){
        let url = window.location.href;
         let index= url.indexOf("tag=");
         let tag =  decodeURI(url.substring(index+4,url.length));
         console.log(tag);
         let currentTag="";
         let tagIndex=0;
        $$(".channels .channel").each(function (i,item){
             let active=  $$(item).hasClass("selected");
             let text = $$(item).find(".name").text().trim();
             //console.log(text);
             if(active){
                 currentTag=text;
             }
             if(tag===text){
                 tagIndex=i;
             }
        });
        console.log(currentTag);
        if(tag!==currentTag){
            $$(".channels .channel")[tagIndex].click();
        }
        _tvFunc.fixedW("body");
        // $("#programMain .title")[1].click()
        _tvFunc.check(function (){return document.getElementsByClassName("mgtv-live-player").length>0;},function (){
            document.getElementsByClassName("mgtv-live-player")[0].classList.add("utv-video-full");

        });
    });
    _tvFunc.volume100();
})();
