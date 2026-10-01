
    const _login={
        loginHtml(html) {
            return `
            <div style="font-size:2vw;line-height:3vw; color: black;text-align: center;padding:1vh 1vw">该平台需要登录才可观看视频<br/>
            请打开手机端咪咕视频扫一扫登录</div>
            <div class="tv-header tv-flex-around">
               <div class="tv-btn tv-focus"  id="tv-reload" onclick="window.location.reload();">刷新</div>
            </div>
            <div>
            <div  id="_tv_login" class="tv-tab tv-tab-flex scan-code" style="justify-content:center;">
                 <img src="${html}" height=250 width=250 />
            <div>
            </div>
           `;
        },
    }
    $$(function(){
        console.log("isReady isReady");
        _tvFunc.check(function(){return $$("#qrccode").length>0;},function(){
            $$("#qrccode").click();
            _tvFunc.check(function(){return $$(".qr-code-img").length>0;},function(){
                let html=   $$(".qr-code-img").attr("src");
               //_login.loginHtml(html);
                _layer.open(_login.loginHtml(html), 99999, null, "tv-index", "tv-bg-yellow");
                TvFocus.init();
            });
        });
    });

