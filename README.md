# SeleniumElementCheck 

This is a small Selenium based tool to perform website checks for testing and monitoring. It can be used as a Nagios plugin.

It uses an XML configuration to define the elements to check as URL with a list of CSS selectors:

```
<?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
<checks>
	<check url="http://www.automationpractice.com/">
		<element selector="#header_logo > a > img" level="warn" />
		<element selector="#social_block > ul > li.google-plus > a"
			level="error" />
	</check>
	<check url="http://www.automationpractice.com/"
		mobileDevice="Pixel 2">
		<element selector="#header_logo > a > img" level="warn" />
		<element selector="#social_block > ul > li.google-plus > a"
			level="error" />
	</check>
</checks>
```

## Nagios plugin configuration

For Ubuntu 18 you copy the jar file to /usr/lib/nagios/plugins/ and place your configuration in /etc/nagios-plugins/config/ and add a new command to commands.cfg:

```
# define selenium check command
define command{
        command_name    check-selenium
        command_line    /usr/bin/java -jar /usr/lib/nagios/plugins/SeleniumCheck.jar /etc/nagios-plugins/config/selenium-checks.xml 2>/var/log/nagios3/selenium.log
        }
```

You can now use that command in any new service definition.

## PhantomJS instead of Chrome

Selenium 4 dropped PhantomJS support so this tool uses Chrome (widely available and standard browser for a majority of people). 
If you switch back to the latest stable Selenium 3 release and change a few lines, you can use PhantomJS as Chrome replacement. But you will lose the mobile device emulation option. 

```
		WebDriverManager.phantomjs().setup();
```

```
		DesiredCapabilities capabilities = new DesiredCapabilities();
	    capabilities.setJavascriptEnabled(true);
	    capabilities.setCapability("takesScreenshot", false);
	    capabilities.setCapability(
	        PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
	        WebDriverManager.phantomjs().getBinaryPath()
	    );		
		internalDriver = new PhantomJSDriver(capabilities); 
```