package com.nttdata.bootcamp.msaccount.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum AccountTypeEnum {

    SAVINGS(0),
    CURRENT(1),
    FIXED_TERM_DEPOSIT(2),
    VIP(3),
    PYME(4);

    private int value;
    private static Map map = new HashMap();

    private AccountTypeEnum(int value) {
        this.value = value;
    }

    static {
        for (AccountTypeEnum accountType : AccountTypeEnum.values()) {
            map.put(accountType.value, accountType);
        }
    }

    public static AccountTypeEnum valueOf(int accountType) {
        return (AccountTypeEnum) map.get(accountType);
    }

    public int getValue() {
        return value;
    }


}
