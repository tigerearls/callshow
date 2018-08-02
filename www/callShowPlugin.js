var exec = require('cordova/exec');

var callShowPlugin={
    add:function(info,success,error){
		exec(success, error, "CallShowPlugin", "add", [info]);
	}
}

module.exports = callShowPlugin
