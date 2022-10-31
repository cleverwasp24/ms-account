package com.nttdata.bootcamp.msaccount.model;

import com.mongodb.lang.NonNull;
import com.mongodb.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "account")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Log4j2
public class Account {

    @Transient
    public static final String SEQUENCE_NAME = "account_sequence";

    @Id
    private Long id;
    @NonNull
    private Long clientId;
    @NonNull
    private Integer accountType;
    @NonNull
    @Indexed(unique = true)
    private String accountNumber;
    @NonNull
    private Double balance;
    @Nullable
    private Integer maxFreeTransactions;
    @Nullable
    private Double maintenanceFee;
    @Nullable
    private Double transactionFee;
    @NonNull
    private LocalDateTime openingDate;
    @Nullable
    private LocalDateTime transactionDay;
    @Nullable
    private List<Owner> owners;
    @Nullable
    private List<AuthorizedSigner> signers;

}
