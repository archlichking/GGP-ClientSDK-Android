class JskitTest
	constructor: (@protonApp, @doc) ->

	appendTextNode: (text) ->
		e = @doc.createTextNode text
		hr = @doc.createElement 'hr'
		@doc.getElementById('resultText').appendChild(e)
		@doc.getElementById('resultText').appendChild(hr) 

	executeSuite: (suite) ->
		for func, params of suite
			@functionCall func, params, "this.appendTextNode('"+func+" test done')"

	executePopupSuite: (suite) ->
		for func, params of suite
      		str = "{'key':'popupLoaded', 'value':'true'}"
      		callback = "this.appendTextNode('"+func+" test done')"
			@functionCall func, params, "this.protonApp.setConfig("+str+", "+callback+")"

	functionCall: (name, params, callback) ->
		str = "this.protonApp.name(mParams, mCallback)"
		str = str.replace 'name', name
		if params is "{}"
		# args: params
			params.callback = callback
			str = str.replace 'mParams, mCallback', params
		else if name is "pushViewWithURL" or name is "openExternalView"
		# args: url, params
			str = str.replace 'mParams,', params + ','
			str = str.replace 'mCallback', null
		else
			if params is ""
			# args: callback
				str = str.replace 'mParams,', ''
			else
			# args: params, callback
				console.log 'enter here'
				str = str.replace 'mParams,', params + ','
			str = str.replace 'mCallback', callback
		console.log 'calling: ' + str
		eval(str)

	invokeAllNonUITest: () ->
		nonUISuite = 
			'getConfig': "{'key':'jsKitTest'}",
			'setValue': "{'key':'jsKitTest', 'value':'lay.zhu'}",
			'getValue': "{'key':'jsKitTest'}",
			'getConfigList': "null",
			'getAppList': "{'schemes': ['greeapp12345', 'greeapp54321']}",
			'getViewInfo': "null",
			'startLog': "{'loglevel':'100'}",
			'stopLog': "{'loglevel':'100'}",
			'deleteCookie': "{'key':'baidu'}",
			'setLocalNotificationEnabled': "{'enabled':'true'}",
			'recordAnayticsData': "{'tp':'pg','pr':{'key_1':'val_1'},'fr':'yyy'}",
			'getContactList': "",
			'getLocalNotificationEnabled': "",
			'flushAnalyticsQueue': "",
			'flushAnalyticsData': "",
			'collateForDeposit':"{}",
			'contactForDeposit':"{'id':'101'}",
			'noticeLaunchDeposit':"{}",
			'pushViewWithURL':"'http://www.baidu.com'",
			'openExternalView':"'http://www.baidu.com'",
			'showMessageDialog':"{'buttons':['OK','Cancel'],'title':'ok cancel dialog','message':'this is message','cancel_index':1}",
			'needUpdate':"{}",
			'updateUser':"null",
			'registerLocalNotificationTimer': "{'callbackParam':'PARAMS','notifyId':'1','barMessage':'this is bar message','interval':'600','message':'Local Notification Timer fired!!','title':'jskit test title'}",
			'cancelLocalNotificationTimer':"{'notifyId':'1'}",
			'setConfig': "{'key':'jskitTestDone', 'value':'true'}"

		console.log JSON.stringify(nonUISuite)
		@executeSuite nonUISuite

	invokePopupTest: () ->
		popupSuite = 
			'showDashboardFromNotificationBoard':"",
			'setConfig': "{'key':'jskitTestDone', 'value':'true'}"

		console.log JSON.stringify(popupSuite)
		@executeSuite popupSuite

	invokeRequestPopup: () ->
		popupSuite = 
			'showRequestDialog':"{'request':{'title':'request test','body':'request body'}}",

		console.log JSON.stringify(popupSuite)
		@executePopupSuite popupSuite

	invokeSharePopup: () ->
		popupSuite = 
			'showShareDialog':"{'type':'normal', 'message':'normal dialog'}",

		console.log JSON.stringify(popupSuite)
		@executePopupSuite popupSuite

	invokeInvitePopup: () ->
		popupSuite = 
			'showInviteDialog': "{'invite':{'body':'this is js invite'}}",

		console.log JSON.stringify(popupSuite)
		@executePopupSuite popupSuite

	invokeWebviewDialog: () ->
		popupSuite = 
			'showWebViewDialog':"{'URL':'http://www.baidu.com','size':[50, 50]}",

		console.log JSON.stringify(popupSuite)
		@executePopupSuite popupSuite

	invokeDepositProductDialog: () ->
		popupSuite = 
			'showDepositProductDialog': "",

		console.log JSON.stringify(popupSuite)
		@executePopupSuite popupSuite

	invokeDepositHistoryDialog: ()->
		popupSuite = 
			'showDepositHistoryDialog': "",

		console.log JSON.stringify(popupSuite)
		@executePopupSuite popupSuite

	invokeNeedUpgrade: ()->
		popupSuite = 
			'needUpgrade':"{'target_grade':'2'}",
			'setConfig': "{'key':'jskitTestDone', 'value':'true'}"

		console.log JSON.stringify(popupSuite)
		@executeSuite popupSuite

	invokeIAPHistoryDialog: () ->
		popupSuite = 
			'closeAndLaunchIAPHistoryDialog': "",
			'setConfig': "{'key':'jskitTestDone', 'value':'true'}"

		console.log JSON.stringify(popupSuite)
		@executeSuite popupSuite

	closePopup: () ->
		popupSuite = 
			'closePopup': ""

		console.log JSON.stringify(popupSuite)
		@executeSuite popupSuite

	inviteExternalUser: ()->
		viewSuite = 
			'inviteExternalUser':"{'URL':'http://www.baidu.com/'}",
		console.log JSON.stringify(viewSuite)
		@executePopupSuite viewSuite

	showActionSheet: ()->
		viewSuite = 
			'showActionSheetL':"{ 'title' : 'Alert',
                               'buttons': ['OK', 'Do Nothing', 'Destroy All Buttons!', 'Cancel'],
                               'cancel_index' : 2,
                               'destructive_index' : 3}",
			'setConfig': "{'key':'jskitTestDone', 'value':'true'}"
		console.log JSON.stringify(viewSuite)
		@executeSuite viewSuite

	showAlertView: ()->
		viewSuite = 
			'showAlertView':"{ 'title' : 'Alert',
                             'message' : 'This is a message',
                             'buttons': ['OK', 'Do Nothing', 'Cancel'],
                             'cancel_index' : 0}",
		console.log JSON.stringify(viewSuite)
		@executeSuite viewSuite

	showDashboard: ()->
		viewSuite = 
			'showDashboard':"{
                           'URL':'http://www.google.com'
                           }",
		console.log JSON.stringify(viewSuite)
		@executePopupSuite viewSuite

jskit = new JskitTest(proton.app, window.document)
window.jskit ? window.jskit = jskit
