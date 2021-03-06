package controller;

import org.hl7.fhir.r5.model.ImplementationGuide;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.utilities.TimeTracker;
import org.hl7.fhir.utilities.VersionUtilities;
import org.hl7.fhir.validation.Scanner;
import org.hl7.fhir.validation.ValidationEngine;
import org.hl7.fhir.validation.cli.model.CliContext;
import org.hl7.fhir.validation.cli.services.ComparisonService;
import org.hl7.fhir.validation.cli.services.ValidationService;
import org.hl7.fhir.validation.cli.utils.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class HL7FHIRController {
	public static final String HTTP_PROXY_HOST = "http.proxyHost";
	public static final String HTTP_PROXY_PORT = "http.proxyPort";
	public static final String HTTP_PROXY_USER = "http.proxyUser";
	public static final String HTTP_PROXY_PASS = "http.proxyPassword";
	public static final String JAVA_DISABLED_TUNNELING_SCHEMES = "jdk.http.auth.tunneling.disabledSchemes";
	public static final String JAVA_DISABLED_PROXY_SCHEMES = "jdk.http.auth.proxying.disabledSchemes";
	public static final String JAVA_USE_SYSTEM_PROXIES = "java.net.useSystemProxies";

	/**
	 * 
	 * @param filePath An XML to be validated
	 * @param customProfile A canonical URL contains HL7 FHIR rule
	 * @throws Exception
	 */
	public static void convertAndValidate(String filePath, String fhirProfile) throws Exception {
		String newFilePath = convert(filePath);
		String[] param = { "-version", "4.0.1", "-debug", newFilePath, "-profile", fhirProfile, "-tx", "n/a" };
		validate(param);
	}
	
	public static void convertAndValidate(String filePath) throws Exception {
		String newFilePath = convert(filePath);
		String[] param = { "-version", "4.0.1", "-debug", newFilePath, "-tx", "n/a" };
		validate(param);
	}

	public static String convert(String filePath) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
		DocumentBuilder db = dbf.newDocumentBuilder();

		Document document = db.parse(new FileInputStream(new File(filePath)));

		Element element = (Element) document.getElementsByTagName("Bundle").item(0);

		// whether an attribute with a given name is specified on this element or has a
		// default value
		if (element.hasAttribute("xsi:schemaLocation")) {
			element.removeAttribute("xsi:schemaLocation");
		}

		String newFileName = filePath.split("\\.")[0] + "_FHIR.xml";
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "5");
		DOMSource domSource = new DOMSource(document);
		StreamResult streamResult = new StreamResult(new File(newFileName));

		transformer.transform(domSource, streamResult);
		return newFileName;
	}

	public static void validate(String[] args) throws Exception {
		TimeTracker tt = new TimeTracker();
		TimeTracker.Session tts = tt.start("Loading");

		Display.displayVersion();
		Display.displaySystemInfo();

		if (Params.hasParam(args, Params.PROXY)) {
			assert Params.getParam(args, Params.PROXY) != null : "PROXY arg passed in was NULL";
			String[] p = Params.getParam(args, Params.PROXY).split(":");
			System.setProperty(HTTP_PROXY_HOST, p[0]);
			System.setProperty(HTTP_PROXY_PORT, p[1]);
		}

		if (Params.hasParam(args, Params.PROXY_AUTH)) {
			assert Params.getParam(args, Params.PROXY) != null : "Cannot set PROXY_AUTH without setting PROXY...";
			assert Params.getParam(args, Params.PROXY_AUTH) != null : "PROXY_AUTH arg passed in was NULL...";
			String[] p = Params.getParam(args, Params.PROXY_AUTH).split(":");
			String authUser = p[0];
			String authPass = p[1];

			/*
			 * For authentication, use java.net.Authenticator to set proxy's configuration
			 * and set the system properties http.proxyUser and http.proxyPassword
			 */
			Authenticator.setDefault(new Authenticator() {
				@Override
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(authUser, authPass.toCharArray());
				}
			});

			System.setProperty(HTTP_PROXY_USER, authUser);
			System.setProperty(HTTP_PROXY_PASS, authPass);
			System.setProperty(JAVA_USE_SYSTEM_PROXIES, "true");

			/*
			 * For Java 1.8 and higher you must set
			 * -Djdk.http.auth.tunneling.disabledSchemes= to make proxies with Basic
			 * Authorization working with https along with Authenticator
			 */
			System.setProperty(JAVA_DISABLED_TUNNELING_SCHEMES, "");
			System.setProperty(JAVA_DISABLED_PROXY_SCHEMES, "");
		}

		CliContext cliContext = Params.loadCliContext(args);

		if (Params.hasParam(args, Params.TEST)) {
			Common.runValidationEngineTests();
		} else if (shouldDisplayHelpToUser(args)) {
			Display.displayHelpDetails();
		} else if (Params.hasParam(args, Params.COMPARE)) {
			if (destinationDirectoryValid(Params.getParam(args, Params.DESTINATION))) {
				doLeftRightComparison(args, cliContext, tt);
			}
		} else {
			Display.printCliArgumentsAndInfo(args);
			doValidation(tt, tts, cliContext);
		}
	}

	private static boolean destinationDirectoryValid(String dest) {
		if (dest == null) {
			System.out.println("no -dest parameter provided");
			return false;
		} else if (!new File(dest).isDirectory()) {
			System.out.println("Specified destination (-dest parameter) is not valid: \"" + dest + "\")");
			return false;
		} else {
			System.out.println("Valid destination directory provided: \"" + dest + "\")");
			return true;
		}
	}

	private static boolean shouldDisplayHelpToUser(String[] args) {
		return (args.length == 0 || Params.hasParam(args, Params.HELP) || Params.hasParam(args, "?")
				|| Params.hasParam(args, "-?") || Params.hasParam(args, "/?"));
	}

	private static void doLeftRightComparison(String[] args, CliContext cliContext, TimeTracker tt) throws Exception {
		Display.printCliArgumentsAndInfo(args);
		if (cliContext.getSv() == null) {
			cliContext.setSv(ValidationService.determineVersion(cliContext));
		}
		String v = VersionUtilities.getCurrentVersion(cliContext.getSv());
		String definitions = VersionUtilities.packageForVersion(v) + "#" + v;
		ValidationEngine validator = ValidationService.getValidator(cliContext, definitions, tt);
		ComparisonService.doLeftRightComparison(args, Params.getParam(args, Params.DESTINATION), validator);
	}

	private static void doValidation(TimeTracker tt, TimeTracker.Session tts, CliContext cliContext) throws Exception {
		if (cliContext.getSv() == null) {
			cliContext.setSv(ValidationService.determineVersion(cliContext));
		}
		System.out.println("Loading");
		// Comment this out because definitions filename doesn't necessarily contain
		// version (and many not even be 14 characters long).
		// Version gets spit out a couple of lines later after we've loaded the context
		String definitions = VersionUtilities.packageForVersion(cliContext.getSv()) + "#"
				+ VersionUtilities.getCurrentVersion(cliContext.getSv());
		ValidationEngine validator = ValidationService.getValidator(cliContext, definitions, tt);
		tts.end();
		switch (cliContext.getMode()) {
		case TRANSFORM:
			ValidationService.transform(cliContext, validator);
			break;
		case NARRATIVE:
			ValidationService.generateNarrative(cliContext, validator);
			break;
		case SNAPSHOT:
			ValidationService.generateSnapshot(cliContext, validator);
			break;
		case CONVERT:
			ValidationService.convertSources(cliContext, validator);
			break;
		case FHIRPATH:
			ValidationService.evaluateFhirpath(cliContext, validator);
			break;
		case VERSION:
			ValidationService.transformVersion(cliContext, validator);
			break;
		case VALIDATION:
		case SCAN:
		default:
			for (String s : cliContext.getProfiles()) {
				if (!validator.getContext().hasResource(StructureDefinition.class, s)
						&& !validator.getContext().hasResource(ImplementationGuide.class, s)) {
					System.out.println("  Fetch Profile from " + s);
					validator.loadProfile(cliContext.getLocations().getOrDefault(s, s));
				}
			}
			System.out.println("Validating");
			if (cliContext.getMode() == EngineMode.SCAN) {
				Scanner validationScanner = new Scanner(validator.getContext(), validator.getValidator(),
						validator.getIgLoader(), validator.getFhirPathEngine());
				validationScanner.validateScan(cliContext.getOutput(), cliContext.getSources());
			} else {
				ValidationService.validateSources(cliContext, validator);
			}
			break;
		}
		System.out.println("Done. " + tt.report());

	}

}
