
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
            }
        },
        init(id,index,notReload){
            let menuId= this.initApp(id,index);
            this.initCtrl();
            if(notReload){
                return menuId;
            }
            return menuId;
        },
        initCtrl(){
            this._ctrl.play = !_ctrlx.play ? function () { $$("#xtv-play").click();} : _ctrlx.play;
        },
        initHtml(){
            const html = `
            <div class="tv-body" id="tv-body" style="background:none;" @vue:mounted="initData()">
            <div class="tv-header">
                <div  tabindex="0" v-for="item in channels" class="tv-btn" :id="tvId(item.tag)" :class="{'tv-active':item.tag==currentChannel.tag}"
                 @focus="switchChannel(item)" @click="switchChannel(item)"  :move-down="moveDown(item.tag)" >{{item.name}}</div>
            </div>
            <div  v-for="item in channels" class="tv-content" :id="tvId(item.tag,'tvd-')" :style="{display:(item.tag==currentChannel.tag ? 'grid' : 'none')}">
               <div tabindex="0"  v-for="(vod,index) in item.vods"    @click="goto(vod)" :id="tvId(index,item.tag)" :move-updown-id=item.tag class="tv-item"
                 move-updown="5" :move-up="tvId(item.tag,'#tv-')">
                    <img v-if="vod.pic" :src=vod.pic :alt=vod.name />
                    <span :class="{'tv-btn':!vod.pic}">{{vod.name}}{{vod.remark}}</span>
               </div>
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
                    hz:{},
                },
                channels:[],
                currentChannel:null,
                focusId:"tv",
                hzs:[],
                isVip:true,
                initData(){
                    let _this=this;
                    window.tv_vue=this;
                    _html.vue=this;
                    _html.xjList(_this);
                  if(this.channels.length>0){
                      let url=decodeURI(window.location.href);
                      let currentChannel=null;
                      this.channels.forEach((channel,index)=>{
                          channel.vods.forEach((item,index)=>{
                              if(url.startsWith(item.url)){
                                  currentChannel=channel;
                                  return;
                              }
                          })
                          if(null!=currentChannel){
                              return;
                          }
                      });
                         if(null==currentChannel){
                             currentChannel=this.channels[0];
                         }
                      this.currentChannel=currentChannel;
                         this.$nextTick(()=>{
                             let idFirst = "#"+this.tvId(this.currentChannel.tag);
                             $$(idFirst).addClass("tv-focus");
                         });
                     }
                    window._isVideo=this.video;
                },
                switchChannel(item){
                    console.log("switchChannel",item.tag);
                    this.currentChannel=item;
                    let channelId = "#"+this.tvId(item.tag);
                    let hasFocus= $$(channelId).hasClass("tv-focus");
                    if(item.vods.length===0){
                        _data.channelPage(item);
                    }
                    this.$nextTick(function (){
                        console.log("hasFocus",hasFocus);
                        if(hasFocus){
                            $$(channelId).addClass("tv-focus");
                        }
                    })

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
                hzChoose(item){
                    _ctrl.hzChoose(item);
                    this.now.hz=item;
                    _layer.hide(menuId);
                    _layer.notifyLess(`画质切换到${item.name}`)
                },
                goto(item){
                    item.url=_tvFunc.url(item.url);
                    console.log(item.url);
                    window.location.href = item.url;
                },
                xjChoose(item){
                    _ctrl.xjChoose(item);
                    this.now.xj=item;
                    _layer.hide(menuId);
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
 /* */
    window._detailHz=function (){
        _data.initData(tv_vue,function(){
            //vip自动选择画质1080p
            //非vip 选择不是vip的最高
            let _this=tv_vue;
            console.log("hzLevel",_this.now.hz.level);
            if(_this.now.hz.level<1080){
                console.log("need qie")
                $$.each(_this.hzs,function(index,item){
                    if(item.level===1080){
                        setTimeout(function(){
                            _layer.notify("正在切换到"+item.name);
                            _this.hzChoose(item);
                        },2000);
                        return false;
                    }
                });
            }
        });
    }
    window._detailInitOnly=function(id,index,notReload){
        return  _html.init(id,index,notReload);
    }
    window._detailInit=function(id,index,notReload){
        let menuId = _html.init(id,index,notReload);
        // new MyFocus(menuId).init();
        TvFocus.init(menuId);
        _menuCtrl.menu=function (){
            TvFocus.keyMenuEvent();
        };
    /*    $$("video").on("click", function(){
            console.log("video click")
            _menuCtrl.menu();
        });*/
        return menuId;
    }
    window._detailData=function(){
        return _html.getData();
    }
})();
