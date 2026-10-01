_tvIsApp=false;
_tvIsGecko=true;
_utao_version="20";
let _browser={
   getURL(src){
      return browser.runtime.getURL(src);
   }
};
const _tvLoadRes={
    js(scrJs){
        let script = document.createElement('script');
        script.setAttribute('type', 'text/javascript');
        //charset="utf-8"
        script.setAttribute('charset','utf-8');
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
        script.setAttribute('charset','utf-8');
        script.src = scrJs;
        script.async = false;
        document.head.appendChild(script);
    }
}
let _api={
    message(service,data){
        myPort.postMessage({ service: service,data:data });
    },
    queryByService(service,param){
        if(null==param){
            param="";
        }
        let reqUrl="http://127.0.0.1:8088/service/"+service+"?param="+param;
        return new Promise((resolve, reject) => {
            $$.ajax({
                type: 'GET',
                url: reqUrl,
                async: true,
                dataType: 'json',
                contentType: "application/json",
                success: function (data) {
                    console.log("service ",data)
                    resolve(data.data)
                },
                error: function (xhr, type) {
                    console.log('Ajax error!'+reqUrl);
                    reject("500")
                }
            });
        });
    },
    ajax(params) {
        // 用promise对象封装
            return new Promise((resolve, reject) => {
                $$.ajax({
                    url: params.url,
                    type: params.type || 'get',
                    dataType: 'json',
                    headers: params.headers || {},
                    data: params.data || {},
                    success(res) {
                        resolve(res)
                    },
                    error(err) {
                        reject(err)
                    }
                })
            })
    },
    userAgent(isH5){
        let userAgent="Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36 Edg/128.0.0.0";
        if(isH5){
            userAgent="Mozilla/5.0 (iPhone; CPU iPhone OS 17_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Cronet Mobile/15E148 Safari/604.1";
        }
        return userAgent;
    },
    getHtml(reqUrl,headerStr){
     let header=  JSON.parse(headerStr);
     let result=null;
     $$.ajax({
         type: 'GET',
         url: reqUrl,
         headers:header,
         async: false,
         //dataType: 'json',
         //contentType: "application/json",
         success: function (html) {
             //console.log("html:::"+html);      
             result=html; 
         },
         error: function (xhr, type) {
             console.log('Ajax error!');
             result= "500";
         }
     });
     return result;
    },
    getJson(reqUrl,headerStr){
        let header=  JSON.parse(headerStr);
        let result=null;
        $$.ajax({
            type: 'GET',
            url: reqUrl,
            headers: header,
            async: false,
            dataType: 'json',
            contentType: "application/json",
            success: function (data) {
                result=JSON.stringify(data);
                //console.log("html:::"+html);      
            },
            error: function (xhr, type) {
                console.log('Ajax error!'+reqUrl);
                result= "500";
            }
        });
        return result;
    },
    postJson(reqUrl,headerStr,data){
        let header=  JSON.parse(headerStr);
        let result=null;
        $$.ajax({
            type: 'POST',
            url: reqUrl,
            data:data,
            headers: header,
            async: false,
            dataType: 'json',
            contentType: "application/json",
            success: function (data) {
                result=JSON.stringify(data);
                //console.log("html:::"+html);      
            },
            error: function (xhr, type) {
                console.log('Ajax error!'+reqUrl);
                result= "500";
            }
        });
        return result;
    }
 };

//document.body.hidden=true;
//document.body.innerHTML="<h1>hello23</h1>"
//browser.extension.getURL("page.js")
//exportFunction(_api, window, { defineAs: "_api.gethtml" });
//xAppLoadJsHead(browser.extension.getURL("page.js"));
if(typeof cloneInto!=="undefined"){
    window.wrappedJSObject._api = cloneInto(_api, window, {
        cloneFunctions: true,
    });
    window.wrappedJSObject._browser = cloneInto(_browser, window, {
        cloneFunctions: true,
    });
   // window.wrappedJSObject._tvEnv = cloneInto(_tvEnv, window);
}

var myPort = browser.runtime.connect({ name: "port-from-cs" });
myPort.postMessage({service:"test",data: "hello from content script" });
myPort.onMessage.addListener(function (m) {
  console.log("In content script, received message from background script: "+m.service);
  if(m.service==="sessionStorage"){
    console.log("In content script, sessionStorage ");
        sessionStorage.setItem(m.data.key,m.data.value);
        sessionStorage.setItem(m.data.key+"Time",new Date().getTime());
        return;
  }
  if(m.service==="ctrl"){
      console.log(`from background ${m.data}` );
      _messageCtrl.ctrl(m.data);
      return;
  }
  if(m.service==="redirect"){
    console.log(`from background ${m.data}` );
    window.location.href=m.data.url;
    return;
 }
    if(m.service==="loginQr"){
        let loginQr=window.wrappedJSObject._loginQr;
        loginQr(m.data.url,m.data.type);
        return;
    }
    if(m.service==="click"){
        console.log(`from background ${m.data}` );
        $("#"+m.data).trigger("click");
       // document.getElementById(m.data).click();
        return;
    }
    if(m.service==="js"){
        console.log(`from background ${m.data}` );
        eval(m.data);
        return;
    }
    if(m.service==="app"){
        sessionStorage.setItem("_tvRunEnv",m.data);
    }
});
_data={
hzChoose(id,name){
        $$("#"+id).click();
        //_layer.notifyLess("画质切换到 "+name);
        //_apiX.toast("画质切换到 "+name);
        localStorage.setItem("chooseHz",id);
        localStorage.setItem("chooseHzName",name);
    }
};

const  _messageCtrl={
    ctrl(message){

        let menuCtrl=null;
        if(typeof _menuCtrl == "undefined"){
            menuCtrl=window.wrappedJSObject._menuCtrl;
        }else{
            menuCtrl=_menuCtrl;
        }
            //||window.wrappedJSObject._menuCtrl;
            //||window.wrappedJSObject._menuCtrl ;
        switch (message){
            case "right":
                menuCtrl.right();
                break;
            case "left":
                menuCtrl.left();
                break;
            case "up":
                menuCtrl.up();
                break;
            case "down":
                menuCtrl.down();
                break;
            case "ok":
                menuCtrl.ok();
                break;
            case "menu":
                menuCtrl.menu();
                break;
            case "back":
                menuCtrl.back();
                break;
        }
    }
}
_tvIsGec=true;
/* function notify(message) {
    console.log("message");
     console.dir(message);
}
browser.runtime.onMessage.addListener(notify); */
