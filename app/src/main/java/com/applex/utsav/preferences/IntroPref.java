package com.applex.utsav.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.applex.utsav.models.UserSearchModel;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class IntroPref {

    private SharedPreferences preferences;
    private final SharedPreferences.Editor editor;
    private static final String PREF_NAME = "com.applex.campus24.users";
    private static final String IS_FIRST_TIME_LAUNCH = "firstTime";
    private static final String IS_FIRST_TIME = "firsttime";
    private static final String USERDP = "userdp";
    private static final String FULLNAME = "fullname";
    private static final String USERTYPE = "type";
    private static final String GENDER= "gender";
    private static final String ACCOUNT= "account";
    private static final String CITY = "city";
    private static final String LANGUAGE = "language";
    private static final String VOLUME = "volume";
    private static final String THEME = "theme";
    private static final String SEARCH_HISTORY = "search_history";
    private static final String OPEN_COUNT = "open_count";
    private final Gson gson;

    @SuppressLint("CommitPrefEdits")
    public IntroPref(Context context){
        if(context != null) {
            preferences = context.getSharedPreferences(PREF_NAME, 0);
        }
        editor = preferences.edit();
        gson = new Gson();
    }

    public void setIsFirstTimeLaunch(boolean firstTimeLaunch) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, firstTimeLaunch);
        editor.commit();
    }

    public boolean isFirstTimeLaunch(){
        return preferences.getBoolean(IS_FIRST_TIME_LAUNCH,true);
    }

    public void setIsFirstTime(boolean firstTimeLaunch) {
        editor.putBoolean(IS_FIRST_TIME, firstTimeLaunch);
        editor.commit();
    }

    public boolean isFirstTime(){
        return preferences.getBoolean(IS_FIRST_TIME,true);
    }

    //GENDER////
    public String getGender(){
        return preferences.getString(GENDER, null);
    }

    public void setGender(String gender){
        editor.putString(GENDER, gender);
        editor.apply();
    }
    //GENDER////

    //CITY////
    public String getCity(){
        return preferences.getString(CITY, null);
    }

    public void setCity(String city){
        editor.putString(CITY, city);
        editor.apply();
    }
    //CITY////
    ///LANG///
    public String getLanguage(){
        return preferences.getString(LANGUAGE, "en");
    }

    public void setLanguage(String language){
        editor.putString(LANGUAGE, language);
        editor.apply();
    }
    ///LANG///

    ///USERDP///
    public String getUserdp(){
        return preferences.getString(USERDP, null);
    }

    public void setUserdp(String userdp){
        editor.putString(USERDP, userdp);
        editor.apply();
    }
    ///USERDP///

    ///FULLNAME///
    public String getFullName(){
        return preferences.getString(FULLNAME, null);
    }

    public void setFullName(String fullName){
        editor.putString(FULLNAME, fullName);
        editor.apply();
    }
    ///FULLNAME///

    ///TYPE///
    public String getType(){
        return preferences.getString(USERTYPE, null);
    }

    public void setType(String type){
        editor.putString(USERTYPE, type);
        editor.apply();
    }
    ///TYPE///

    public void setIsVolumeOn(boolean volume) {
        editor.putBoolean(VOLUME, volume);
        editor.commit();
    }

    public boolean isVolumeOn(){
        return preferences.getBoolean(VOLUME,true);
    }

    public void setTheme(int isDarkMode){
        editor.putInt(THEME, isDarkMode);
        editor.apply();
    }

    public int getTheme() { return preferences.getInt(THEME,1); }

    public void setRecentSearchHistory(ArrayList<UserSearchModel> arrayList) {
        String list = gson.toJson(arrayList);
        editor.putString(SEARCH_HISTORY, list);
        editor.apply();
    }

    public ArrayList<UserSearchModel> getRecentSearchHistory() {
        String response = preferences.getString(SEARCH_HISTORY, null);
        return gson.fromJson(response, new TypeToken<ArrayList<UserSearchModel>>(){}.getType());
    }

    public void setCount(int count) {
        editor.putInt(OPEN_COUNT, count);
        editor.apply();
    }

    public int getCount() { return preferences.getInt(OPEN_COUNT, 1); }

    public GoogleSignInAccount getGoogleSignInAccount() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriDeserializer())
                .create();
        String json = preferences.getString(ACCOUNT, "");
        return gson.fromJson(json, GoogleSignInAccount.class);
    }

    public void setGoogleSignInAccount(GoogleSignInAccount googleSignInAccount) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriSerializer())
                .create();
        String json = gson.toJson(googleSignInAccount);
        editor.putString(ACCOUNT, json);
        editor.apply();
    }

    public static class UriSerializer implements JsonSerializer<Uri> {
        public JsonElement serialize(Uri src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }

    public static class UriDeserializer implements JsonDeserializer<Uri> {
        @Override
        public Uri deserialize(final JsonElement src, final Type srcType,
                               final JsonDeserializationContext context) throws JsonParseException {
            return Uri.parse(src.getAsString());
        }
    }
}