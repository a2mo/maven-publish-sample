package ir.a2mo.lib.maven.publish.sample.impl;

import ir.a2mo.lib.maven.publish.sample.api.model.Person;

public class Employee implements Person {
    private String firstName;
    private String lastName;
    private String ssn;
    private int age;

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getSsn() {
        return ssn;
    }

    @Override
    public int getAge() {
        return age;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", ssn='" + ssn + '\'' +
                ", age=" + age +
                '}';
    }
}
