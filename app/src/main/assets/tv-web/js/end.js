(function(){
    function loadCssCode(code) {
       var style = document.createElement('style')
      // style.type = 'text/css'
       style.rel = 'stylesheet'
       style.appendChild(document.createTextNode(code))
       var head = document.getElementsByTagName('head')[0]
       head.appendChild(style)
    }
  function createDiv(html,index,idName,...classNames){
     var myDiv = document.createElement("div");
     if(!idName){
         idName="tv-index-"+index;
     }
     myDiv.id = idName;
     myDiv.style.zIndex=index;
     myDiv.style.visibility="visible";
     myDiv.style.background="#E3EDCD";
     if(classNames.length==0){
         myDiv.className="tv-index";
     }else{
         myDiv.className=classNames.join(" ");
     }
     myDiv.innerHTML = html;
     document.body.appendChild(myDiv);
     return idName;
 };
 var css=`
 body{
   visibility:visible;
 }
 .utv-video-full{
   position: fixed !important;
    z-index: 99990 !important;
    width: 100vw !important;
    height: 100vh !important;
    top: 0 !important;
    left: 0 !important;
    right:0 !important;
    bottom: 0 !important;
    background-color: rgb(0, 0, 0); 
 }
 `;
 loadCssCode(css);
  let html=`
  <div style="font-size:3vw; text-align: center;padding:10vh 3vw">
  请耐心等待视频或页面加载 加载速度取决于你电视或电视盒子的性能(电视盒子其实就是安卓手机的变形 至少也得500+的才流畅)<br/>
  <b>无需开通各大视频平台电视端会员 普通会员即可！</b> <br/>
  每年可以省下至少二三百块 买个好的电视盒子 被各大视频平台霸凌了这么久，还不能享受享受😏
  </div>
  `;
  //createDiv(html,9999999,"_tv_begin");
 })();

 var _tvLoadRes={
     js(scrJs){
         let script = document.createElement('script');
         script.setAttribute('type', 'text/javascript');
         script.src = scrJs;
         script.async = false;
         document.body.appendChild(script);
     },
     css(scrCss){
         let script = document.createElement('link');
         script.setAttribute('rel', 'stylesheet');
         script.setAttribute('type', 'text/css');
         script.href = scrCss;
         script.async = false;
         document.head.appendChild(script);
     },
     jsBottom(scrJs){
         let script = document.createElement('script');
         script.setAttribute('type', 'text/javascript');
         script.src = scrJs;
         script.async = false;
         document.head.appendChild(script);
     }
 };
 var _browser={
    config:{
         isTvApp:false,
    },
    getURL(src){
       let  baseUrl="https://www.utao.tv/tv-web/";
           if(window.location.href.startsWith("https://www.bestv.com.cn/web/play/")){
               baseUrl="https://www.bestv.com.cn/tv-web/";
           }
       return baseUrl+src;
    },
     app(normal,callback){
           if(!this.config.isTvApp){
               normal();
               return;
           }
           callback();
      }
};
 _tvIsApp=true;