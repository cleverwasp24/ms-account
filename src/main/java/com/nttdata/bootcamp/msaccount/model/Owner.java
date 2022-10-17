package com.nttdata.bootcamp.msaccount.model;

import com.mongodb.lang.NonNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
public class Owner {

    @Id
    @Indexed(unique = true)
    private Integer id;
    @NonNull
    private String docType;
    @NonNull
    private String docNumber;
    @NonNull
    private String firstName;
    @NonNull
    private String lastName;

}
