package com.example.voicerecognition;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int REQUEST_READ_CONTACTS_PERMISSION = 201;
    static final int REQUEST_CALL_PHONE_PERMISSION = 202;
    static final int REQUEST_SEND_SMS_PERMISSION = 203;

    private TextView resultTextView;
    private SpeechRecognizer speechRecognizer;
    private CommandProcessor commandProcessor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        commandProcessor = new CommandProcessor(this);

        ImageView imageView = findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.image);

        Button startButton = findViewById(R.id.startButton);
        resultTextView = findViewById(R.id.resultTextView);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                resultTextView.setText("በማዳመጥ ላይ...");
            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {
                resultTextView.setText("በመተንተን ላይ...");
            }

            @Override
            public void onError(int errorCode) {
                String errorMessage = getErrorDescription(errorCode);
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }

            private String getErrorDescription(int errorCode) {
                switch (errorCode) {
                    case SpeechRecognizer.ERROR_AUDIO:
                        return "Audio recording error";
                    case SpeechRecognizer.ERROR_CLIENT:
                        return "Client side error";
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        return "Insufficient permissions";
                    case SpeechRecognizer.ERROR_NETWORK:
                        return "Network error";
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        return "Network timeout";
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        return "No speech recognition match";
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        return "RecognitionService busy";
                    case SpeechRecognizer.ERROR_SERVER:
                        return "Server error";
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        return "No speech input";
                    default:
                        return "Unknown error";
                }
            }


            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String recognizedText = matches.get(0);
                    resultTextView.setText(recognizedText);
                    CommandProcessor.processCommand(MainActivity.this, recognizedText);

                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }

        });
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestRecordPermission();
            }
        });
    }

    // Request permission to record audio
    private void requestRecordPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            // Permission already granted
            requestContactsPermission();
        }
    }

    // Request permission to read contacts
    private void requestContactsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS_PERMISSION);
        } else {
            // Permission already granted
            requestCallPhonePermission();
        }
    }

    // Request permission to make phone calls
    private void requestCallPhonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    REQUEST_CALL_PHONE_PERMISSION);
        } else {
            // Permission already granted
            requestSendSmsPermission();
        }
    }

    // Request permission to send SMS
    private void requestSendSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    REQUEST_SEND_SMS_PERMISSION);
        } else {
            // Permission already granted
            startListening();
        }
    }

    void startListening() {

        // Create intent for speech recognition
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "am");
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        speechRecognizer.startListening(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    // Handle speech recognition result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Read contacts permission granted
                    requestContactsPermission();
                } else {
                    // Read contacts permission denied
                    Toast.makeText(getApplicationContext(),
                            "Permission to record audio denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case REQUEST_READ_CONTACTS_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Read contacts permission granted
                    requestCallPhonePermission();
                } else {
                    // Read contacts permission denied
                    Toast.makeText(getApplicationContext(),
                            "Permission to read contacts denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case REQUEST_CALL_PHONE_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Call phone permission granted
                    requestSendSmsPermission();
                } else {
                    // Call phone permission denied
                    Toast.makeText(getApplicationContext(),
                            "Permission to make phone calls denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case REQUEST_SEND_SMS_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Send SMS permission granted
                    startListening();
                } else {
                    // Send SMS permission denied
                    Toast.makeText(getApplicationContext(),
                            "Permission to send SMS denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }

    }
}