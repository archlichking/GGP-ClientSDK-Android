(function() {
   if (typeof(window.proton) === 'undefined') {
     var __indexOf = Array.prototype.indexOf || function(item) { for (var i = 0, l = this.length; i < l; i++) { if (i in this && this[i] === item) {return i;} } return -1; };

     var userAgent = 
       (function() {
          function userAgent() {}
          var conditions = {
            ProtonApp: false,
            WebKit: /WebKit/,
            Android: /Android/,
            IOS: /\((iPhone( Simulator)?|iPod|iPad);/,
            WindowsPhone: /\bWindows Phone OS\b/,
            Tablet: function(ua) {
              return userAgent.isAndroid() && !/\bMobile\s*Safari\b/.test(navigator.userAgent);
            },
            IS03: function(ua) {
              return userAgent.isAndroid() && __indexOf.call(navigator.userAgent, 'IS03') >= 0;
            },
            smallIOSDevice: function(ua) {
              return userAgent.isIOS() && !userAgent.isTablet();
            }
          };

          var runCondition = function(condition) {
            if (toString.call(condition) == '[object Function]') {
              return condition(navigator.userAgent);
            } else if (toString.call(condition) == '[object RegExp]') {
              return condition.test(navigator.userAgent);
            } else {
              return condition;
            }
          };

          var cache = {};
          var _fn = 
            function(name, condition) {
              return userAgent["is" + name] = function() {
                var _ref;
                return (_ref = cache[name]) != null ? _ref : cache[name] = runCondition(condition);
              };
            };

          for (var n in conditions) {
            _fn(n, conditions[n]);
          }
          return userAgent;
        })();

     var proton = {};
     var commands_ = {
       GET_CONTACT_LIST: 'get_contact_list',
       GET_VALUE: 'get_value',
       SET_VALUE: 'set_value',
       SET_PULL_TO_REFRESH_ENABLED : 'set_pull_to_refresh_enabled',
       PUSH_VIEW_WITH_URL: 'push_view_with_url',
       POP_VIEW: 'pop_view',
       OPEN_EXTERNAL_VIEW: 'open_external_view',
       SET_SUBNAVIGATION: 'set_subnavigation',
       CLOSE_POPUP: 'close_popup',
       PAGE_LOADED: 'page_loaded',
       NEED_UPDATE: 'need_update',
       NEED_UPGRADE: 'need_upgrade',
       NEED_RE_AUTHORIZE: 'need_re_authorize',
       LAUNCH_MAIL_COMPOSER: 'launch_mail_composer',
       LAUNCH_SMS_COMPOSER: 'launch_sms_composer',
       LAUNCH_NATIVE_BROWSER: 'launch_native_browser',
       SHOW_MESSAGE_DIALOG: 'show_message_dialog',
       SHOW_SHARE_DIALOG: 'show_share_dialog',
       LAUNCH_NATIVE_APP: 'launch_native_app',
       SHOW_WEBVIEW_DIALOG: 'show_webview_dialog',
       CLOSE_REQUEST_SERVICE_POPUP: 'close_request_service_popup', // FIXME - Obsolete?
       SHOW_REQUEST_DIALOG: 'show_request_dialog',
       RECORD_ANALYTICS_DATA: 'record_analytics_data',
       FLUSH_ANALYTICS_DATA: 'flush_analytics_data',
       ADD_ANALYTICS_EVENT: 'add_analytics_event',
       FLUSH_ANALYTICS_QUEUE: 'flush_analytics_queue',
       SHOW_ALERT_VIEW: 'show_alert_view',
       SHOW_ACTION_SHEET: 'show_action_sheet',
       SHOW_DASHBOARD: 'show_dashboard',
       TAKE_PHOTO: 'take_photo',
       SHOW_PHOTO: 'show_photo',
       DEPOSIT_PRODUCT: 'deposit_product',
       SHOW_INVITE_DIALOG: 'show_invite_dialog',
       SHOW_DEPOSIT_PRODUCT_DIALOG: 'show_deposit_product_dialog',
       CLOSE_AND_LAUNCH_IAP_HISTORY_DIALOG: 'close_and_launch_iap_history_dialog',
       COLLATE_FOR_DEPOSIT: 'collate_for_deposit',
       CONTACT_FOR_DEPOSIT: 'contact_for_deposit',
       NOTICE_LAUNCH_DEPOSIT: 'notice_launch_deposit',
       SHOW_DEPOSIT_HISTORY_DIALOG: 'show_deposit_history_dialog',
       INVITE_EXTERNAL_USER: 'invite_external_user',
       SEE_MORE: 'see_more',
       SHOW_WA_NAVIGATION_BAR: 'show_wa_navigation_bar',
       LOGOUT: 'logout',
       SHOW_DASHBOARD_FROM_NOTIFICATION_BOARD: 'show_dashboard_from_notification_board',
       LAUNCH_SERVICE: 'launch_service',
       NOTIFY_SERVICE_RESULT: 'notify_service_result',
       REGISTER_LOCAL_NOTIFICATION_TIMER: 'register_local_notification_timer',
       CANCEL_LOCAL_NOTIFICATION_TIMER: 'cancel_local_notification_timer',
       GET_LOCAL_NOTIFICATION_ENABLED: 'get_local_notification_enabled',
       SET_LOCAL_NOTIFICATION_ENABLED: 'set_local_notification_enabled',
       CLOSE: 'close',
       SET_CONFIG: 'set_config',
       GET_CONFIG: 'get_config',
       GET_CONFIG_LIST: 'get_config_list',
       GET_APP_LIST: 'get_app_list',
       GET_VIEW_INFO: 'get_view_info',
       CONTENTS_READY: 'contents_ready',
       DELETE_COOKIE: 'delete_cookie',
       UPDATE_USER: 'update_user',
       CONTROL_SOUND: 'control_sound',
       START_LOG: 'start_log',
       STOP_LOG: 'stop_log'
     };

     proton.app = {
       makeInstance : function(userAgent, params) {
         if (userAgent.isAndroid()) {
           return CommandInterface.initAndroid(params);
         } else if (userAgent.isIOS()) {
           return CommandInterface.initIOS(params);
         } else {
           return CommandInterface.initBrowser(params);
         }
       },
       init : function(params) {
         var _ref;
         if (params == null) params = {};
         return !proton.app.makeInstance(userAgent, params);
       },
       execute : function(command, args) {
         CommandInterface.execute(command, args);
       },
       getParams : function() {
         return CommandInterface.getParams();
       },
       callback : function(id, params) {
         return CommandInterface.callback(id, params);
       },
       addCallback : function(id, callback) {
         return CommandInterface.addCallback(id, callback);
       },
       onCommandInvoked : function(command) {
         return CommandInterface.onCommandInvoked(command);
       },
       onCommandCompleted : function(command, serial, result) {
         return CommandInterface.onCommandCompleted(command, serial, result);
       },

       // Command
       getContactList : function(callback) {
         var callback_id, params;
         params = {};
         callback_id = commands_.GET_CONTACT_LIST;
         params.callback = callback_id;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.GET_CONTACT_LIST, params);
       },
       getValue : function(params, callback) {
         var callback_id;
         if (params == null) params = {};
         callback_id = commands_.GET_VALUE;
         params.callback = callback_id;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.GET_VALUE, params);
       },
       setValue : function(params) {
         if (params == null) params = {};
         return proton.app.execute(commands_.SET_VALUE, params);
       },
       setPullToRefresh : function(enabled) {
         return proton.app.execute(commands_.SET_PULL_TO_REFRESH_ENABLED, {enabled: enabled != null ? enabled : true});
       },
       pushViewWithURL : function(url, params) {
         if (params == null) params = {};
         params.url = url;
         return proton.app.execute(commands_.PUSH_VIEW_WITH_URL, params);
       },
       popView : function() {
         return proton.app.execute(commands_.POP_VIEW);
       },
       openExternalView : function(url, params) {
         if (params == null) params = {};
         params.url = url;
         return proton.app.execute(commands_.OPEN_EXTERNAL_VIEW, params);
       },
       setSubNavigation : function(subNavigation) {
         if (subNavigation == null) subNavigation = {};
         var i;
         for (i = 0; i < subNavigation.length; i++) {
           var nav = subNavigation[i];
           var callbackId = 'subNavigationPressed_' + nav.id;
           var callback = nav.callback;
           proton.app.addCallback(callbackId, callback);
           nav.callback = callbackId;
         }
         return proton.app.execute(commands_.SET_SUBNAVIGATION, {
                                  "subNavigation" : {"subNavigation": subNavigation }
                                });
       },
       closePopup : function (params) {
         if (params == null) params = {};
         return proton.app.execute(commands_.CLOSE_POPUP, params);
       },
       pageLoaded : function() {
         proton.app.execute(commands_.PAGE_LOADED);
       },
       needUpdate : function(params) {
         if (params == null) params = {};
         return proton.app.execute(commands_.NEED_UPDATE, params);
       },
       needUpgrade : function(params, callback) {
         if (params == null) params = {};
         if (callback) {
           callback_id = commands_.NEED_UPGRADE;
           params.callback = callback_id;
           proton.app.addCallback(callback_id, callback);
         }
         return proton.app.execute(commands_.NEED_UPGRADE, params);
       },
       needReAuthorize : function(params) {
         if (params == null) params = {};
         return proton.app.execute(commands_.NEED_RE_AUTHORIZE, params);
       },
       launchMailer : function(params, callback) {
         var callback_id;
         if (params == null) params = {};
         callback_id = commands_.LAUNCH_MAIL_COMPOSER;
         params.callback = callback_id;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.LAUNCH_MAIL_COMPOSER, params);
       },
       launchSMSComposer : function(params, callback) {
         var callback_id;
         if (params == null) params = {};
         callback_id = commands_.LAUNCH_SMS_COMPOSER;
         params.callback = callback_id;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.LAUNCH_SMS_COMPOSER, params);
       },
       launchNativeBrowser : function(params, callback) {
         var callback_id;
         if (params == null) params = {};
         callback_id = commands_.LAUNCH_NATIVE_BROWSER;
         params.callback = callback_id;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.LAUNCH_NATIVE_BROWSER, params);
       },
       showMessageDialog : function(params, callback) {
         var callback_id;
         if (params == null) params = {};
         callback_id = commands_.SHOW_MESSAGE_DIALOG;
         params.callback = callback_id;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.SHOW_MESSAGE_DIALOG, params);
       },
       showShareDialog : function(params, callback) {
         var callback_id;
         if (params == null) params = {};
         callback_id = commands_.SHOW_SHARE_DIALOG;
         params.callback = callback_id;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.SHOW_SHARE_DIALOG, params);
       },
       launchNativeApp : function(params, callback) {
         var callback_id;
         if (params == null) params = {};
         callback_id = commands_.LAUNCH_NATIVE_APP;
         params.callback = callback_id;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.LAUNCH_NATIVE_APP, params);
       },
       showWebViewDialog : function(params, callback) {
         var callback_id;
         if (params == null) params = {};
         callback_id = commands_.SHOW_WEBVIEW_DIALOG;
         params.callback = callback_id;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.SHOW_WEBVIEW_DIALOG, params);
       },
       showRequestDialog : function(params, callback) {
         var callback_id;
         if (params == null) params = {};
         callback_id = commands_.SHOW_REQUEST_DIALOG;
         params.callback = callback_id;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.SHOW_REQUEST_DIALOG, params);
       },
       recordAnayticsData : function(params, callback) {
         var callback_id;
         if (params == null) params = {};
         callback_id = commands_.RECORD_ANALYTICS_DATA;
         params.callback = callback_id;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.RECORD_ANALYTICS_DATA, params);
       },
       addAnalyticsEvent: function(params) {
         if (params == null) params = {};
         return proton.app.execute(commands_.ADD_ANALYTICS_EVENT, params);
       },
       flushAnalyticsQueue: function(callback) {
         var callback_id, params;
         params = {};
         callback_id = commands_.FLUSH_ANALYTICS_QUEUE;
         params.callback = callback_id;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.FLUSH_ANALYTICS_QUEUE, params);
       },
       flushAnalyticsData : function() {
         var callback_id;
         var params = {};
         callback_id = commands_.FLUSH_ANALYTICS_DATA;
         return proton.app.execute(commands_.FLUSH_ANALYTICS_DATA, params);
       },
       showAlertView : function(params, callback) {
         if (params == null) params = {};
         callback_id = commands_.SHOW_ALERT_VIEW;
         params.callback = callback_id;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.SHOW_ALERT_VIEW, params);
       },
       showActionSheet : function(params, callback) {
         if (params == null) params = {};
         callback_id = commands_.SHOW_ACTION_SHEET;
         params.callback = callback_id;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.SHOW_ACTION_SHEET, params);
       },
       showDashboard : function(params, callback) {
         var callback_id;
         if (params == null) params = {};
         callback_id = commands_.SHOW_DASHBOARD;
         params.callback = callback_id;
         proton.app.addCallback(callback_id, callback);
         if (typeof params.open !== "undefined" && params.open !== null) {
           var openCallback_id = commands_.SHOW_DASHBOARD + "_open";
           params.openCallback = openCallback_id;
           proton.app.addCallback(openCallback_id, params.open);
         }
         if (typeof params.close !== "undefined" && params.close !== null) {
           var closeCallback_id = commands_.SHOW_DASHBOARD + "_close";
           params.closeCallback = closeCallback_id;
           proton.app.addCallback(closeCallback_id, params.close);
         }
         return proton.app.execute(commands_.SHOW_DASHBOARD, params);
       },
       showPhoto: function(query) {
         return proton.app.execute(commands_.SHOW_PHOTO, query);
       },
       takePhoto: function(callback, resetCallback) {
         var params = {};
         var callback_id = commands_.TAKE_PHOTO;
         params.callback = callback_id;
         proton.app.addCallback(callback_id, callback);

         if (typeof resetCallback !== "undefined" && resetCallback !== null) {
           var resetCallback_id = commands_.TAKE_PHOTO + "_reset";
           params.resetCallback = resetCallback_id;
           proton.app.addCallback(resetCallback_id, resetCallback);
         }

         return proton.app.execute(commands_.TAKE_PHOTO, params);
       },
       depositProduct: function(params) {
         if (params == null) params = {};
         return proton.app.execute(commands_.DEPOSIT_PRODUCT, params);
       },
       showInviteDialog : function(params, callback) {
         var callback_id;
         if (params == null) params = {};
         callback_id = commands_.SHOW_INVITE_DIALOG;
         params.callback = callback_id;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.SHOW_INVITE_DIALOG, params);
       },
       showDepositProductDialog : function(params, callback) {
         if (params == null) params = {};
         callback_id = commands_.SHOW_DEPOSIT_PRODUCT_DIALOG;
         params.callback = callback_id;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.SHOW_DEPOSIT_PRODUCT_DIALOG, params);
       },
       closeAndLaunchIAPHistoryDialog : function(params, callback) {
         if (params == null) params = {};
         return proton.app.execute(commands_.CLOSE_AND_LAUNCH_IAP_HISTORY_DIALOG, params);
       },
       collateForDeposit : function(params) {
         if (params == null) params = {};
         return proton.app.execute(commands_.COLLATE_FOR_DEPOSIT, params);
       },
       contactForDeposit : function(params) {
         if (params == null) params = {};
         return proton.app.execute(commands_.CONTACT_FOR_DEPOSIT, params);
       },
       noticeLaunchDeposit : function(params) {                                                                     
         if (params == null) params = {};
         return proton.app.execute(commands_.NOTICE_LAUNCH_DEPOSIT, params);
       },
       showDepositHistoryDialog : function(params) {
         if (params == null) params = {};
         return proton.app.execute(commands_.SHOW_DEPOSIT_HISTORY_DIALOG, params);
       },
       inviteExternalUser : function(params) {
         if (params == null) params = {};
         return proton.app.execute(commands_.INVITE_EXTERNAL_USER, params);
       },
       seeMore : function(params, callback) {
         if (params == null) params = {};
         callback_id = commands_.SEE_MORE;
         params.callback = callback_id;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.SEE_MORE, params);
       },
       showWaNavigationBar : function(params) {
         if (params == null) params = {};
         return proton.app.execute(commands_.SHOW_WA_NAVIGATION_BAR, params);
       },
       logout : function(params) {
         if (params == null) params = {};
         return proton.app.execute(commands_.LOGOUT, params);
       },
       showDashboardFromNotificationBoard : function(params) {
         var callback_id;
         if (params == null) params = {};
         return proton.app.execute(commands_.SHOW_DASHBOARD_FROM_NOTIFICATION_BOARD, params);
       },
       launchService : function(params) {
         if (params == null) params = {};
         return proton.app.execute(commands_.LAUNCH_SERVICE, params);
       },
       notifyServiceResult : function(params) {
         if (params == null) params = {};
         return proton.app.execute(commands_.NOTIFY_SERVICE_RESULT, params);
       },
       registerLocalNotificationTimer : function(params, callback) {
         if (params == null) params = {};
         callback_id = commands_.REGISTER_LOCAL_NOTIFICATION_TIMER;
         params.callback = callback_id;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.REGISTER_LOCAL_NOTIFICATION_TIMER, params);
       },
       cancelLocalNotificationTimer : function(params, callback) {
         if (params == null) params = {};
         callback_id = commands_.CANCEL_LOCAL_NOTIFICATION_TIMER;
         params.callback = callback_id;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.CANCEL_LOCAL_NOTIFICATION_TIMER, params);
       },
       getLocalNotificationEnabled : function (callback) {
         var params = {};
         callback_id = commands_.GET_LOCAL_NOTIFICATION_ENABLED;
         params.callback = callback_id;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.GET_LOCAL_NOTIFICATION_ENABLED, params);
       },
       setLocalNotificationEnabled : function (params) {
         if (params == null) params = {};
         return proton.app.execute(commands_.SET_LOCAL_NOTIFICATION_ENABLED, params);
       },
       close : function(params, callback) {
         var callback_id;
         if (params == null) params = {};
         params.callback = callback_id = commands_.CLOSE;
         //proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.CLOSE, params);
       },
       setConfig : function(params, callback) {
         var callback_id;
         if (params == null) params = {};
         params.callback = callback_id = commands_.SET_CONFIG;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.SET_CONFIG, params);
       },
       getConfig : function(params, callback) {
         var callback_id;
         if (params == null) params = {};
         params.callback = callback_id = commands_.GET_CONFIG;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.GET_CONFIG, params);
       },
       getConfigList : function(params, callback) {
         var callback_id;
         if (params == null) params = {};
         params.callback = callback_id = commands_.GET_CONFIG_LIST;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.GET_CONFIG_LIST, params);
       },
       getAppList : function(params, callback) {
           var callback_id;
           if (params == null) params = {};
           params.callback = callback_id = commands_.GET_APP_LIST;
           proton.app.addCallback(callback_id, callback);
           return proton.app.execute(commands_.GET_APP_LIST, params);
       },
       getViewInfo : function(params, callback) {
           var callback_id;
           if (params == null) params = {};
           params.callback = callback_id = commands_.GET_VIEW_INFO;
           proton.app.addCallback(callback_id, callback);
           return proton.app.execute(commands_.GET_VIEW_INFO, params);
       },
       contentsReady : function() {
           proton.app.execute(commands_.CONTENTS_READY);
       },
       deleteCookie : function(params, callback) {
         var callback_id;
         if (params == null) params = {};
         params.callback = callback_id = commands_.DELETE_COOKIE;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.DELETE_COOKIE, params);
       },
       updateUser : function(params, callback) {
         var callback_id;
         if (params == null) params = {};
         params.callback = callback_id = commands_.UPDATE_USER;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.UPDATE_USER, params);
       },
       controlSound : function(params, callback) {
         var callback_id;
         if (params == null) params = {};
         callback_id = commands_.CONTROL_SOUND;
         params.callback = callback_id;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.CONTROL_SOUND, params);
       },
       startLog : function(params, callback) {
         var callback_id;
         if (params == null) params = {};
         params.callback = callback_id = commands_.START_LOG;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.START_LOG, params);
       },
       stopLog : function(params, callback) {
         var callback_id;
         if (params == null) params = {};
         params.callback = callback_id = commands_.STOP_LOG;
         proton.app.addCallback(callback_id, callback);
         return proton.app.execute(commands_.STOP_LOG, params);
       }
     };

     window.use = function(namespace) {
       return eval(namespace);
     };
     window.proton = proton;
   }
 }).call(this);