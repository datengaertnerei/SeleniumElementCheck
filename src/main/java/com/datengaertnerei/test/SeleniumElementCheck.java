package com.datengaertnerei.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Starter class to perform Selenium website checks
 * 
 * Can be started standalone with XML config file as command line parameter. 
 * Complies with Nagios plugin conventions
 * Can be integrated in an automated test (JUnit, TestNG)
 * 
 * @author Jens Dibbern
 *
 */
public class SeleniumElementCheck {

	// constants
	private static final String MOBILE_DEVICE_ATTRIBUTE = "mobileDevice";
	private static final String URL_ATTRIBUTE = "url";
	private static final String CHECK_TAG = "check";

	/**
	 * Std main entry
	 * 
	 * @param args	One parameter pointing to the XML config file
	 */
	public static void main(String[] args) {

		// check parameter
		if (args.length != 1 || Files.notExists(Path.of(args[0]))) {
			System.out.println("ERROR: no config file");
			System.exit(3); // Nagios status unknown
		}

		// parse XML config file
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(args[0]));

			// and run the selenium checks
			CheckResultList<CheckResult> allResults = runSeleniumChecks(document);

			if (allResults.size() > 0) {
				if (allResults.getErrorCount() > 0) {
					System.out.println("CRITICAL: there were errors");
					printResults(allResults);
					System.exit(2); // Nagios status critical
				} else {
					System.out.println("WARNING: checks with warnings");
					printResults(allResults);
					System.exit(1); // Nagios status warn
				}
			} else {
				System.out.println("OK: all checks are successful");
				System.exit(0); // Nagios status ok
			}

		} catch (ParserConfigurationException | SAXException | IOException e) {
			System.out.println("ERROR: config problem");
			System.out.println(e.getMessage());
			System.exit(3); // Nagios status unknown
		}
	}

	/**
	 * @param document
	 * @return
	 * @throws DOMException
	 */
	public static CheckResultList<CheckResult> runSeleniumChecks(Document document) throws DOMException {
		// suppress some unnecessary webdriver output
		System.setProperty("webdriver.chrome.args", "--disable-logging");
		System.setProperty("webdriver.chrome.silentOutput", "true");
		
		// Init webdriver manager (will download suitable webdriver binary if necessary)
		WebDriverManager.chromedriver().setup();

		// read config tree
		CheckResultList<CheckResult> allResults = new CheckResultList<>();
		NodeList checks = document.getElementsByTagName(CHECK_TAG);
		for (int i = 0; i < checks.getLength(); i++) {
			Node check = checks.item(i);
			String url = check.getAttributes().getNamedItem(URL_ATTRIBUTE) == null ? null
					: check.getAttributes().getNamedItem(URL_ATTRIBUTE).getTextContent();
			String mobileDevice = check.getAttributes().getNamedItem(MOBILE_DEVICE_ATTRIBUTE) == null ? null
					: check.getAttributes().getNamedItem(MOBILE_DEVICE_ATTRIBUTE).getTextContent();

			NodeList elements = check.getChildNodes();
			if (null != url && 0 != elements.getLength()) {
				// run checks and collect results
				ChromeCheck chromeCheck = new ChromeCheck(url, elements, mobileDevice);
				CheckResultList<CheckResult> checkResults = chromeCheck.runChecks();
				allResults.addAll(checkResults);
			}
		}

		return allResults;
	}

	/**
	 * @param allResults
	 */
	private static void printResults(CheckResultList<CheckResult> allResults) {
		for (CheckResult result : allResults) {
			System.out.print(result.isError() ? "ERROR: " : "WARNING: ");
			System.out.println(result.getSelector());
			System.out.println(result.getMessage());
		}
	}

}
