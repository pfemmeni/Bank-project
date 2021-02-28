package se.sensera.banking;

public interface User {
    String getId();

    String getName();

    void setName(String name);

    String getPersonalIdentificationNumber();

    void setPersonalIdentificationNumber(String pid);

    boolean isActive();

    void setActive(boolean active);
}
