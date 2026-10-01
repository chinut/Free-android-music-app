(function(){
    const _html={
        _ctrl:{
            hzChoose(item){
                setTimeout(function(){
                    //$$("#"+item.id).click();
                    if(item.type&&item.type==="id"){
                        $$("#"+item.id).click();
                        return;
                    }
                    $$("#xhz-"+item.id).click();
                },1000);
             },
             rateChoose(item){
                 if(item.videoDo){
                     console.log("videoDo");
                     _tvFunc.getVideo().playbackRate = item.name;
                     return;
                 }
                 $$("#xrate-"+item.id).click();
             },
             xjChoose(item){
                let waitId= _layer.wait("请耐心等待跳转。。。");
                 if(""!==item.url){
                     window.location.href=item.url;
                     return;
                 }
                 $$("#xxj-"+item.id).click();
                 _data.xjExt(item);
                 _layer.close(waitId);
             },
             next(){
                 let index=_data.vue.now.xj.index+1;
                 let item=_data.vue.xjs[index];
                 this.xjChoose(item);
             }
        },
        init(id,index,notReload){
            let menuId= this.initApp(id,index);
           this.initCtrl();
           if(notReload){
               return menuId;
           }
          let orgUrl=window.location.href;
          setInterval(function(){
            let nowUrl=window.location.href;
            if(nowUrl!==orgUrl){
                 console.log("UUUUUUUPP 已经发生变化！");
                 window.location.reload();
            }
           },2000);
           return menuId;
        },
        initCtrl(){
            this._ctrl.play = !_ctrlx.play ? function () { $$("#xtv-play").click();} : _ctrlx.play;
        },
        initHtml(){
            let html = `
            <div class="tv-menu" @vue:mounted="initData()">
                <div tabindex="0"  class="tv-btn tv-focus" @focus=tabChoose('xj')  id="tv-xj" :move-down="tvId(now.xj.id,'#xj-')" move-downb="#tv-xj-content:.tv-btn">{{video?"选集":"频道"}}</div>
                 <div tabindex="0" v-if="video&&xjs.length>0&&(now.xj.index+1) < xjs.length" @click="next()" @focus=tabChoose('') class="tv-btn"  id="tv-next">下一集</div>   
                 <div tabindex="0" v-if="video" @focus=tabChoose('jd') class="tv-btn" move-down="#tv-jd-content:.tv-btn"  id="tv-jd">进度</div>
                 <div tabindex="0" v-if="hzs.length>0" class="tv-btn " @focus=tabChoose('hz')  :move-down="tvId(now.hz.id,'#hz-')" move-downb="#tv-hz-content:.tv-btn" id="tv-hz">{{now.hz.name}}</div> 
                 <div tabindex="0"  v-if="rates.length>0" class="tv-btn" @focus=tabChoose('rate') :move-down="tvId(now.rate.id,'#rate-')" id="tv-rate">倍数{{now.rate.name}}</div>
                  <div tabindex="0"   @click="reload()" @focus=tabChoose('reload') class="tv-btn"  id="tv-reload">重载</div>
             </div>
          
             <div id="tv-content">
              <div id="tv-jd-content" class="tv-tab tv-tab-grid" :style="{display:(tab == 'jd' ? 'grid' : 'none')}">
                  <div v-for="item in jds"  @click="jdChoose(item)" :id="tvId(item.id,'jd-')" class="tv-btn  tab-grid-item" style="white-space:normal;" 
                  move-updown="6" move-up="#tv-jd">{{item.name}}</div>
               </div>
               <div id="tv-hz-content" class="tv-tab tv-tab-flex" :style="{display:(tab == 'hz' ? 'flex' : 'none')}">
                  <div  v-for="item in hzs" @click="hzChoose(item)" :id="tvId(item.id,'hz-')" class="tv-btn"  move-up="#tv-hz">{{item.name}} {{item.isVip?"VIP":""}}</div>
               </div>
               <div id="tv-rate-content" class="tv-tab tv-tab-flex" :style="{display:(tab == 'rate' ? 'flex' : 'none')}">
                  <div  v-for="item in rates" @click="rateChoose(item)"  :id="tvId(item.id,'rate-')" class="tv-btn"  move-up="#tv-rate">{{item.name}}</div>
               </div>
               <div id="tv-xj-content" class="tv-tab tv-tab-grid" :style="{display:(tab == 'xj' ? 'grid' : 'none')}">
                  <div v-for="item in xjs"  @click="xjChoose(item)" :id="tvId(item.id,'xj-')" class="tv-btn  tab-grid-item" style="white-space:normal;" move-updown="6" move-up="#tv-xj">{{item.title}} {{item.remark}}</div>
               </div>
             </div>`
           return html;
        },
        vue:null,
        getData(){
              let data={
                  now:this.vue.now,
                  video: this.vue.video,
                  hzs:this.vue.hzs,
                  xjs:this.vue.xjs,
                  jds:this.vue.jds,
                  rates:this.vue.rates,
                  tab:this.vue.tab,
                  focusId:this.vue.focusId,
                  isVip:this.vue.isVip
              }
              console.log("all: "+JSON.stringify(data));
              return data;
        },
        initApp(id,index){
            let  html= this.initHtml();
            let menuId = _layer.initMenu(id,html,index);  
            let _ctrl=this._ctrl;
            PetiteVue.createApp({
                now:{
                   play:{enabled:false,play:true,btn:"暂停"},
                   rate:{id:"10X",name:"1.0X",index:0},
                   hz:{},
                   dm:{enabled:false,name:"弹幕开"},
                   xj:{id:null,index:0}
                },
                video:true,
                hzs:[],
                xjs:[],
                rates:[],
                jds:[],
                tab:"xj",
                focusId:"tv",
                isVip:true,
                initData(){
                    let _this=this;
                    _html.vue=this;
                    _data.initData(this,function(){
                        //vip自动选择画质1080p
                        //非vip 选择不是vip的最高
                        let isVip=_this.isVip;
                        console.log("hzLevel",_this.now.hz.level);
                        if(_this.now.hz.level<1080){
                            console.log("need qie")
                            $$.each(_this.hzs,function(index,item){
                                console.log("isVip",isVip,item.isVip);
                                if(isVip&&item.level===1080){
                                    setTimeout(function(){
                                        _layer.notify("正在切换到"+item.name);
                                        _this.hzChoose(item);
                                    },2000);
                                    return false;
                                }
                                if(!isVip&&!item.isVip){
                                    if(item.level===_this.now.hz.level){
                                        return false;
                                    }
                                    setTimeout(function(){
                                        _layer.notify("正在切换到"+item.name);
                                        _this.hzChoose(item);
                                    },2000);
                                    return false;
                                }
                            });
                        }
                    });
                    window._isVideo=this.video;
                    //进度
                    this.getJds( (splits)=>{
                        this.jds=splits;
                    });
                    setInterval(function (){
                        _this.getJds( (splits)=>{
                            _this.jds=splits;
                        });
                    },10*1000);
                },
                getJds(callback){
                    _tvFunc.videoReady(function (video){
                        console.log("videoReady getJds",video.duration)
                        if(video.duration=="Infinity"){
                            callback([])
                            return;
                        }
                        let timeAll = video.duration;
                        if(timeAll<120){
                            return;
                        }
                        let num=20;
                        if(timeAll<600){
                            num=10;
                        }
                        if(timeAll>3600){
                            num=40;
                        }
                        if(timeAll>60*60*10){
                            timeAll=60*60;
                            num=40;
                        }
                        let splitTime =  timeAll/num;
                        let splits=[];
                        let currentTime=splitTime;
                        for (let i = 0; i < num-1; i++) {
                            let minute = Math.floor(currentTime/60);
                            let seconds = Math.floor(currentTime%60);
                            let name = `${minute}分${seconds}`;
                            splits.push({id:i+"",name:name,time:currentTime});
                            currentTime+=splitTime;
                        }
                        callback(splits);
                    });
                },
                moveDown(id){
                   return "#tvd-"+id+":.tv-item";
                },
                next(){
                    _ctrl.next();
                    _layer.hide(menuId);
                },
                reload(){
                    window.location.reload();
                },
                jdChoose(item){
                   _tvFunc.getVideo().currentTime=item.time;
                    _layer.hide(menuId);
                   _layer.notifyLess(`进度跳转到${item.name}处`)
                },
                hzChoose(item){
                    _ctrl.hzChoose(item); 
                    this.now.hz=item;
                    _layer.hide(menuId);
                    _layer.notifyLess(`画质切换到${item.name}`)
                },
                rateChoose(item){
                    _ctrl.rateChoose(item);
                    this.now.rate=item;
                    _layer.hide(menuId);
                    _layer.notifyLess(`视频倍数切换到${item.name}`)
                },
                xjChoose(item){
                    _ctrl.xjChoose(item);
                    this.now.xj=item;
                    _layer.hide(menuId);
                },
                dmChoose(){
                    _ctrl.dm(this);
                },
                tvId(value,pre){
                    if(pre){
                        return pre+value;
                    }
                    return 'tv-'+value;
                },
                tabChoose(tabName){
                    console.log("tabName:"+tabName);
                   this.tab=tabName;
                }
            }).mount('#'+menuId);
            return menuId;
        }
    };
    window._detailInitOnly=function(id,index,notReload){
        return  _html.init(id,index,notReload);
    }
    window._detailInit=function(id,index,notReload){
       let menuId = _html.init(id,index,notReload);
      // new MyFocus(menuId).init();
        TvFocus.init(menuId);
       return menuId;
    }
    window._detailData=function(){
        return _html.getData();
    }
})();
