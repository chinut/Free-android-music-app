let _data = {
    vue: null,
    initData(vue) {
        this.vue = vue;
        // 立即执行一次空搜索以显示默认结果
        this.search('');
    },

    // 添加获取搜索建议的方法
    getSuggestions(query) {
        if (!query || query.length < 1) {
            this.vue.suggestions = [];
            return;
        }
        let _this=this;
        let data ={"query":query,"appID":"3172","appKey":"lGhFIPeD3HsO9xEp","pageNum":0,"pageSize":10}
        let url= encodeURIComponent("https://pbaccess.video.qq.com/trpc.videosearch.smartboxServer.HttpRountRecall/Smartbox");
        $$.ajax({
            type: 'POST',
            url: "/api/sug_json",
            data:JSON.stringify(data),
            headers: {"rel-url":url,"tv-ref":"https://pbaccess.video.qq.com/"},
            async: true,
            dataType: 'json',
            contentType: "application/json;charset=utf-8",
            success: function (data) {
                console.log(data);
                let suggestions=[];
                data.data.smartboxItemList.forEach((item) => {
                    suggestions.push(item.basicDoc.docname);
                });
               const filtered = suggestions.filter(item =>
                    item.trim()!=""
                );
                _this.vue.suggestions = filtered;
                //console.log("html:::"+html);
            },
            error: function (xhr, type) {
                console.log('Ajax error!');
            }
        });
    },
    hot(){
        let url= encodeURIComponent("https://m.douban.com/rexxar/api/v2/tv/recommend?refresh=0&start=0&count=20&selected_categories={}&uncollect=false&sort=U&playable=true&tags=");
        let _this=this;
        _this.vue.results=[];
        $$.ajax({
            type: 'GET',
            url: "/api/hot_json",
            headers: {"rel-url":url,"tv-ref":"https://movie.douban.com/tv/"},
            async: true,
            dataType: 'json',
            contentType: "application/json",
            success: function (data) {
                data.items.forEach((item) => {
                    let desc=`年份:${item.year} 来源:豆瓣`;
                    let pic= "/image?tv-ref=https://www.douban.com/&url="+item.pic.normal;
                    let url= `https://movie.douban.com/subject/${item.id}/`;
                    _this.vue.results.push({title:item.title,desc:desc,pic:pic,url:url})
                })
                //console.log("html:::"+html);
            },
            error: function (xhr, type) {
                console.log('Ajax error!'+url);
                _layer.notify("网络错误");
            }
        });
    },
    search(query) {
        if (!query || query.trim()==="") {
            return this.hot();
        }
        let url= encodeURIComponent("https://movie.douban.com/j/subject_suggest?q="+query);
        let _this=this;
        _this.vue.results=[];
        $$.ajax({
            type: 'GET',
            url: "/api/search_json",
            headers: {"rel-url":url,"tv-ref":"https://movie.douban.com/"},
            async: true,
            dataType: 'json',
            contentType: "application/json",
            success: function (data) {
                data.forEach((item) => {
                    let desc=`年份:${item.year} 来源:豆瓣`;
                    let pic= "/image?tv-ref=https://www.douban.com/&url="+item.img;
                    _this.vue.results.push({title:item.title,desc:desc,pic:pic,url:item.url})
                })
                //console.log("html:::"+html);
            },
            error: function (xhr, type) {
                console.log('Ajax error!'+url);
                _layer.notify("网络错误");
            }
        });
        this.searchQ(query);
    },
    mapKey(key){
        if(key==="qq"){
            return "腾讯";
        }
        if(key==="qiyi"){
            return "爱奇艺";
        }
        if(key==="cntv"){
            return "央视片库";
        }
        if(key==="youku"){
            return "优酷";
        }
        if(key==="imgo"){
            return "芒果";
        }
        if(key==="bilibili1"){
            return "哔哩哔哩";
        }
        if(key==="leshi"){
            return "乐视";
        }
        if(key==="xigua"){
            return "西瓜";
        }
        if(key==="douyin"){
            return "抖音";
        }
        return key;
    },
    searchQ(query){
        let url= encodeURIComponent("https://api.so.360kan.com/index?kw="+query);
        let _this=this;
        $$.ajax({
            type: 'GET',
            url: "/api/json",
            headers: {"rel-url":url,"tv-ref":"https://www.360kan.com/"},
            async: true,
            dataType: 'json',
            contentType: "application/json",
            success: function (data) {
                data.data.longData.rows.forEach((item) => {
                    //${item.coverInfo?.txt}
                    //let detail = item.description;
                    let pic= "/image?tv-ref=https://www.360kan.com/&url="+item.cover;
                    $$.each(item.playlinks,function (k,v){
                        let link=v;
                        if(v instanceof Array){
                            link=v[0].url;
                        }
                        let keyWorld = _this.mapKey(k);
                        let desc=`年份:${item.year}  来源: ${keyWorld}`;
                        _this.vue.results.push({title:item.titleTxt,desc:desc,pic:pic,url:link})
                    });

                })
                //console.log("html:::"+html);
            },
            error: function (xhr, type) {
                console.log('Ajax error!'+url);
                _layer.notify("网络错误");
            }
        });
    }
};

$$(function() {
    const app = PetiteVue.createApp({
        searchQuery: '',
        results: [],
        suggestions: [],
        showSuggestions: false,
        initData(){
            _data.initData(this);
        },
        onSearch() {
            _data.getSuggestions(this.searchQuery);
            //_data.search(this.searchQuery);
        },

        selectSuggestion(suggestion) {
            this.searchQuery = suggestion;
            this.showSuggestions = false;
            _data.search(suggestion);
        },

        openResult(result) {
           // window.location.href = result.url;
            $$.get("/ctrl?url="+result.url);
            _layer.notify("即将在电视端打开对应视频链接");
        },

        doSearch() {
            this.showSuggestions = false;
            _data.search(this.searchQuery);
        },

        mounted() {
            console.log("mounted");
            _data.initData(this);
            
            // 点击页面其他地方时隐藏建议列表
            document.addEventListener('click', (e) => {
                if (!e.target.closest('.search-input-wrapper')) {
                    this.showSuggestions = false;
                }
            });
        }
    });

    app.mount('#app');
}); 