const _ctrlx = {
    ok() {
        // 播放/暂停控制
        _tvFunc.videoPlay();
        _layer.notifyLess("OK键暂停或播放");
    },
    fullscreen() {
        // 全屏控制
        _tvFunc.fullscreen("video");
    }
};

(function(){
    const _app = {
        init() {
            _detailInit(null, 999999);
        }
    };

    const _login = {
        init() {
            // 检查是否需要登录
            _tvFunc.check(function(){
                return $(".login-btn").length > 0 || $(".user-login").length > 0;
            }, function(){
                let loginUrl = "https://v.vcinema.cn/login";
                _layer.open(_login.loginHtml(loginUrl), 99999, null, "tv-index", "tv-bg-yellow");
                TvFocus.init();
            });
        },
        loginHtml(loginUrl) {
            return `
            <div style="font-size:2vw;line-height:3vw; color: black;text-align: center;padding:1vh 1vw">
                南瓜电影需要登录才能观看高清视频<br/>
                请打开手机访问 ${loginUrl} 进行登录
            </div>
            <div class="tv-header tv-flex-around">
               <div class="tv-btn tv-focus" id="tv-reload" onclick="window.location.reload();">刷新页面</div>
            </div>
           `;
        }
    };

    $(function(){
        console.log("南瓜电影详情页初始化");
        
        // 检查登录状态
        _tvFunc.check(function(){
            return $(".user-info").length > 0 || !$(".login-btn").length;
        }, function(){
            _app.init();
        });

        // 如果需要登录
        if($(".login-btn").length > 0) {
            _login.init();
        } else {
            _app.init();
        }

        _ctrlx.fullscreen();
    });
})();

const _data = {
    vue: null,
    initData(vue, hzCallback) {
        this.vue = vue;
        _tvFunc.check(function(){
            return $("video").length > 0 || $(".video-player").length > 0;
        }, function(){
            _data.fullscreen();
            _data.hzList(hzCallback);
            _data.xjList();
        });
    },
    fullscreen() {
        // 自动全屏
        _tvFunc.volume100();
        
        // 隐藏不需要的元素
        $(".header").hide();
        $(".footer").hide();
        $(".nav").hide();
        $(".sidebar").hide();
        
        // 设置视频全屏
        if($("video").length > 0) {
            $("video").addClass("utv-video-full");
        }
    },
    hzList(hzCallback) {
        // 画质列表处理
        this.vue.hzs = [];
        
        $(".quality-list .quality-item").each(function(index, item){
            let hzName = $(item).text().trim();
            let isVip = $(item).hasClass("vip") || hzName.includes("VIP");
            let id = index.toString();
            $(item).attr("id", "xhz-" + id);
            
            let itemData = {
                id: id,
                name: hzName,
                isVip: isVip,
                level: _tvFunc.hzLevel(hzName, 1)
            };
            
            if($(item).hasClass("active")) {
                _data.vue.now.hz = itemData;
            }
            
            _data.vue.hzs.push(itemData);
        });
        
        if(hzCallback) {
            hzCallback();
        }
    },
    xjList() {
        // 选集列表处理
        this.vue.xjs = [];
        let currentEpisode = null;
        
        $(".episode-list .episode-item").each(function(index, item){
            let episodeTitle = $(item).text().trim();
            let episodeUrl = $(item).attr("href") || $(item).data("url");
            let episodeId = $(item).data("id") || index;
            
            let itemData = {
                id: episodeId,
                name: episodeTitle,
                url: episodeUrl,
                title: _tvFunc.title(episodeTitle),
                index: index,
                site: "ngys",
                isVip: $(item).hasClass("vip")
            };
            
            if($(item).hasClass("active") || $(item).hasClass("current")) {
                currentEpisode = itemData;
                _data.vue.now.xj = itemData;
            }
            
            _data.vue.xjs.push(itemData);
        });
        
        if(currentEpisode) {
            _tvFunc.currentXj(currentEpisode);
        }
    },
    rateList() {
        // 播放速度控制
        _data.vue.rates = [];
        _data.vue.rates.push({id:"05X", name:"0.5", isCurrent:false, videoDo:true});
        _data.vue.rates.push({id:"075X", name:"0.75", isCurrent:false, videoDo:true});
        _data.vue.rates.push({id:"10X", name:"1.0", isCurrent:true, videoDo:true});
        _data.vue.rates.push({id:"125X", name:"1.25", isCurrent:false, videoDo:true});
        _data.vue.rates.push({id:"15X", name:"1.5", isCurrent:false, videoDo:true});
        _data.vue.rates.push({id:"20X", name:"2.0", isCurrent:false, videoDo:true});
    }
};