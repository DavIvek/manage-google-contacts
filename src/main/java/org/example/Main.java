package org.example;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import com.google.api.services.people.v1.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

        private static final Logger logger = LoggerFactory.getLogger(Main.class);

        public static void main(String[] args) throws GeneralSecurityException, IOException, InterruptedException {
            ContactsManager contactsManager = new ContactsManager("ContactList",
                    "credentials.json",
                    "contacts.txt");
            if(args.length != 0){
                int counter = 0;
                switch (args[0]) {
                    case "all" -> {
                        List<Contact> contactList = contactsManager.getContactReader().readContactsFromFile();
                        if(contactList == null || contactList.size() == 0){
                            System.out.println("No contacts to update");
                            return;
                        }
                        int created = 0;
                        List<Person> people = contactsManager.listContacts();
                        if(people.size() != 0){
                            System.out.println("Deleted: " + contactsManager.deleteAllContacts(people));
                        }
                        for (Contact contact : contactList) {
                            contactsManager.createContact(contact);
                            counter++;
                            created++;
                            if (counter >= 50){
                                counter = 0;
                                try {
                                    Thread.sleep(100000);
                                } catch (InterruptedException e) {
                                    logger.error(e.getMessage());
                                }
                            }
                        }
                        System.out.println("Created: " + created);
                    }
                    case "create" -> {
                        List<Contact> contactList = contactsManager.getContactReader().readContactsFromFile();
                        if(contactList == null || contactList.size() == 0){
                            System.out.println("No contacts to update");
                            return;
                        }
                        int created = 0;
                        for (Contact contact : contactList) {
                            contactsManager.createContact(contact);
                            counter++;
                            created++;
                            if (counter >= 50){
                                counter = 0;
                                try {
                                    Thread.sleep(100000);
                                } catch (InterruptedException e) {
                                    logger.error(e.getMessage());
                                }
                            }
                        }
                        System.out.println("Created: " + created);
                    }
                    case "delete" -> {
                        List<Contact> contactList = contactsManager.getContactReader().readContactsFromFile();
                        if(contactList == null || contactList.size() == 0){
                            System.out.println("No contacts to update");
                            return;
                        }
                        List<Person> people = contactsManager.listContacts();
                        if(people.size() == 0)
                            System.out.println("No contacts to delete");
                        else
                            System.out.println("Deleted: " + contactsManager.deleteContacts(contactList, people));
                    }
                    case "count"-> System.out.println("Number of contacts: " + contactsManager.listContacts().size());
                    case "duplicates" -> System.out.println("Successfully removed: " + contactsManager.removeDuplicates() + " duplicates");
                }
            }
            else{
                List<Contact> contactList = contactsManager.getContactReader().readContactsFromFile();
                if(contactList == null || contactList.size() == 0){
                    System.out.println("No contacts to update");
                    return;
                }
                System.out.println("Updated :" + contactsManager.updateContactList(contactList));
            }
        }
}
