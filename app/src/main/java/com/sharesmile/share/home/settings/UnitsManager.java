package com.sharesmile.share.home.settings;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.SharedPrefsManager;
import com.sharesmile.share.core.cause.model.ExchangeRate;
import com.sharesmile.share.utils.Utils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.sharesmile.share.core.Constants.DEFAULT_EXCHANGE_RATE_JSON;
import static com.sharesmile.share.core.Constants.PREFS_DEFAULT_UNITS_SET;
import static com.sharesmile.share.core.Constants.PREFS_MY_COUNTRY_CODE;
import static com.sharesmile.share.core.Constants.PREFS_MY_CURRENCY;
import static com.sharesmile.share.core.Constants.PREFS_MY_DISTANCE_UNIT;
import static com.sharesmile.share.core.Constants.PREFS_MY_EXCHANGE_RATE;

/**
 * Created by ankitmaheshwari on 10/12/17.
 */

public class UnitsManager {

    private static final String TAG = "UnitsManager";

    public static boolean defaultUnitsSet(){
        return SharedPrefsManager.getInstance().getBoolean(PREFS_DEFAULT_UNITS_SET, false);
    }

    public static void setDefaultUnits(){
        String countryCode = Utils.getUserCountry(MainApplication.getContext());

        // Default to INR and Kms
        String currencyCodeString = CurrencyCode.INR.toString();
        String distanceUnitString = DistanceUnit.KILOMETER.toString();

        if (countryCode != null && !countryCode.isEmpty()){
            String curr = getCurrencyFromCountry(countryCode);
            if (!TextUtils.isEmpty(curr)){
                CurrencyCode code = CurrencyCode.fromString(curr.toUpperCase());
                if (code != null){
                    currencyCodeString = curr.toUpperCase();
                }
            }
        }

        if ("US".equalsIgnoreCase(countryCode)){
            distanceUnitString = DistanceUnit.MILES.toString();
        }

        SharedPrefsManager.getInstance().setString(PREFS_MY_CURRENCY, currencyCodeString);
        SharedPrefsManager.getInstance().setString(Constants.PREFS_MY_DISTANCE_UNIT, distanceUnitString);
        if (!TextUtils.isEmpty(countryCode)){
            SharedPrefsManager.getInstance().setString(PREFS_MY_COUNTRY_CODE, countryCode);
        }
        SharedPrefsManager.getInstance().setBoolean(PREFS_DEFAULT_UNITS_SET, true);
    }

    private static List<ExchangeRate> getExchangeRates(){
        List<ExchangeRate> rates = SharedPrefsManager.getInstance().getCollection(Constants.PREFS_MY_EXCHANGE_RATE,
                new TypeToken<ArrayList<ExchangeRate>>(){}.getType());
        if (rates == null || rates.isEmpty()){
            Gson gson = new Gson();
            rates = gson.fromJson(DEFAULT_EXCHANGE_RATE_JSON, new TypeToken<ArrayList<ExchangeRate>>(){}.getType());
        }
        return rates;
    }

    public static double getExchangeRateForMyCurrency(){
        String myCurrencyCodeString = getCurrencyCode().toString();
        for (ExchangeRate rate : getExchangeRates()){
            if (myCurrencyCodeString.equalsIgnoreCase(rate.getCurrencyCode())){
                return rate.getRate();
            }
        }
        return 1;
    }

    public static void setCurrencyCode(CurrencyCode currencyCode){
        SharedPrefsManager.getInstance().setString(PREFS_MY_CURRENCY, currencyCode.toString());
    }

    public static void setDistanceUnit(DistanceUnit distanceUnit){
        SharedPrefsManager.getInstance().setString(Constants.PREFS_MY_DISTANCE_UNIT, distanceUnit.toString());
    }

    public static void setExchangeRates(List<ExchangeRate> exchangeRates){
        if (exchangeRates == null || exchangeRates.isEmpty()){
            return;
        }
        // Validate the input
        Map<CurrencyCode, Float> map = new HashMap<>();
        for (ExchangeRate rate : exchangeRates){
            CurrencyCode currencyCode = CurrencyCode.fromString(rate.getCurrencyCode());
            if (currencyCode == null){
                throw new IllegalArgumentException(rate.getCurrencyCode() + " is not valid");
            }else if (rate.getRate() == 0){
                throw new IllegalArgumentException(rate.getCurrencyCode() + " cannot have 0 exchange rate");
            }
            map.put(currencyCode, rate.getRate());
        }
        for (CurrencyCode currencyCode : CurrencyCode.values()){
            // Check if this currencyCode is present in map created from input list
            if (map.get(currencyCode) == null){
                throw new IllegalArgumentException(currencyCode + " not present in input list of exchange rates");
            }
        }
        SharedPrefsManager.getInstance().setCollection(PREFS_MY_EXCHANGE_RATE, exchangeRates);
    }

    public static CurrencyCode getCurrencyCode(){
        String currCodeString = SharedPrefsManager.getInstance().getString(PREFS_MY_CURRENCY, "INR");
        return CurrencyCode.fromString(currCodeString);
    }

    public static DistanceUnit getDistanceUnit(){
        String distUnitString = SharedPrefsManager.getInstance().getString(PREFS_MY_DISTANCE_UNIT,
                DistanceUnit.KILOMETER.toString());
        return DistanceUnit.fromString(distUnitString);
    }

    public static String getCurrencySymbol(){
        return getCurrencyCode().getSymbol();
    }

    public static String getDistanceLabel(){
        return getDistanceUnit().getLabel();
    }

    public static boolean isImperial(){
        return DistanceUnit.MILES.equals(getDistanceUnit());
    }

    public static String formatToMyDistanceUnitWithTwoDecimal(float distanceInMeters){
        if (DistanceUnit.MILES.equals(getDistanceUnit())){
            return Utils.getDecimalFormat("0.00").format(distanceInMeters*(0.000621));
        }else {
            return Utils.getDecimalFormat("0.00").format(distanceInMeters / 1000);
        }
    }

    public static String getCurrencyFromCountry(String countryCode){
        if (countryCode == null || countryCode.isEmpty()){
            return null;
        }
        countryCode = countryCode.toUpperCase();
        Locale locale = new Locale("EN",countryCode);
        Currency currency = Currency.getInstance(locale);
        return currency.getCurrencyCode();
    }

    private static String formatAsPerCurrency(long value, Currency currency){
        NumberFormat format =  NumberFormat.getInstance();
        format.setCurrency(currency);
        return format.format(value);
    }

    private static String formatAsPerCurrency(float value, Currency currency){
        NumberFormat format =  NumberFormat.getInstance();
        format.setCurrency(currency);
        format.setMaximumFractionDigits(2);
        return format.format(value);
    }

    private static String formatAsPerCurrency(double value, CurrencyCode currency){
        NumberFormat format = null;
        if (CurrencyCode.INR.equals(currency)){
            // If it is INR then we create instance using Indian Locale
            format = NumberFormat.getInstance(new Locale("EN", "IN"));
            // And we will remove all decimals
            format.setMaximumFractionDigits(0);
        } else {
            // If it is any other supported currency then we create instance using US Locale
            format = NumberFormat.getInstance(new Locale("EN", "US"));
            if (value > 1000){
                format.setMaximumFractionDigits(0);
            }else {
                format.setMaximumFractionDigits(2);
                format.setMinimumFractionDigits(2);
            }
        }
        String ret = format.format(value);
        return ret;
    }

    public static String formatRupeeToMyCurrency(long rupeeAmount){
        double amount = rupeeAmount / getExchangeRateForMyCurrency();
        return formatInMyCurrency(amount);
    }

    public static String formatRupeeToMyCurrency(float rupeeAmount){
        double amount = rupeeAmount / getExchangeRateForMyCurrency();
        return formatInMyCurrency(amount);
    }

    public static String formatRupeeToMyCurrency(double rupeeAmount){
        double amount = rupeeAmount / getExchangeRateForMyCurrency();
        return formatInMyCurrency(amount);
    }

    public static String impactToVoice(long rupeeAmount){
        double amount = rupeeAmount / getExchangeRateForMyCurrency();
        StringBuilder sb = new StringBuilder();
        if (getCurrencySymbol().contains("$")){
            int cents = (int)Math.round(amount*100);
            int dollar = cents / 100;
            cents = cents % 100;
            if (dollar > 0){
                sb.append(dollar).append(" dollars ");
            }
            sb.append(cents).append(" cents ");
            return sb.toString();
        }else if (CurrencyCode.INR.equals(getCurrencyCode())){
            return String.format("%d rupees", rupeeAmount);
        }else {
            // Other currencies
            NumberFormat format = null;
            format = NumberFormat.getInstance(new Locale("EN", "US"));
            if (amount > 100){
                format.setMaximumFractionDigits(0);
            }else {
                format.setMaximumFractionDigits(2);
            }
            return format.format(amount) + " " + getCurrencySymbol();
        }
    }

    public static String distanceToVoice(float distanceInMeters){
        NumberFormat format = null;
        StringBuilder sb = new StringBuilder();
        format = NumberFormat.getInstance(new Locale("EN", "US"));
        if (distanceInMeters > 100){
            format.setMaximumFractionDigits(1);
            if (DistanceUnit.KILOMETER.equals(getDistanceUnit())){
                sb.append(format.format(distanceInMeters / 1000) + " ");
            }else {
                sb.append(format.format(distanceInMeters*(0.000621)) + " ");
            }
        }else {
            sb.append("0 ");
        }

        if (DistanceUnit.KILOMETER.equals(getDistanceUnit())){
            sb.append("kilometers");
        }else {
            sb.append("miles");
        }

        return sb.toString();
    }

    private static String formatInMyCurrency(double amount){
        return getCurrencySymbol() + " " + formatAsPerCurrency(amount, getCurrencyCode());
    }







}
