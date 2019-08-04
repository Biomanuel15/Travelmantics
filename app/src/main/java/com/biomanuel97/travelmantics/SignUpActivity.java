package com.biomanuel97.travelmantics;

import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SignUpActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mFullname;
    private TextInputLayout mPassword;
    private String mUserEmail;
    private String mUserFullname;
    private String mUserPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mEmail = (EditText) findViewById(R.id.text_email);
        mFullname = (EditText) findViewById(R.id.text_fullname);
        mPassword = (TextInputLayout) findViewById(R.id.password);
        Button saveButton = (Button) findViewById(R.id.save_btn);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUserEmail = String.valueOf(mEmail.getText());
                mUserFullname = String.valueOf(mFullname.getText());
                mUserPassword = String.valueOf(mPassword.getEditText().getText());
            }
        });
    }
}
