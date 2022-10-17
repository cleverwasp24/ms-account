package com.nttdata.bootcamp.msaccount.model;

import com.mongodb.lang.NonNull;
import com.mongodb.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "account")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Account {

    @Id
    @Indexed(unique = true)
    private Integer id;
    @NonNull
    private Integer clientId;
    @NonNull
    private Integer accountType; // 0 - CTA AHORROS, 1 - CTA CORRIENTE, 2 - CTA PLAZO FIJO
    @NonNull
    @Indexed(unique = true)
    private String accountNumber;
    @NonNull
    private Double balance;
    @Nullable
    private Integer maxTransactions;
    @Nullable
    private Double maintenanceFee;
    @NonNull
    private LocalDateTime openingDate;
    @Nullable
    private LocalDateTime transactionDay;
    @Nullable
    private List<Owner> owners;
    @Nullable
    private List<AuthorizedSigner> signers;

}
