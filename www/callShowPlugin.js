var exec = require('cordova/exec');

var callShowPlugin={
    add:function(info,success,error){
		exec(success, error, "CallShowPlugin", "add", [info]);
	},
	get:function(success,error){
		exec(success, error, "CallShowPlugin", "get",[]);
	},
	reqWindowPerms:function(packageName,success,error){
		exec(success, error, "CallShowPlugin", "ACTION_MANAGE_OVERLAY_PERMISSION",[packageName]);
	},
	checkWindowPerms:function(success,error){
		exec(success, error, "CallShowPlugin", "CHECK_SYSTEM_ALERT_WINDOW",[]);
	}
}

module.exports = callShowPlugin
