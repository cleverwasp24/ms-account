package com.nttdata.bootcamp.msaccount.model;

import com.mongodb.lang.NonNull;
import com.mongodb.lang.Nullable;
import com.nttdata.bootcamp.msaccount.model.enums.AccountTypeEnum;
import com.nttdata.bootcamp.msaccount.util.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "transaction")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Transaction {

    @Transient
    public static final String SEQUENCE_NAME = "transaction_sequence";

    @Id
    private Long id;
    @NonNull
    private Long accountId;
    @Nullable
    private Long destinationAccountId;
    @NonNull
    private Integer transactionType;
    @NonNull
    private String description;
    @NonNull
    private Double amount;
    @NonNull
    private Double fee;
    @NonNull
    private Double newBalance;
    @NonNull
    private LocalDateTime transactionDate;

    public void calculateFee(AccountTypeEnum type){
        switch (type){
            case SAVINGS -> this.setFee(Constants.SAVINGS_TRANSACTION_FEE);
            case CURRENT -> this.setFee(Constants.CURRENT_TRANSACTION_FEE);
            case FIXED_TERM_DEPOSIT -> this.setFee(Constants.FIXED_TERM_DEPOSIT_TRANSACTION_FEE);
            case VIP -> this.setFee(Constants.VIP_TRANSACTION_FEE);
            case PYME -> this.setFee(Constants.PYME_TRANSACTION_FEE);
        }
    }

}
