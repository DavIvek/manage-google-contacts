# Contacts Manager for Google

This Java program is designed to manage contacts on Google. It reads a file called "contacts.txt", which should be located in the same folder as the .jar file.
Your application should be named "ContactList". You should have a credentials.json file in the same folder as the .jar file.



### Requirements

This program requires Java to be installed on your system. You can check if Java is installed by running the following command in your terminal:
```bash
java -version
```

If Java is not installed, you can download it from the following link:
https://www.java.com/en/download/

Also, you need to create a Google account and enable the People API. You can find more information on how to do this here:
https://developers.google.com/people

### Usage

To use this program, follow these steps:

1. Ensure that the contacts.txt file is located in the same folder as the .jar file.
2. Open your terminal or command prompt and navigate to the folder containing the .jar file.
3. Run the following command:
```bash
java -jar contacts.jar [argument]
```

Replace [argument] with one of the following options:

* all - first deletes all contacts from the Google account, then creates all contacts from the contacts.txt file
* delete - deletes all contacts from the contacts.txt file
* create - creates all contacts from the contacts.txt file
* duplicates - removes duplicate contacts from the contacts.txt file
* count - counts the number of contacts

If no argument is provided, the program will update the contact list by reading the contacts.txt file.

### File Format

```txt
Name;OIB;Email;phoneNumber;Adress;CompanyName;JobDescription
```

### Logging
Logs are saved in logs.txt file that will bre created in the same folder as the .jar file.
