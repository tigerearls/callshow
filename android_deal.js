#!/usr/bin/env node
 
module.exports = function (context) {
    var path         = context.requireCordovaModule('path'),
        fs           = context.requireCordovaModule('fs'),
        shell        = context.requireCordovaModule('shelljs'),
        projectRoot  = context.opts.projectRoot,
        ConfigParser = context.requireCordovaModule('cordova-common').ConfigParser,
        config       = new ConfigParser(path.join(context.opts.projectRoot, "config.xml")),
        packageName = config.android_packageName() || config.packageName();
 
    if (!packageName) {
        console.error("Package name could not be found!");
        return ;
    }
 
    if (context.opts.cordova.platforms.indexOf("android") === -1) {
        console.info("Android platform has not been added.");
        return ;
    }
 
    var targetDir  = path.join(projectRoot, "platforms", "android", "app","src","main","java","com", "tongdatech", "callshow");
	var targetFiles = ["PhoneReceiver.java"];
	
    if (['after_plugin_add', 'after_plugin_install', 'after_platform_add'].indexOf(context.hook) === -1) {
        try {
			if(context.opts.plugins && context.opts.plugins.indexOf(context.opts.plugin.id) !== -1){
				targetFiles.forEach(function(file){
					var targetFile = path.join(targetDir, file);
					fs.unlinkSync(targetFile);
				});
			}
        } catch (err) {}
    } else {
		targetFiles.forEach(function(file){
			var targetFile = path.join(targetDir, file);
			fs.readFile(targetFile, {encoding: 'utf-8'}, function (err, data) {
				if (err) {
					throw err;
				}
				data = data.replace(/^import .*\.R;/m, 'import ' + packageName + '.R;');
				fs.writeFileSync(targetFile, data);
			});
		});
    }
};