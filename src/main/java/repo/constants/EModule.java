package repo.constants;

import javax.annotation.Nullable;
import javax.validation.constraints.Null;

public enum EModule {
    
    CORE("core"),

    COREUI("coreui"),

    ORDER("order"),

    PAYMENT("payment"),

    CHECK("check"),

    USER("user"),

    STORE("store"),

    REVIEW("review"),

    HISTORY("history");

    private String value;

    private EModule(String value) {
        this.value = value;
    }

    @Nullable
    public static EModule create(String value) {
        switch (value) {
            case "core":
                return EModule.CORE;
            case "coreui":
                return EModule.COREUI;
            case "order":
                return EModule.ORDER;
            case "payment":
                return EModule.PAYMENT;
            case "check":
                return EModule.CHECK;
            case "user":
                return EModule.USER;
            case "store":
                return EModule.STORE;
            case "review":
                return EModule.REVIEW;
            case "history":
                return EModule.HISTORY;
            default:
                return null;

        }
    }

    public String getValue() {
        return value;
    }
}
