var exec = require('cordova/exec');

var callShowPlugin={
    add:function(info,success,error){
		exec(success, error, "CallShowPlugin", "add", [info]);
	},
	get:function(success,error){
		exec(success, error, "CallShowPlugin", "get",[]);
	}
}

module.exports = callShowPlugin
