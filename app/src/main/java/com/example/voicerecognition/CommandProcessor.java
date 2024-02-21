package com.example.voicerecognition;


import static com.example.voicerecognition.MainActivity.REQUEST_CALL_PHONE_PERMISSION;
import static com.example.voicerecognition.MainActivity.REQUEST_SEND_SMS_PERMISSION;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.os.Handler;

public class CommandProcessor {

    private static MainActivity mainActivity;
    private static Handler handler = new Handler();


    public CommandProcessor(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    //helper method to check the pattern
    public static void processCommand(Context context, String command) {

        // Here, implement the logic to process the recognized command
        // Based on the command, perform specific actions or invoke methods
        if (command.contains("chrome ክፈት") || command.contains("ክሮም ክፈት")||command.contains("chrome")||command.contains("ክሮም")) {
            openChrome(context);
        }
        // Check if spoken text is a command to open camera app
        else if (Pattern.matches("camera ክፈት", command)||Pattern.matches("ካሜራ ክፈት", command)||
        Pattern.matches("camera", command)||Pattern.matches("ካሜራ", command)) {
            openCameraApp(context);
        }
        //check if the spoken text is command to to make a call by contact
        else if (Pattern.matches("ወደ\\s[\\p{InEthiopic}\\p{L}]+\\sደ[^\\s]+", command)) {
            String contactName = command.substring(3, command.length() - 4);
            makeCallByContactName(context, contactName);
        }
        else if (Pattern.matches("ለ[\\p{InEthiopic}\\p{L}]+\\sደ[^\\s]+", command)) {
            String contactName = command.substring(1, command.length() - 4);
            makeCallByContactName(context, contactName);
        }
        else if (Pattern.matches("ወደ\\s(09\\s*\\d\\s*\\d\\s*\\d\\s*\\d\\s*\\d\\s*\\d\\s*\\d\\s*\\d)\\sደ\\w*", command)) {
            String phoneNumber = command.replaceAll("\\D", ""); // Remove non-digit characters from the command
            makeCallByPhoneNumber(context, phoneNumber);
        }
        // Check if spoken text is a command to send an SMS
        if (Pattern.matches("ወደ .+? .+? ብ\\w+ ላክ", command)) {
            Pattern pattern = Pattern.compile("ወደ (.+?) (.+?) ብ\\w+ ላ\\w+");
            Matcher matcher = pattern.matcher(command);
            if (matcher.find()) {
                String contactName = matcher.group(1).trim();
                String message = matcher.group(2).trim();
                sendSmsByContactName(context,contactName, message);
            }

        }

        //check if the spoken text is command to set an alarm
        else if (Pattern.matches("\\d+:\\d+", command)) {
            String[]  timeParts = command.split(":");

            int hour = Integer.parseInt(timeParts[0].trim());
            int minute = Integer.parseInt(timeParts[1].trim());
            setAlarm(context,hour, minute);
        }
        else if (Pattern.matches("\\d+\\s*ሰ\\w*ት\\s*ከ\\s*\\d{1,2}\\s*", command)) {
            // Extract the hour from the command
            String[] commandParts = command.split("\\s+");
            int hour = Integer.parseInt(commandParts[0].replaceAll("\\D", ""));

            // Extract the minute from the command
            int minute = Integer.parseInt(commandParts[3].replaceAll("\\D", ""));

            // Check if the hour and minute values are valid
            if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                // Set the alarm
                setAlarm(context, hour, minute);
            } else {
                // Invalid hour or minute value
                Toast.makeText(context, "Invalid hour or minute value", Toast.LENGTH_SHORT).show();
            }
        }

        // Check if spoken text is a command to turn on the wi-fi
        else if (Pattern.matches("wifi አብራ", command) || Pattern.matches("ዋይፋይ ክፈት",command)
                ||Pattern.matches("wifi", command)||Pattern.matches("ዋይፋይ", command)) {
            enableWiFi(context);
        }
        // Check if spoken text is a command to turn on data connection
        else if (Pattern.matches("connection አብራ", command) || Pattern.matches("ኮኔክሽን ክፈት", command)
                || Pattern.matches("connection", command) || Pattern.matches("ኮኔክሽን", command)) {
            if (enableDataConnection(context)) {
                Toast.makeText(context, "Data connection turned on", Toast.LENGTH_SHORT).show();
            } else {
                redirectToSettings(context);
            }
        }

        else {
            // Invalid command
            Toast.makeText(context.getApplicationContext(),
                    "የተሳሳተ የትዕዛዝ ቃል", Toast.LENGTH_SHORT).show();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mainActivity.startListening();
                }
            }, 1500);  // Delay of 1500 milliseconds (1.5 seconds)
//            mainActivity.startListening();
        }

    }

    //helper method to open the chrome app
    private static void openChrome(Context context ) {
        // Logic to open the Chrome app
        Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.android.chrome");
        if (intent != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "Chrome app not installed", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to open the camera app
    private static void openCameraApp(Context context) {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        context.startActivity(intent);
    }

    // Helper method to enable Wi-Fi
    private static void enableWiFi(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
                Toast.makeText(context, "Wi-Fi turned on", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Wi-Fi is already enabled", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Wi-Fi manager is not available
            Toast.makeText(context, "Wi-Fi manager is not available", Toast.LENGTH_SHORT).show();

            // Open Wi-Fi settings
            Intent settingsIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(settingsIntent);
        }
    }

    // Helper method to enable data connection
    private static boolean enableDataConnection(Context context) {
        ConnectivityManager dataManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (dataManager != null) {
            try {
                Method dataMtd = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
                if (dataMtd != null) {
                    dataMtd.setAccessible(true);
                    dataMtd.invoke(dataManager, true);
                    return true;
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    //helper method to open the setting
    private static void redirectToSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // Helper method to set an alarm
//    private static void setAlarm(Context context,int hour, int minute) {
//        // Create an instance of the AlarmManager
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//
//        // Create an intent to trigger the alarm
//        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent,  PendingIntent.FLAG_IMMUTABLE);
//
//        // Set the alarm time
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(System.currentTimeMillis());
//        calendar.set(Calendar.HOUR_OF_DAY, hour);
//        calendar.set(Calendar.MINUTE, minute);
//        calendar.set(Calendar.SECOND, 0);
//
//        // Schedule the alarm
//        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
//
//        // Notify the user that the alarm is set
//        Toast.makeText(context, "Alarm set for " + hour + ":" + minute, Toast.LENGTH_SHORT).show();
//    }
    //helper method to set an alarm
    private static void setAlarm(Context context,int hour, int minute) {
        // Create an instance of the AlarmManager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Create an intent to trigger the alarm
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent,  PendingIntent.FLAG_IMMUTABLE);

        // Set the alarm time
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // Schedule the alarm
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        // Notify the user that the alarm is set
        Toast.makeText(context, "Alarm set for " + hour + ":" + minute, Toast.LENGTH_SHORT).show();
    }

    // Helper method to get phone number from contact name and make a call
    private static void makeCallByContactName(Context context,String contactName) {
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " = ?",
                new String[]{contactName},
                null);

        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            if (columnIndex >= 0) {
                // column exists, retrieve its value
                String phoneNumber = cursor.getString(columnIndex);
                cursor.close();
                makePhoneCall(context,phoneNumber);
            } else {
                // Could not find phone number for contact
                Toast.makeText(context.getApplicationContext(),
                        "Could not find phone number for " + contactName,
                        Toast.LENGTH_SHORT).show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.startListening();
                    }
                }, 1500);
            }
        } else {
            // Could not find contact
            Toast.makeText(context.getApplicationContext(),
                    "Could not find contact for " + contactName,
                    Toast.LENGTH_SHORT).show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mainActivity.startListening();
                }
            }, 1500);
        }
    }

    // Helper method to make phone call by phone number
    private static void makeCallByPhoneNumber(Context context,String phoneNumber) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        context.startActivity(callIntent);
    }

    // Helper method to make a phone call
    private static void makePhoneCall(Context context,String phoneNumber) {
        // Check if permission to make phone calls is granted
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            context.startActivity(callIntent);
        }
        else {
            // Permission not granted, request it
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{android.Manifest.permission.CALL_PHONE},
                    REQUEST_CALL_PHONE_PERMISSION);
        }
    }

    // Helper method to get phone number from contact name and send an SMS
    private static void sendSmsByContactName(Context context,String contactName, String message) {
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " = ?",
                new String[]{contactName},
                null);

        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            if (columnIndex >= 0) {
                // column exists, retrieve its value
                String phoneNumber = cursor.getString(columnIndex);
                cursor.close();
                sendSms(context,phoneNumber, message);
            } else {
                // Could not find phone number for contact
                Toast.makeText(context.getApplicationContext(),
                        "Could not find phone number for " + contactName,
                        Toast.LENGTH_SHORT).show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.startListening();
                    }
                }, 1500);
            }
        } else {
            // Could not find contact
            Toast.makeText(context.getApplicationContext(),
                    "Could not find contact for " + contactName,
                    Toast.LENGTH_SHORT).show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mainActivity.startListening();
                }
            }, 1500);
        }
    }

    // Helper method to send an SMS
    private static void sendSms(Context context,String phoneNumber, String message) {
        // Check if permission to send SMS is granted
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(context.getApplicationContext(), "SMS sent to " + phoneNumber,
                    Toast.LENGTH_SHORT).show();
        }
        else {
            // Permission not granted, request it
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.SEND_SMS},
                    REQUEST_SEND_SMS_PERMISSION);
             }
    }

}
