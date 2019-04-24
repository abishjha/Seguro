package io.mlh.hackhers.seguro;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ContactsActivity extends AppCompatActivity {
    ArrayList <TestContact> m_contactList = new ArrayList<>();
    private static int contactID = 0;
    private static int INVALID_ID = -5000;
    private static String helpMessage = "I need help. You can track my location below";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        setUpContactBoxes();
    }

    private void populateContactData( ArrayList<TestContact> contactList ){
        contactList.add( new TestContact("Ash", "Ghim", "2018878613", "afjjk@jkjk.edu", 0) );
        contactList.add( new TestContact("Prativa", "Parajuli", "2018848613", "pubjjk@jkjk.edu", 1) );
        contactList.add( new TestContact("Niva", "Uli", "2028848619", "sjsjjk@jkjk.edu", 2) );
        contactList.add( new TestContact("Abs", "Jha", "2028845657", "sdk@jkjk.edu", 3) );
    }

    private void setUpContactBoxes(){
        populateContactData(m_contactList);

        LinearLayout allContacts = findViewById(R.id.allContactsLinear);

        for(TestContact i: m_contactList)
            addOneContactToView(i);

        addNewContactIcon();
    }

    private void addNewContactIcon(){
        LinearLayout allContacts = findViewById(R.id.allContactsLinear);

        LinearLayout addContact = new LinearLayout(this);
        addContact.setOrientation(LinearLayout.HORIZONTAL);

        // Add add image icon
        ImageButton plusSign = new ImageButton(this);
        plusSign.setClickable(false);
        int resId = getResources().getIdentifier( "add_sign", "drawable", getPackageName() );
        plusSign.setBackgroundResource( resId );
        plusSign.setLayoutParams(new LinearLayout.LayoutParams(120, 120));
        addContact.addView(plusSign);

        TextView prompt = new TextView(this);
        prompt.setText("Add new contact");  // Use symbolic constant
        prompt.setGravity(-1);
        prompt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 120));
        addContact.addView(prompt);

        addContact.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                show_dialog(INVALID_ID);
            }
        });

        allContacts.addView( addContact );
    }

    private void editContact(View view, LinearLayout parent){

        show_dialog( view.getId() );

        TestContact contact = m_contactList.get(view.getId());

        TextView name = (TextView) parent.getChildAt(0);
        System.out.println("Texxt to set is " + contact.firstName + " " + contact.lastName );
        name.setText(contact.firstName + " " + contact.lastName);
    }

    private void addOneContactToView( TestContact contact){
        LinearLayout allContacts = findViewById(R.id.allContactsLinear);

        final LinearLayout oneContact = new LinearLayout(this);
        oneContact.setId(contactID);
        oneContact.setOrientation(LinearLayout.HORIZONTAL);
        if(contactID % 2 == 0)
            oneContact.setBackgroundColor(Color.rgb(242, 243, 244));

        TextView name = new TextView(this);
        name.setText(contact.firstName + " " + contact.lastName);
        name.setLayoutParams(new LinearLayout.LayoutParams(500, 160));   // Symbolic constants
        name.setId(contactID);
        oneContact.addView(name);


        Button editButton = new Button(this);
        editButton.setBackgroundColor(Color.GREEN);
        editButton.setText("Edit");             // Use symbolic constant
        editButton.setTextColor(Color.BLACK);
        editButton.setId(contactID);
//            editButton.setLayoutParams(new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, 160));
        editButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                editContact(v, oneContact);
            }
        });
        oneContact.addView(editButton);

        Button delButton = new Button(this);
        delButton.setBackgroundColor(Color.RED);
        delButton.setText("Delete");           // Use symbolic constant
        delButton.setTextColor(Color.WHITE);
        delButton.setId(contactID);
//            delButton.setLayoutParams(new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, 160));
        oneContact.addView(delButton);
        delButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                deleteContact(oneContact);
            }
        });

        allContacts.addView(oneContact);
        contactID++;
    }

    private void deleteContact( LinearLayout layoutToDelete){
        int viewId = layoutToDelete.getId();
        LinearLayout allContacts = findViewById(R.id.allContactsLinear);
        allContacts.removeView(layoutToDelete);
    }

    public void show_dialog(final int viewId){
        //Dialog
        final Dialog d = new Dialog(ContactsActivity.this);
        d.setTitle("Input Contact Info: ");
        d.setContentView(R.layout.layout_input_contact_details);

        d.show();

        // Prepopulate the dialog text fields if edit
        if( viewId != INVALID_ID ){
            prepopulateDialog( d, viewId );
        }

        ImageButton okButton = d.findViewById(R.id.doneButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                EditText name = (EditText)d.findViewById(R.id.nameEditText);
                EditText phone = (EditText)d.findViewById(R.id.phoneEditText);
                EditText email = (EditText)d.findViewById(R.id.emailEditText);
                addNewContact(getTextFromEditText(name), getTextFromEditText(phone), getTextFromEditText(email), viewId );
                d.dismiss();
            }
        });

        ImageButton cancelButton = d.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                d.dismiss();
            }
        });
    }

    private void prepopulateDialog( Dialog d, int viewId ){

        TestContact contact = m_contactList.get(viewId);
        EditText name = (EditText)d.findViewById(R.id.nameEditText);
        name.setText(contact.firstName);

        EditText phone = (EditText)d.findViewById(R.id.phoneEditText);
        phone.setText(contact.phoneNumber);

        EditText email = (EditText)d.findViewById(R.id.emailEditText);
        email.setText( contact.email );
    }

    private String getTextFromEditText( EditText editText){
        return editText.getText().toString();
    }

    private void addNewContact( String name, String phone, String email, int viewId ){
        // Make sure the added contact has id
        TestContact contact;

        if( viewId != INVALID_ID ){
            contact = m_contactList.get(viewId);
            contact.firstName = name;
            contact.phoneNumber = phone;
            contact.email = email;
            m_contactList.set(viewId, contact);

            // Gotta change the view as well
            return;
        }

        contact = new TestContact(name,  " ", phone, email, contactID );
        m_contactList.add(contact);

        // Add contact to the view
        LinearLayout allContacts = findViewById(R.id.allContactsLinear);

        // Remove the last view
        final int childCount = allContacts.getChildCount();
        allContacts.removeView(allContacts.getChildAt(childCount - 1));

        addOneContactToView(contact);

        addNewContactIcon();
    }

//    public void doneButtonListener(View view){
//        this.finish();
//    }

//    public void deleteThisFunction(View view){
//        Intent intent = new Intent(this, SettingsActivity.class);
//        startActivity(intent);
//    }

    public void respondToHelpButton(View view){
//        TextView help = findViewById(R.id.helpString);
//        String helpString = "";
//        if(help != null)
//            helpString = help.toString();
//
//        if(!helpString.isEmpty() )
//            helpMessage = helpString;

        for(TestContact i: m_contactList) {
//            String toPhoneNumber = "+1"+ i.phoneNumber;
            String toPhoneNumber = "+1"+ "2018878613";
            String fromPhoneNumber = "+12015747548";
            sendSms(toPhoneNumber, fromPhoneNumber, "I need help. You can track my location below");
        }

        System.out.println( " Passed checkpoint 1");
    }

    private void sendSms(String toPhoneNumber, String fromPhoneNumber, String message){

        System.out.println( " Passed checkpoint 2");

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.setThreadPolicy( new StrictMode.ThreadPolicy.Builder().permitAll().build() );
        }

        OkHttpClient client = new OkHttpClient();
        String url = "https://api.twilio.com/2010-04-01/Accounts/"+"AC0833244b1a144c6584b596efc516a0d7"+"/SMS/Messages";
        String base64EncodedCredentials = "Basic " + Base64.encodeToString(("AC0833244b1a144c6584b596efc516a0d7" + ":" + "9bfbf8e52950ac58f600387cead87e1f").getBytes(), Base64.NO_WRAP);

        RequestBody body = new FormBody.Builder()
                .add("From", fromPhoneNumber)
                .add("To", toPhoneNumber)
                .add("Body", message)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", base64EncodedCredentials)
                .build();
        try {
            Response response = client.newCall(request).execute();
//            Log.d(TAG, "sendSms: "+ response.body().string());

            System.out.println("sendSms: "+ response.body().string());

        } catch (IOException e) { e.printStackTrace(); }

    }
}
