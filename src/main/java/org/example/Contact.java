package org.example;


import java.util.Objects;

/**
 * Contact class
 * Contact can be created with name, notes, email, phone number, address, company name and job description.
 * Name, email, phone and job description must be set, other fields are optional.
 * @author Pelion
 * @version 1.0
 */

public class Contact {
    private String name;
    private String notes;
    private String email;
    private String phone;
    private String address;
    private String companyName;
    private String jobDescription;

    public Contact(String name, String notes, String email, String phone, String address, String companyName, String jobDescription) {
        this.name = name;
        this.notes = notes;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.companyName = companyName;
        this.jobDescription = jobDescription;
    }

    public Contact(String name, String email, String phone, String jobDescription){
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.jobDescription = jobDescription;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    private boolean checkPhoneNumbers(String phoneNumber1, String phoneNumber2) {
        if(phoneNumber1 == null || phoneNumber2 == null) {
            return false;
        }
        phoneNumber1 = phoneNumber1.replaceAll("-","");
        phoneNumber2 = phoneNumber2.replaceAll("-","");
        phoneNumber1 = phoneNumber1.replaceAll(" ","");
        phoneNumber2 = phoneNumber2.replaceAll(" ","");
        phoneNumber1 = phoneNumber1.replace("+385","0");
        phoneNumber2 = phoneNumber2.replace("+385","0");
        return phoneNumber1.equals(phoneNumber2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return Objects.equals(name, contact.name)
                && Objects.equals(email, contact.email)
                && checkPhoneNumbers(phone, contact.phone)
                && Objects.equals(jobDescription, contact.jobDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email, phone, jobDescription);
    }

    @Override
    public String toString() {
        return "Contact : name = " + name +
                ", email = " + email +
                ", phone = " + phone;
    }
}
