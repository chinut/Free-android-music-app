_data={
    hzList(){
        let current= $$("#player_resolution_show_player").attr("activeresolution");
        let hzList=[];
        let currentLevelHz=null;
        $$("#player_resolution_bar_player").find("[itemvalue]").each(function(i,item){
            let id=$$(item).attr("id");
            // $$(item).attr("id","xhz-"+id);
            let hzLevel=$$(item).attr("itemvalue");
            let hzName= $$(item).text().trim().replace(/\s*/g,"");
            let itemData=
                {id:id,name:hzName,level:_tvFunc.hzLevel(hzName,2),
                    action:`_data.hzChoose("${id}","${hzName}")`};
            if(hzLevel===current){
                currentLevelHz=itemData;
            }
            hzList.push(itemData);
        });
       let chooseHzId= localStorage.getItem("chooseHz");
       if(chooseHzId&&currentLevelHz.id!==chooseHzId){
           _data.hzChoose(chooseHzId,localStorage.getItem("chooseHzName"));
       }
       /* if(currentLevelHz.id!==hzList[0].id&&hzList[0].level>=720){
            console.log(hzList[0]);
            _layer.notifyLess("切换到 "+hzList[0].name);
            $$("#"+hzList[0].id).click();
        }*/
        _apiX.msg("videoQuality",hzList);
        return hzList;
    },
    hzChoose(id,name){
        $$("#"+id).click();
        //_layer.notifyLess("画质切换到 "+name);
        _apiX.toast("画质切换到 "+name);
        localStorage.setItem("chooseHz",id);
        localStorage.setItem("chooseHzName",name);
    }
};

(function(){
    _tvFunc.fixedW("body");
    //_detailInit(null,999990,true);
    _tvFunc.check(function (){return null!=document.getElementById("player");},function (){
        //$$("#control_bar_player").hide();
      /*  setInterval(function(){
            if($$("#control_bar_player").css("display")!=='none'){
                $$("#control_bar_player").css("display","none");
                console.log("control_bar_player none");
            }
        },2000);*/
        _tvFunc.fullscreenW("#player");
        _data.hzList();
        //_tvFunc.addKeyFullScreen(document.getElementById("player"));
       // _apiX.msgStr("key","F");
        //document.getElementById("player").classList.add("utv-video-full");
        $$(".floatNav").hide();
        $$(".nav_wrapper_bg").hide();
        $$(".header_nav").hide();
        $$(".playingVideo").css("width","100%");

    });
    _tvFunc.volume100();
    /*let _app={
     init(){
           _tvFunc.check(function(){return  $$("#player_pagefullscreen_msg_player").length>0},function(index){
               _data.hzList();
           },1000);
        }
    };
    _app.init();*/

})();

