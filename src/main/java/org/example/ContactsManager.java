package org.example;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.PeopleServiceScopes;
import com.google.api.services.people.v1.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class for managing contacts in Google Contacts
 * @author Pelion
 * @version 1.0
 */

public class ContactsManager {
    private final String applicationName;
    private final File credentialsFilePath;
    private final PeopleService service;
    private static final List<String> SCOPES = List.of(PeopleServiceScopes.CONTACTS);
    public static String TOKENS_DIRECTORY_PATH = "tokens";
    public static JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final ContactReader contactReader;
    private static final Logger logger = LoggerFactory.getLogger(ContactsManager.class);


    public ContactsManager(String applicationName, String credentialsFilePath, String filePath) throws GeneralSecurityException, IOException {
        this.applicationName = applicationName;
        this.credentialsFilePath = new File(credentialsFilePath);
        this.service = createPeopleService();
        this.contactReader = new ContactReader(filePath);
    }

    private HttpRequestInitializer setHttpTimeout(final HttpRequestInitializer requestInitializer) {
        return httpRequest -> {
            requestInitializer.initialize(httpRequest);
            httpRequest.setConnectTimeout(3 * 60000);  // 3 minutes connect timeout
            httpRequest.setReadTimeout(3 * 60000);  // 3 minutes read timeout
        };
    }

    /**
     * @return the credentialsFilePath
     */
    public File getCredentialsFilePath() {
        return credentialsFilePath;
    }

    /**
     * @return the applicationName
     */
    public String getApplicationName() {
        return applicationName;
    }

    public ContactReader getContactReader() {
        return contactReader;
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    public Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = new FileInputStream(credentialsFilePath);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Build and return an authorized People client service.
     *
     * @return an authorized People client service
     * @throws GeneralSecurityException, IOException
     */
    public PeopleService createPeopleService() throws GeneralSecurityException, IOException{
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new PeopleService.Builder(HTTP_TRANSPORT, JSON_FACTORY, setHttpTimeout(getCredentials(HTTP_TRANSPORT)))
                .setApplicationName(applicationName)
                .build();
    }

    /**
     * Creates a new contact.
     *
     * @return true if new contact is created, else return false.
     */
    public boolean createContact(Contact contact) throws IOException, InterruptedException {
        Person contactToCreate = new Person();
        List<Name> names = new ArrayList<>();
        names.add(new Name().setGivenName(contact.getName()));
        contactToCreate.setNames(names);
        if(contact.getPhone() != null) {
            List<PhoneNumber> phoneNumbers = new ArrayList<>();
            phoneNumbers.add(new PhoneNumber().setValue(contact.getPhone()));
            contactToCreate.setPhoneNumbers(phoneNumbers);
        }
        if(contact.getEmail() != null) {
            List<EmailAddress> emailAddresses = new ArrayList<>();
            emailAddresses.add(new EmailAddress().setValue(contact.getEmail()));
            contactToCreate.setEmailAddresses(emailAddresses);
        }
        List<Organization> organizations = new ArrayList<>();
        List<Address> addresses = new ArrayList<>();
        organizations.add(new Organization().setName(contact.getCompanyName()).setTitle(contact.getJobDescription()));
        addresses.add(new Address().setStreetAddress(contact.getAddress()));
        contactToCreate.setOrganizations(organizations);
        contactToCreate.setAddresses(addresses);
        List<Biography> biographies = new ArrayList<>();
        biographies.add(new Biography().setValue(contact.getNotes()));
        contactToCreate.setBiographies(biographies);
        Person createdContact;
        try {
            createdContact = service.people().createContact(contactToCreate).execute();
        } catch (Exception e) {
            logger.error(e.getMessage());
            Thread.sleep(100000);
            try {
                createdContact = service.people().createContact(contactToCreate).execute();
            } catch (Exception ex) {
                logger.error(ex.getMessage());
                throw ex;
            }
        }
        logger.info("Contact created: "
                + contact.getName() + " "
                + contact.getPhone() + " "
                + contact.getEmail());
        return createdContact != null;
    }

    /**
     * Deletes a contact.
     *
     * @return number of deleted contacts.
     */

    public int deleteContacts(List<Contact> contacts, List<Person> connections) throws IOException, InterruptedException {
        int deletedContacts = 0;
        int counter = 0;
        for (Contact contact : contacts) {
            for (Person connection : connections) {
                if (connection.getNames() != null && connection.getNames().size() > 0) {
                    Name name = connection.getNames().get(0);
                    Contact contactToDelete = new Contact("", "", "", "");
                    if (name.getGivenName() != null) {
                        contactToDelete.setName(name.getGivenName());
                    }
                    if (connection.getPhoneNumbers() != null && connection.getPhoneNumbers().size() > 0) {
                        PhoneNumber phoneNumber = connection.getPhoneNumbers().get(0);
                        contactToDelete.setPhone(phoneNumber.getValue());
                    }
                    if (connection.getEmailAddresses() != null && connection.getEmailAddresses().size() > 0) {
                        EmailAddress emailAddress = connection.getEmailAddresses().get(0);
                        contactToDelete.setEmail(emailAddress.getValue());
                    }
                    if(connection.getOrganizations() != null){
                        Organization organization = connection.getOrganizations().get(0);
                        contactToDelete.setCompanyName(organization.getName());
                        contactToDelete.setJobDescription(organization.getTitle());
                    }
                    if (contact.equals(contactToDelete)) {
                        try {
                            service.people().deleteContact(connection.getResourceName()).execute();
                        } catch (Exception e) {
                            logger.error("Contact not deleted: " + contactToDelete.getName() + " "
                                    + contactToDelete.getPhone() + " "
                                    + contactToDelete.getEmail() + " " + e.getMessage());
                            try {
                                Thread.sleep(100000);
                                service.people().deleteContact(connection.getResourceName()).execute();
                            } catch (Exception ex) {
                                logger.error(ex.getMessage());
                                throw ex;
                            }
                        }
                        logger.info("Contact deleted: " + contactToDelete.getName() + " "
                                + contactToDelete.getPhone() + " "
                                + contactToDelete.getEmail());
                        counter++;
                        if (counter >= 50){
                            counter = 0;
                            try {
                                Thread.sleep(100000);
                            } catch (InterruptedException e) {
                                logger.error(e.getMessage());
                            }
                        }
                        deletedContacts++;
                    }
                }
            }
        }
        return deletedContacts;
    }


    /**
     * Converts Person object to Contact object.
     *
     * @return list of contacts as Contact object.
     */
    private List<Contact> convertPersonToContact(List<Person> connections){
        List<Contact> contacts = new ArrayList<>();
        for (Person connection : connections) {
            if (connection.getNames() != null && connection.getNames().size() > 0) {
                Name name = connection.getNames().get(0);
                Contact contact = new Contact("", "", "", "");
                if(name.getGivenName() != null) {
                    contact.setName(name.getGivenName());
                }
                if(connection.getPhoneNumbers() != null && connection.getPhoneNumbers().size() > 0) {
                    PhoneNumber phoneNumber = connection.getPhoneNumbers().get(0);
                    contact.setPhone(phoneNumber.getValue());
                }
                if(connection.getEmailAddresses() != null && connection.getEmailAddresses().size() > 0) {
                    EmailAddress emailAddress = connection.getEmailAddresses().get(0);
                    contact.setEmail(emailAddress.getValue());
                }
                if(connection.getOrganizations() != null) {
                    Organization organization = connection.getOrganizations().get(0);
                    if(organization.getName() != null) {
                        contact.setCompanyName(organization.getName());
                    }
                    if(organization.getTitle() != null) {
                        contact.setJobDescription(organization.getTitle());
                    }
                }
                contacts.add(contact);
            }
        }
        return contacts;
    }

    /**
     * Updates contact list.
     * @return number of updated contacts.
     */
    public int updateContactList(List<Contact> contacts) throws IOException, InterruptedException {
        int createdContacts = 0;
        int counter = 0;
        List<Person> people = listContacts();
        List<Contact> contactsFromGoogle = convertPersonToContact(people);
        for (Contact contact : contacts) {
            if (!contactsFromGoogle.contains(contact)) {
                if (createContact(contact)) {
                    createdContacts++;
                    counter++;
                    if(counter >= 50){
                        try {
                            Thread.sleep(100000); // 100 seconds
                            counter = 0;
                        } catch (InterruptedException e) {
                            // Handle the exception if sleep is interrupted
                            logger.error(e.getMessage());
                        }
                    }
                }
            }
        }
        List<Contact> contactsToDelete = new ArrayList<>();
        for(Contact contact : contactsFromGoogle) {
            if (!contacts.contains(contact)) {
                contactsToDelete.add(contact);
            }
        }
        if(contactsToDelete.size() > 0){
            logger.info("Deleting contacts..." + contactsToDelete.size() + " contacts to delete.");
            deleteContacts(contactsToDelete, people);
        }
        logger.info("Contacts updated: " + createdContacts + " contacts created, " + contactsToDelete.size() + " contacts deleted.");
        return createdContacts + contactsToDelete.size();
    }


    /**
     * Lists all contacts.
     * @return list of contacts as Person object.
     */
    public List<Person> listContacts() throws IOException, InterruptedException {
        // Initial request
        ListConnectionsResponse response = service.people().connections().list("people/me")
                .setPersonFields("phoneNumbers,names,emailAddresses,organizations")
                .setPageSize(2000)
                .execute();
        while (response.getNextPageToken() != null) {
            Thread.sleep(100000);
            // Fetch all the pages
            ListConnectionsResponse response1 = service.people().connections().list("people/me")
                    .setPersonFields("phoneNumbers,names,emailAddresses,organizations")
                    .setPageToken(response.getNextPageToken())
                    .setPageSize(2000)
                    .execute();
            response.getConnections().addAll(response1.getConnections());
            response.setNextPageToken(response1.getNextPageToken());
        }
        List<Person> personList = response.getConnections();
        Set<Person> personsWithoutDuplicates = new HashSet<>();
        Set<String> set = new HashSet<>();
        if(personList == null){
            return new ArrayList<>();
        }
        for(Person person: personList){
            if(set.add(person.getResourceName())){
                personsWithoutDuplicates.add(person);
            }
        }
        return new ArrayList<>(personsWithoutDuplicates);
    }


    /**
     * Removes duplicates from contact list.
     * @return number of removed duplicates.
     */
    public int removeDuplicates() throws IOException, InterruptedException {
        List<Person> people = listContacts();
        List<Contact> contacts = convertPersonToContact(people);
        Set<Contact> set = new HashSet<>();
        List<Contact> duplicates = new ArrayList<>();
        for (Contact contact : contacts) {
            if (!set.add(contact)) {
                duplicates.add(contact);
                logger.info("Duplicate found: "
                        + contact.getName() + " "
                        + contact.getPhone() + " "
                        + contact.getEmail());
            }
        }
        System.out.println("Duplicates found: " + duplicates.size());
        deleteContacts(duplicates, people);
        logger.info("Duplicates removed: " + duplicates.size());
        return duplicates.size();
    }

    /**
     * Deletes all contacts.
     * @return number of deleted contacts.
     */
    public int deleteAllContacts(List<Person> people) throws InterruptedException, IOException {
        int deletedContacts = 0;
        for(Person person : people){
            try {
                service.people().deleteContact(person.getResourceName()).execute();
            } catch (IOException e) {
                logger.error(e.getMessage());
                Thread.sleep(100000);
                try {
                    service.people().deleteContact(person.getResourceName()).execute();
                } catch (IOException ex) {
                    logger.error(ex.getMessage());
                    throw ex;
                }
            }
            Name name;
            if(person.getNames() != null && person.getNames().size() > 0){
                name = person.getNames().get(0);
            }
            else{
                name = new Name();
                name.setGivenName("");
            }
            PhoneNumber phoneNumber;
            if(person.getPhoneNumbers() != null && person.getPhoneNumbers().size() > 0){
                phoneNumber = person.getPhoneNumbers().get(0);
            }
            else{
                phoneNumber = new PhoneNumber();
                phoneNumber.setValue("");
            }
            EmailAddress emailAddress;
            if(person.getEmailAddresses() != null && person.getEmailAddresses().size() > 0){
                emailAddress = person.getEmailAddresses().get(0);
            }
            else{
                emailAddress = new EmailAddress();
                emailAddress.setValue("");
            }
            logger.info("Contact deleted: "
                    + name.getGivenName() + " "
                    + phoneNumber.getValue() + " "
                    + emailAddress.getValue());
            deletedContacts++;
        }
        return deletedContacts;
    }
}
