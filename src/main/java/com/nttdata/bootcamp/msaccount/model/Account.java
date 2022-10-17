package com.nttdata.bootcamp.msaccount.model;

import com.mongodb.lang.NonNull;
import com.mongodb.lang.Nullable;
import com.nttdata.bootcamp.msaccount.model.enums.AccountTypeEnum;
import com.nttdata.bootcamp.msaccount.model.enums.TransactionTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.annotation.Id;
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

    public boolean makeTransaction(Transaction transaction) {
        if (transaction.getAmount() < 0) {
            log.info("Transaction amount must be greater than 0");
            return false;
        } else {
            switch (TransactionTypeEnum.valueOf(transaction.getTransactionType())) {
                case DEPOSIT:
                    this.balance = this.balance + transaction.getAmount();
                    break;
                case WITHDRAW:
                    if (this.balance - transaction.getAmount() >= 0){
                        this.balance = this.balance - transaction.getAmount();
                    }else{
                        log.info("Insufficient balance");
                        return false;
                    }
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

}
