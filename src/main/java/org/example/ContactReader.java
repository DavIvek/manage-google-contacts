package org.example;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * ContactReader class
 * Reads contacts from file and returns list of contacts.
 * @author Pelion
 * @version 1.0
 */
public class ContactReader {

    private static final Logger logger = LoggerFactory.getLogger(ContactReader.class);

    private String filePath;

    public ContactReader(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Reads contacts from file and returns list of contacts.
     * @return List of contacts
     */

    public List<Contact> readContactsFromFile() {
        List<Contact> contacts = new ArrayList<>();
        File file = new File(filePath);

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] contactData = line.split(";");

                String name = contactData[0];
                String notes = contactData[1];
                String email = contactData[2];
                String phone = contactData[3];
                String address = contactData[4];
                String company = contactData[5];
                String jobDescription = contactData[6];

                Contact contact = new Contact(name, notes, email, phone, address, company, jobDescription);
                contacts.add(contact);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filePath);
            e.printStackTrace();
        }
        logger.info("Contacts read from file: " + contacts.size());
        return contacts;
    }
}
