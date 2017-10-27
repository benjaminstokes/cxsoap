package com.checkmarx.api.demo;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

@SpringBootApplication
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static final String HOST = "http://cxlocal";

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	@Profile("!test")
	CommandLineRunner lookup(CxPortalClient portal) {
		return args -> {

			if (args.length != 3) {
				printUsage();
				logArgs(args);
				System.exit(1);
			}

			final String host = args[0];
			final String admin = args[1];
			final String password = args[2];

			log.info("cxsoap: host={}; admin={}", host, admin);

			try {

				System.out.print("\nLogging into CxManager...");
				final CxPortalUtils utils = CxPortalUtils.factory(portal, host, admin, password);
				System.out.println("success!\n");

				System.out.print("Retrieving Cx users...");
				final List<CxUser> users = utils.findAllUsers();
				System.out.println("success!\n");

				final int count = users.size();

				if (count > 0) {
					log.info("{} users found:", count);
					System.out.println("\t" + count + " users found:\n");
					users.forEach(user -> {
						log.info("{}", user.toString(true));
						System.out.println("\t\t" + user.toString());
					});
					saveCSV(users);
				} else {
					System.out.println("\tNo users found.");
					log.info("No users found.");
				}
				
			} catch (Throwable t) {
				System.out.println("FAILED!\n");
				log.error("Error: " + t.getMessage(), t);
			}
			System.out.println();
		};
	}

	private void saveCSV(List<CxUser> users) {
		log.debug("saveCSV()");
		
		final String CSV_FILE = "users.csv";

		System.out.print("\n\tSaving users to csv file: " + CSV_FILE + "...");
		final StringBuilder sb = new StringBuilder();
		sb.append(CxUser.csvHeader() + "\n");
		users.forEach(user -> sb.append(user.toCsv() + "\n"));
		final File file = new File(CSV_FILE);
		try {
			Files.asCharSink(file, Charsets.UTF_8).write(sb.toString());
			System.out.println("success!\n");
			log.debug("Users saved to file: " + CSV_FILE);
		} catch (IOException e) {
			System.out.println("FAILED!\n");
			log.error("Unable to write users to file", e);
		}
	}

	private void logArgs(String[] args) {
		log.debug("Args: count={}", args.length);
		int i = 0;
		for (String arg : args) {
			log.debug("\t{} : {}", i, arg);
			i++;
		}
	}

	private void printUsage() {
		System.out.println("");
		System.out.println("usage: cxsoap <url> <username> <password>");
		System.out.println("");
		System.out.println("  url      : CxManager url, e.g. http://checkmarx.corp.com");
		System.out.println("  username : system manager username, e.g. admin@cx ");
		System.out.println("  password : system manager password ");
		System.out.println("");
		System.out.println("desc: Dumps all Cx users along with their permissions to users.csv file");
		System.out.println("");
	}
}
