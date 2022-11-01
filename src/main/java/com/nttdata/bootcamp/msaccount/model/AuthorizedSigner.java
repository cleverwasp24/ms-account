package com.nttdata.bootcamp.msaccount.model;

import com.mongodb.lang.NonNull;
import lombok.Data;

@Data
public class AuthorizedSigner {

    @NonNull
    private String docType;
    @NonNull
    private String docNumber;
    @NonNull
    private String firstName;
    @NonNull
    private String lastName;

}
