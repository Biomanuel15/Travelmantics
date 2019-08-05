package com.biomanuel97.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class AdminActivity extends AppCompatActivity {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    EditText txtTitle;
    EditText txtDescription;
    EditText txtPrice;
    int mDealPosition;
    TravelDeal mDeal;
    ImageView dealImageView;
    private DatabaseReference mDealRef;
    private static final int PICTURE_RESULT = 42; //used as an handle for getting image
    private Button mUploadImageBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;


        Intent intent = getIntent();
        this.mDeal = (TravelDeal) intent.getSerializableExtra("Deal");
        if(mDeal == null){
            mDeal = new TravelDeal();
        }

        setDisplay();
        mUploadImageBtn = (Button) findViewById(R.id.btn_select_image);
        mUploadImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Insert Picture"), PICTURE_RESULT);
            }
        });

    }

    private void setDisplay() {
        txtTitle = (EditText) findViewById(R.id.txtTitle);
        txtPrice = (EditText) findViewById(R.id.txtPrice);
        txtDescription = (EditText) findViewById(R.id.txtDescription);
        dealImageView = (ImageView) findViewById(R.id.deal_image);

        txtTitle.setText(mDeal.getTitle());
        txtPrice.setText(mDeal.getPrice());
        txtDescription.setText(mDeal.getDescription());
        showImage(mDeal.getImageUrl());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(this, "Deal Save", Toast.LENGTH_LONG).show();
                clean();
                backToList();
                return true;
            case R.id.delete_menu:
                deleteDeal();
                Toast.makeText(this, "Deal Deleted", Toast.LENGTH_SHORT).show();
                backToList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void clean() {
        txtTitle.setText("");
        txtDescription.setText("");
        txtPrice.setText("");
        txtTitle.requestFocus();
    }

    private void saveDeal() {
        mDeal.setTitle(txtTitle.getText().toString());
        mDeal.setDescription(txtDescription.getText().toString());
        mDeal.setPrice(txtPrice.getText().toString());
        if(mDeal.getId() == null){
            mDatabaseReference.push().setValue(mDeal);
        }else{
            mDatabaseReference.child(mDeal.getId()).setValue(mDeal);
        }
    }

    private void deleteDeal(){
        if (mDeal == null){
            Toast.makeText(this, "Please save the deal before deleting", Toast.LENGTH_SHORT).show();
            return;
        }
        mDatabaseReference.child(mDeal.getId()).removeValue();
        if(mDeal.getImageName() != null && mDeal.getImageName().isEmpty() == false){
            StorageReference picRef = FirebaseUtil.mStorage.getReference().child(mDeal.getImageName());
            picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Delete Image", "Image was successfully deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Delete Image", e.getMessage());
                }
            });
        }
    }

    private void backToList(){
        Intent intent = new Intent(this, UserActivity.class);
        startActivity(intent);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        if (FirebaseUtil.isAdmin){
            menu.findItem(R.id.save_menu).setVisible(true);
            menu.findItem(R.id.delete_menu).setVisible(true);
            enableViews(true);

        }else{
            menu.findItem(R.id.save_menu).setVisible(false);
            menu.findItem(R.id.delete_menu).setVisible(false);
            enableViews(false);
        }
        return true;
    }

    public void enableViews(boolean isEnabled){
        txtTitle.setEnabled(isEnabled);
        txtPrice.setEnabled(isEnabled);
        txtDescription.setEnabled(isEnabled);
        mUploadImageBtn.setEnabled(isEnabled);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            final StorageReference ref = FirebaseUtil.mStorageRef.child(imageUri.getLastPathSegment());
            ref.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String pictureName = taskSnapshot.getStorage().getPath();
                            mDeal.setImageUrl(uri.toString());
                            mDeal.setImageName(pictureName);
                            Log.d("Url", uri.toString());
                            Log.d("Name", pictureName);
                            showImage(uri.toString());
                        }
                    });
                }
            });
        }
    }

    private void showImage(String url){
        if(url != null && url.isEmpty() == false){
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.with(this).load(url).resize(width, width*4/5).centerCrop().into(dealImageView);
        }
    }
}
