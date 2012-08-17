var CommandInterface = 
(function() {
   function CommandInterface() {};

   var __hasProp = Object.prototype.hasOwnProperty,
   __extends = function(child, parent) { 
     for (var key in parent) { 
       if (__hasProp.call(parent, key)) 
         child[key] = parent[key]; 
     }
     function ctor() { this.constructor = child; }
     ctor.prototype = parent.prototype;
     child.prototype = new ctor;
     child.__super__ = parent.prototype; 
     return child;
   },
   __bind = function(fn, me){ 
     return function(){ return fn.apply(me, arguments); }; 
   };

   var AppImpl = 
     (function() {
        function AppImpl() {}

        AppImpl.prototype.init = function(params) {
          throw new Error('proton.app.AppImpl.init must be overridden');
        };

        AppImpl.prototype.execute = function(command, args) {
          throw new Error('proton.app.AppImpl.execute must be overridden');
        };

        AppImpl.prototype.getParams = function() {
          throw new Error('proton.app.AppImpl.getParams must be overridden');
        };

        AppImpl.prototype.onCommandInvoked = function(command) {
          throw new Error('proton.app.AppImpl.onCommandInvoked must be overridden');
        };

        AppImpl.prototype.onCommandCompleted = function(command, serial, result) {
          throw new Error('proton.app.AppImpl.onCommandCompleted must be overridden');
        };
        return AppImpl;
      })();
   // Android
   var Android = 
     (function(_super) {
        __extends(Android, _super);

        function Android() {
          Android.__super__.constructor.apply(this, arguments);
        }

        Android.prototype.interface_ = null;

        Android.prototype.init = function(params) {
          var _ref;
          return (this.interface_ = window[(_ref = params.interfaceName) != null ? _ref : 'protonapp']) != null;
        };

        Android.prototype.execute = function(command, args) {
          var _base;
          if (typeof (_base = this.interface_).executeCommand === "function") {
            _base.executeCommand(command, JSON.stringify(args));
          }
        };

        Android.prototype.getParam = function() {};
        Android.prototype.onCommandInvoked = function(command) {};
        Android.prototype.onCommandCompleted = function(command, serial, result) { return "ok" };
        return Android;
      })(AppImpl);

   //IOS
   var IOS = 
     (function(_super) {
        __extends(IOS, _super);
        function IOS() {
          this.run_ = __bind(this.run_, this);
          IOS.__super__.constructor.apply(this, arguments);
        }

        IOS.COMMAND_FORMAT_ = '%INTERFACE%://%COMMAND%/?serial=%SERIAL%';

        IOS.prototype.interfaceName_ = 'proton';

        IOS.prototype.commandQueue_ = [];

        IOS.prototype.queueMonitor_ = null;

        IOS.prototype.isRunning_ = false;

        IOS.prototype.iframe_ = null;

        IOS.prototype.serial_ = 0;

        IOS.prototype.contexts_ = {};

        IOS.prototype.init = function(params) {
          var _ref;
          this.interfaceName_ = (_ref = params.interfaceName) != null ? _ref : 'proton';
          this.iframe_ = document.createElement('iframe');
          this.iframe_.id = this.interfaceName_;
          this.iframe_.style.display = 'none';
          this.iframe_.src = 'about:blank';
          document.body.appendChild(this.iframe_);
          this.iframe_.contentWindow.document.writeln('<body></body>');
          return true;
        };

        IOS.prototype.execute = function(command, args) {
          this.commandQueue_.push([command, args]);
          if (!(this.queueMonitor_ != null)) {
            this.queueMonitor_ = window.setInterval(this.run_, 10);
          }
        };

        IOS.prototype.run_ = function(ev) {
          var args, c, command, item, serial;
          if (this.isRunning_) return;
          this.isRunning_ = true;
          item = this.commandQueue_.shift();
          if (this.commandQueue_.length === 0) {
            window.clearInterval(this.queueMonitor_);
            this.queueMonitor_ = null;
          }
          command = item[0];
          args = item[1];
          if (args != null) {
            if (args.callback) {
              serial = ++this.serial_;
              this.contexts_[serial] = args;
            } else {
              serial = 0;
            }
            args = JSON.stringify(args);
            this.iframe_.contentWindow.document.body.appendChild(document.createTextNode(args));
          }
          c = IOS.COMMAND_FORMAT_;
          c = c.replace('%INTERFACE%', this.interfaceName_).replace('%COMMAND%', command).replace('%SERIAL%', serial);
          return this.iframe_.contentWindow.location.href = c;
        };

        IOS.prototype.getParams = function() {
          var params;
          params = this.iframe_.contentWindow.document.body.innerHTML;
          this.iframe_.contentWindow.document.body.innerHTML = null;
          return params;
        };

        IOS.prototype.onCommandInvoked = function(command) {
          this.iframe_.contentWindow.document.body.innerHTML = null;
	        return this.isRunning_ = false;
        };
        IOS.prototype.onCommandCompleted = function(command, serial, result) {
          var args;
          if (serial > 0 && serial in this.contexts_) {
            args = this.contexts_[serial];
            if (typeof args.callback === 'function') {
              args.callback.call(command, args, result);
            }
            delete this.contexts_[serial];
          }
          return "ok";
        };

        return IOS;

      })(AppImpl);

   //Browser
   var Browser = 
     (function(_super) {
        __extends(Browser, _super);

          function Browser() {
            Browser.__super__.constructor.apply(this, arguments);
          }

          Browser.prototype.init = function(params) {
            return true;
          };

          Browser.prototype.execute = function(command, args) {
            console.log(['COMMAND', command, args]);
            var result;
            if (command in commandImplementations) {
              result = commandImplementations[command].call(this, args);
              if ((args.callback != null) && args.callback instanceof Function) {
                return args.callback.apply(command, [args, result]);
              }
            }
          };

        var commandImplementations = {
          readConfiguration: function(args) {
            return {
              platform: 'browser',
              supports: {
                cache: false
              },
              cache_version: 0,
              cache_synchronized: false
            };
          },
          log: function (args) {
            console.log.call(console, args.text)
            return true
          }
        };

          Browser.prototype.getParams = function() {
            return null;
          };

          Browser.prototype.onCommandInvoked = function(command) {};
          Browser.prototype.onCommandCompleted = function(command, serial, result) { return "ok" };

        return Browser;
      })(AppImpl);

   var WindowsPhone = 
     (function(_super) {
        __extends(WindowsPhone, _super);

        function WindowsPhone() {
          WindowsPhone.__super__.constructor.apply(this, arguments);
        }

        WindowsPhone.prototype.init = function(params) {
          return true;
        };

        WindowsPhone.prototype.execute = function(command, args) {
          window.external.notify("" + command + ":::" + (JSON.stringify(args)));
          console.log(['COMMAND', command, args]);
        };

        return WindowsPhone;
      })(AppImpl);

   var initialized_ = false;
   var appImpl_ = null;
   var callbacks_ = {};

   var init = function(appImpl, params) {     
     if (initialized_ || appImpl == null) {
       return false; 
     }
     if (params == null) {
       params = {};
     }
     appImpl_ = appImpl;
     initialized_ = true;
     appImpl_.init(params);
     return true;
   };

   CommandInterface.initAndroid = function(params) {     
     init(new Android(), params);
   };
   CommandInterface.initIOS = function(params) {     
     init(new IOS(), params);
   };
   CommandInterface.initBrowser = function(params) {     
     init(new Browser(), params);
   };
   CommandInterface.initWindowsPhone = function(params) {     
     init(new WindowsPhone(), params);
   };

   CommandInterface.execute = function(command, args) {
     if (args == null) args = {};
     if (!initialized_) return;
     appImpl_.execute(command, args);
   };
   CommandInterface.onCommandInvoked = function(command) {
     appImpl_.onCommandInvoked(command);
   };
   CommandInterface.onCommandCompleted = function(command, serial, result) {
     return appImpl_.onCommandCompleted.apply(appImpl_, arguments);
   };

   CommandInterface.getParams = function() {
     appImpl_.getParams();
   };
   CommandInterface.callback = function(var_args) {
     var _ref, id, args;
     id = arguments[0];
     args = Array.prototype.slice.call(arguments, 1);
     return (_ref = callbacks_[id]) != null ? _ref.apply(CommandInterface.app, args) : void 0;
   };
   CommandInterface.addCallback = function(id, callback) {
     return callbacks_[id] = callback;
   };
   CommandInterface.removeCallback = function(id) {
     delete callbacks_[id];
   };

   //CommandInterface.
   return CommandInterface;
})();