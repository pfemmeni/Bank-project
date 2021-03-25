package se.sensera.banking.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import se.sensera.banking.User;

@Data //DTO pattern
@AllArgsConstructor
public class UserImpl implements User {
    private final String id;
    private String name;
    private String personalIdentificationNumber;
    private boolean active;


}
