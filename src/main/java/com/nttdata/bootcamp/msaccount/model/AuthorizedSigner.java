package com.nttdata.bootcamp.msaccount.model;

import com.mongodb.lang.NonNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
public class AuthorizedSigner {

    @Id
    private Integer id;
    @NonNull
    private String docType;
    @NonNull
    @Indexed(unique = true)
    private String docNumber;
    @NonNull
    private String firstName;
    @NonNull
    private String lastName;

}
