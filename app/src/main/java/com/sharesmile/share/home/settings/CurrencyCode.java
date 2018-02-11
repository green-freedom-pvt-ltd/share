package com.sharesmile.share.home.settings;

/**
 * Created by ankitmaheshwari on 10/12/17.
 */

public enum CurrencyCode {

    USD("$"),
    EUR("€"),
    JPY("¥"),
    GBP("£"),
    MXN("$"),
    AUD("A$"),
    CAD("C$"),
    CHF("Fr"),
    CNY("元"),
    SEK("kr"),
    NZD("NZ$"),
    SGD("S$"),
    HKD("HK$"),
    NOK("kr"),
    KRW("₩"),
    TRY("₺"),
    RUB("\u20BD"),
    INR("₹"),
    BRL("R$"),
    ZAR("R");

    private String symbol;

    CurrencyCode(String symbol){
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public static CurrencyCode fromString(String input){
        if (input == null || input.isEmpty()){
            return null;
        }
        input = input.toUpperCase();
        for (CurrencyCode currencyCode : CurrencyCode.values()){
            if (currencyCode.toString().equals(input)){
                return currencyCode;
            }
        }
        return null;
    }

    public float getDefaultExchangeRate(){
        switch (this){
            case USD:
                return 65.08f;
            case CAD:
                return 52.26f;
            case INR:
                return 1f;
            case GBP:
                return 86.11f;
            case EUR:
                return 77.21f;
            case JPY:
                return 0.58f;
            case AUD:
                return 50.94f;
            case CHF:
                return 66.83f;
            default:
                return 1f;
        }
    }
}
