function FOCUS(model) {
    window.my = this
    window.model = model;
    my.domIdName = !model.domIdName ? "item" : model.domIdName
    //my.focusId = !model.focusId ? "0" : model.focusId.replace(my.domIdName, "")
   // my.focusClass = !model.focusClass ? "focus" : model.focusClass
    my.darkClass = !model.darkClass ? "active" : model.darkClass
    my.darkFocus = !model.darkFocus ? [] : model.darkFocus
    my.unFocusArr = !model.unFocusArr ? [] : model.unFocusArr
    my.appendEvent = []
    my.created = !model.created ? function (next) {
        next()
    } : model.created

    this.init()
}
FOCUS.prototype =  {
    focusId: ".tv-focus",
    //focusIdName: "currentFocus",
    focusClass: "tv-focus",
    init() {
        console.log(this.focusClass);
        //$(this.focusId).addClass(this.focusClass);
        this.initKeyEvent();
        this.initEvent(model);
        this.ctrl();
    },
    initEvent: function (model) {
        if (!model.event) model.event = {}
        my.keyNumberEvent = !model.event.keyNumberEvent ? function () { } : model.event.keyNumberEvent
        my.keyPortalEvent = !model.event.keyPortalEvent ? function () { } : model.event.keyPortalEvent
        my.keyMenuEvent = !model.event.keyMenuEvent ? function () { } : model.event.keyMenuEvent
        my.keyPageUpEvent = !model.event.keyPageUpEvent ? function () { } : model.event.keyPageUpEvent
        my.keyPageDownEvent = !model.event.keyPageDownEvent ? function () { } : model.event.keyPageDownEvent
        my.keyDelEvent = !model.event.keyDelEvent ? function () { } : model.event.keyDelEvent
        my.keyVolUpEvent = !model.event.keyVolUpEvent ? function () { } : model.event.keyVolUpEvent
        my.keyVolDownEvent = !model.event.keyVolDownEvent ? function () { } : model.event.keyVolDownEvent
        my.keyMuteEvent = !model.event.keyMuteEvent ? function () { } : model.event.keyMuteEvent
        my.keyPausePlayEvent = !model.event.keyPausePlayEvent ? function () { } : model.event.keyPausePlayEvent
        my.keyMediaErrorEvent = !model.event.keyMediaErrorEvent ? function () { } : model.event.keyMediaErrorEvent
        my.keyMediaEndEvent = !model.event.keyMediaEndEvent ? function () { } : model.event.keyMediaEndEvent
        my.keyPlayModeChange = !model.event.keyPlayModeChange ? function () { } : model.event.keyPlayModeChange
        my.keyMediaBeginEvent = !model.event.keyMediaBeginEvent ? function () { } : model.event.keyMediaBeginEvent
        my.keyDefaultEvent = !model.event.keyDefaultEvent ? function () { } : model.event.keyDefaultEvent
        my.keyBackEvent = !model.event.keyBackEvent ? function () { } : model.event.keyBackEvent
        my.keyOkEvent = !model.event.keyOkEvent ? function () { } : model.event.keyOkEvent
        my.focusEvent = !model.event.focusEvent ? function () { } : model.event.focusEvent
        my.unfocusEvent = !model.event.unfocusEvent ? function () { } : model.event.unfocusEvent
        my.darkFocusEvent = !model.event.darkFocusEvent ? function () { } : model.event.darkFocusEvent
        my.undarkFocusEvent = !model.event.undarkFocusEvent ? function () { } : model.event.undarkFocusEvent
        my.changeIndexBeforeFind = !model.event.changeIndexBeforeFind ? function () { } : model.event.changeIndexBeforeFind
        my.changeIndexAfterFind = !model.event.changeIndexAfterFind ? function () { } : model.event.changeIndexAfterFind
    },
    initKeyEvent() {
        var KEY_BACK = 8,
            KeyQ=81,
            KEY_OK = 13,
            KEY_LEFT = 37,
            KeyA=65,
            KEY_UP = 38,
            KeyW=87,
            KEY_RIGHT = 39,
            KeyD = 68,
            KEY_DOWN = 40,
            KeyS=83,
            KeyR=82
        document.onkeydown = function (e) {
            var keyCode=e.keyCode;
           // console.log(e.keyCode);
           // if(!e.code){ keyCode=e.keyCode;}
            console.log(keyCode);
            switch (keyCode) {
                case KEY_UP:
                    my.keyUpEvent()
                    break
                case KEY_DOWN:
                    my.keyDownEvent()
                    break
                case KeyW:
                    my.keyUpEvent()
                    break;
                case KeyS:
                    my.keyDownEvent()
                    break
                case KeyA:
                    my.keyLeftEvent()
                    break
                case KeyD:
                    my.keyRightEvent()
                    break
                case KeyQ:
                    my.keyBackEvent()
                    break
                case KEY_OK:
                    my.keyOkEvent()
                    break
                case KeyR:
                    my.keyMenuEvent();
                    break
                default:
                    console.log("CCCCC");
                    //my.keyDefaultEvent(e.keyCode)
                    //e.preventDefault();
                    break
            }
        }
    },
    ctrl(){
        window._menuCtrl={
            menu(){
                my.keyMenuEvent();
            },
            ok(){
                my.keyOkEvent();
            },
            back(){
                my.keyBackEvent();
            },
            left(){
                my.keyLeftEvent();
            },
            right(){
                my.keyRightEvent();
            },
            down(){
                my.keyDownEvent();
            },
            up(){
                my.keyUpEvent();
            }
        }
    },
    keyDownEvent(){
        var elem= $(this.focusId);
        var updownNum= elem.attr("move-updown");
        if(updownNum){
            this.move(this.next(elem,updownNum));
        }
        this.moveBack(elem,"move-down");
    },
    moveBack(elem,attr){
        var attrElem= elem.attr(attr);
        if(attrElem){
            var strs =  attrElem.split(":");
            if(strs.length==1){
                if($(attrElem).length>0){
                    this.move($(attrElem).first());
                }else{
                    this.moveBack(elem,attr+"b");
                }
            }
            if(strs.length==2){
                this.move($(strs[0]).find(strs[1]).first());
            }   
        }
    },
    keyUpEvent(){
        var elem= $(this.focusId);
        var updownNum= elem.attr("move-updown");
        if(updownNum){
            this.move(this.prev(elem,updownNum));
            return;
        }
        var moveUp= elem.attr("move-up");
        if(moveUp){
            this.move($(moveUp).first());
        }
        
    },
    keyRightEvent() {
        console.log("keyRightEvent");
        this.move($(this.focusId).next());
    },
    next(elem,num){
        //var elem= $(this.focusId);
        for(var i=0;i<num;i++){
            if(elem.next()&&elem.next().length>0){
                elem=elem.next();
            }
        }
        return elem;
    },
    prev(elem,num){
        //var elem= $(this.focusId);
        for(var i=0;i<num;i++){
            if(elem.prev()&&elem.prev().length>0){
                elem=elem.prev();
            }else{
                var moveUp= elem.attr("move-up");
                return $(moveUp);
            }
        }
        return elem;
    },
    move(nextElm){
        if(nextElm&&nextElm.length>0){
            $(this.focusId).removeClass(this.focusClass);
            // var nextElm=$(this.focusId).next();
            // $(this.focusId).removeAttr("id");
            this.foucsActive(nextElm);
            // nextElm.attr('id', this.focusIdName);
             my.scrollTo();
             my.focusEvent(my.focusId);
        }
    }, 
    foucsActive(elem){
        elem.addClass(this.focusClass);
        //elem.css("background-color","#16baaa");
    },
    isElementInViewport(el) {
        var rect = el.getBoundingClientRect();
        return (
          rect.top >= 0 &&
          rect.left >= 0 &&
          rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&
          rect.right <= (window.innerWidth || document.documentElement.clientWidth)
        );
      },   
      scrollTo() {
              var el = document.querySelector(this.focusId);
              var flag= my.isElementInViewport(el);
              console.log(flag);
              if(!flag){
                  setTimeout(() => {
                      el.scrollIntoView({ behavior: "smooth", block: "start", inline: "nearest" });
                  },50);
              }
              //console.log(offset);
              //element.scrollIntoView({behavior: "instant", block: "end", inline: "nearest"});
              //el.scrollIntoView();
    },        
    keyLeftEvent() {
        console.log("keyLeftEvent");
        this.move($(this.focusId).prev());
    }
}

