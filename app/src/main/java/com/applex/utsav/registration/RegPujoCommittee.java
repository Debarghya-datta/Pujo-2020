package com.applex.utsav.registration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.applex.utsav.MainActivity;
import com.applex.utsav.R;
import com.applex.utsav.SearchCityState;
import com.applex.utsav.models.AccessToken;
import com.applex.utsav.models.BaseUserModel;
import com.applex.utsav.models.NotifCount;
import com.applex.utsav.models.PujoCommitteeModel;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.utility.BasicUtility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class RegPujoCommittee extends AppCompatActivity {

    private EditText etcommitteename, etdescription, etaddressline, ettype, etpin;
    public static EditText etcity,etstate;
    private String scommitteename, sdescription, saddress, scity, stype, sstate, spin;
    private TextView email_pc;
    private Button register;
    private ProgressDialog progressDialog;
    private String userID, usertype;
    private String semail,spassword;
    private ImageView cover_pc,dp_pc,edit_cover_pc,edit_dp_pc;
    private String tokenStr;

    private DocumentReference docrefBase, docrefCommittee, docref2, docref3;
    private BaseUserModel baseUserModel;
    private IntroPref introPref;
    private PujoCommitteeModel pujoCommitteeModel;

    private FirebaseAuth mAuth;
    //    private File file;
    private Uri filepath;
    private Uri downloaduri;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private StorageReference reference;
    private FirebaseUser fireuser;
    private String generatedfilepath;
    byte[] pic, coverpicbyte;
    private AccessToken accessToken;
    private int pictype;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_pujo_committee);

        etaddressline = findViewById(R.id.committee_addressline);
        etcity = findViewById(R.id.committee_city);
        etcommitteename = findViewById(R.id.committee_name);
        etdescription = findViewById(R.id.committee_description);
        ettype = findViewById(R.id.committee_type);
        etstate = findViewById(R.id.committee_state);
        register = findViewById(R.id.register);
        email_pc = findViewById(R.id.email_pc);
        cover_pc = findViewById(R.id.reg_coverpic_pc);
        dp_pc = findViewById(R.id.reg_dp_pc);
        edit_cover_pc = findViewById(R.id.reg_edit_coverpic_icon_pc);
        edit_dp_pc = findViewById(R.id.reg_edit_dp_pc);
        etpin = findViewById(R.id.committee_pin);


        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        introPref = new IntroPref(RegPujoCommittee.this);
        usertype = introPref.getType();

        mAuth=FirebaseAuth.getInstance();
        fireuser= mAuth.getCurrentUser();

        storage= FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        if(getIntent().getStringExtra("email")!=null){
            semail = getIntent().getStringExtra("email");
            email_pc.setText(semail);
            email_pc.setFreezesText(true);
        }
        if(getIntent().getStringExtra("password")!=null){
            spassword = getIntent().getStringExtra("password");
        }

//        semail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
//        spassword = getIntent().getStringExtra("password");

        accessToken= new AccessToken();
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("TAG", "getInstanceId failed", task.getException());
                        BasicUtility.showToast(getApplicationContext(),"Error creating token");
                        return;
                    }
                    // Get new Instance ID token
                    tokenStr = task.getResult().getToken();
                    accessToken.setRegToken(tokenStr);
                    // Log and toast
                    // String msg = getString(R.string.msg_token_fmt, token);
                    Log.d("TAG", tokenStr);
                    // Toast.makeText(RegFormPost2.this, token, Toast.LENGTH_LONG).show();
                });

        cover_pc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
                pictype=1;
            }
        });
        edit_cover_pc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
                pictype=1;
            }
        });
        dp_pc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
                pictype=0;
            }
        });
        edit_dp_pc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
                pictype=10;
            }
        });

        etcity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RegPujoCommittee.this, SearchCityState.class);
                i.putExtra("from","city_pujo");
                startActivity(i);
            }
        });
        etstate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RegPujoCommittee.this, SearchCityState.class);
                i.putExtra("from","state_pujo");
                startActivity(i);
            }
        });




        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                scommitteename = etcommitteename.getText().toString().trim();
                sdescription = etdescription.getText().toString().trim();
                stype = ettype.getText().toString().trim();
                saddress = etaddressline.getText().toString().trim();
                scity = etcity.getText().toString().trim();
                sstate = etstate.getText().toString().trim();
                spin = etpin.getText().toString().trim();


                if (scommitteename.isEmpty() || scity.isEmpty() ||stype.isEmpty() || saddress.isEmpty()
                        || sstate.isEmpty() || spin.isEmpty()|| sdescription.isEmpty() ||pic==null || coverpicbyte==null) {
                    if (scommitteename.isEmpty()) {
                        etcommitteename.setError("Committee name is missing");
                        etcommitteename.requestFocus();
                    }
                    if (sdescription.isEmpty()) {
                        etdescription.setError("Description is missing");
                        etdescription.requestFocus();
                    }
                    if (saddress.isEmpty()) {
                        etaddressline.setError("Address line is missing");
                        etaddressline.requestFocus();
                    }
                    if (scity.isEmpty()) {
                        etcity.setError("City is missing");
                        etcity.requestFocus();
                    }
                    if (sstate.isEmpty()) {
                        etstate.setError("State is missing");
                        etstate.requestFocus();
                    }
                    if (spin.isEmpty()) {
                        etpin.setError("Pincode is missing");
                        etpin.requestFocus();
                    }
                    if (stype.isEmpty()) {
                        ettype.setError("Type is missing");
                        ettype.requestFocus();
                    }
                    if(pic==null){
                        BasicUtility.showToast(RegPujoCommittee.this,"Please set a Profile Photo");
                    }
                    if(coverpicbyte==null){
                        BasicUtility.showToast(RegPujoCommittee.this,"Please set a Cover Photo");
                    }

                }
                else {

                    progressDialog = new ProgressDialog(RegPujoCommittee.this);
                    progressDialog.setTitle("Creating Profile");
                    progressDialog.setMessage("Hang on...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    docrefBase = FirebaseFirestore.getInstance().collection("Users")
                              .document(userID);

                    docref2= FirebaseFirestore.getInstance()
                            .collection("Users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()+"/AccessToken/")
                            .document("Token");
                    docref3= FirebaseFirestore.getInstance()
                            .collection("Users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()+"/notifCount/")
                            .document("notifCount");


                    docrefCommittee = FirebaseFirestore.getInstance().collection("Users/"+userID+"/"+usertype+"/")
                              .document(userID);

                      baseUserModel = new BaseUserModel();
                      baseUserModel.setAddressline(saddress);
                      baseUserModel.setCity(scity);
                      baseUserModel.setEmail(semail);
                      baseUserModel.setName(scommitteename);
                      baseUserModel.setState(sstate);
                      baseUserModel.setUid(userID);
                      baseUserModel.setType(usertype);
                      baseUserModel.setPin(spin);


                      pujoCommitteeModel = new PujoCommitteeModel();
                      pujoCommitteeModel.setDescription(sdescription);
                      pujoCommitteeModel.setType(stype);

                      if(pic!=null && coverpicbyte!=null){
                          NotifCount notifCount= new NotifCount();
                          notifCount.setNotifCount(0);

                          if (pic != null) {
                              reference = storageReference.child("Users/").child("DP/").child(fireuser.getUid() + "_dp");
                              reference.putBytes(pic).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                  @Override
                                  public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                      reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                          @Override
                                          public void onSuccess(Uri uri) {
                                              downloaduri = uri;
                                              generatedfilepath = downloaduri.toString();
                                              introPref.setUserdp(generatedfilepath);
                                              baseUserModel.setDp(generatedfilepath);

                                              if (coverpicbyte != null) {
                                                  reference = storageReference.child("Users/").child("Coverpic/").child(fireuser.getUid() + "_coverpic");
                                                  reference.putBytes(coverpicbyte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                      @Override
                                                      public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                          reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                              @Override
                                                              public void onSuccess(Uri uri) {
                                                                  downloaduri = uri;
                                                                  generatedfilepath = downloaduri.toString();
                                                                  baseUserModel.setCoverpic(generatedfilepath);

                                                                  docrefBase.set(baseUserModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                      @Override
                                                                      public void onComplete(@NonNull Task<Void> task) {

                                                                          if (task.isSuccessful()) {

                                                                              docrefCommittee.set(pujoCommitteeModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                  @Override
                                                                                  public void onComplete(@NonNull Task<Void> task) {
                                                                                      if(task.isSuccessful()){
                                                                                          docref2.set(accessToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                              @Override
                                                                                              public void onComplete(@NonNull Task<Void> task) {
                                                                                                  if(task.isSuccessful()){
                                                                                                      docref3.set(notifCount).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                          @Override
                                                                                                          public void onComplete(@NonNull Task<Void> task) {
                                                                                                              if(task.isSuccessful()){
                                                                                                                  progressDialog.dismiss();
                                                                                                                  BasicUtility.showToast(getApplicationContext(), "Profile Created");
                                                                                                                  Intent intent = new Intent(RegPujoCommittee.this, MainActivity.class);
                                                                                                                  intent.putExtra("uid", fireuser.getUid());
                                                                                                                  startActivity(intent);
                                                                                                                  finish();
                                                                                                              }
                                                                                                              else{
                                                                                                                  progressDialog.dismiss();
                                                                                                                  BasicUtility.showToast(getApplicationContext(), "Something went wrong.");
                                                                                                              }
                                                                                                          }
                                                                                                      })
                                                                                                              .addOnFailureListener(new OnFailureListener() {
                                                                                                                  @Override
                                                                                                                  public void onFailure(@NonNull Exception e) {
                                                                                                                      progressDialog.dismiss();
                                                                                                                      BasicUtility.showToast(getApplicationContext(), "Something went wrong.");
                                                                                                                  }
                                                                                                              });
                                                                                                  }
                                                                                                  else{
                                                                                                      progressDialog.dismiss();
                                                                                                      BasicUtility.showToast(getApplicationContext(), "Something went wrong.");
                                                                                                  }
                                                                                              }
                                                                                          })
                                                                                                  .addOnFailureListener(new OnFailureListener() {
                                                                                                      @Override
                                                                                                      public void onFailure(@NonNull Exception e) {
                                                                                                          progressDialog.dismiss();
                                                                                                          BasicUtility.showToast(getApplicationContext(), "Something went wrong.");
                                                                                                      }
                                                                                                  });
                                                                                      }
                                                                                      else{
                                                                                          progressDialog.dismiss();
                                                                                          BasicUtility.showToast(getApplicationContext(), "Something went wrong.");
                                                                                      }
                                                                                  }
                                                                              })
                                                                                      .addOnFailureListener(new OnFailureListener() {
                                                                                          @Override
                                                                                          public void onFailure(@NonNull Exception e) {
                                                                                              progressDialog.dismiss();
                                                                                              BasicUtility.showToast(getApplicationContext(), "Something went wrong.");
                                                                                          }
                                                                                      });

                                                                          } else {
                                                                              progressDialog.dismiss();
                                                                              BasicUtility.showToast(getApplicationContext(), "Something went wrong.");
                                                                          }
                                                                      }
                                                                  })
                                                                          .addOnFailureListener(new OnFailureListener() {
                                                                              @Override
                                                                              public void onFailure(@NonNull Exception e) {
                                                                                  progressDialog.dismiss();
                                                                                  BasicUtility.showToast(getApplicationContext(), "Something went wrong.");
                                                                              }
                                                                          });
                                                              }
                                                          });
                                                      }
                                                  });
                                              }
                                              else {
                                                  progressDialog.dismiss();
                                                  BasicUtility.showToast(RegPujoCommittee.this,"Please set Profile Photo and Cover Photo");
                                              }
                                          }
                                      });
                                  }
                              });
                          }
                          else{
                              progressDialog.dismiss();
                              BasicUtility.showToast(RegPujoCommittee.this,"Please set Profile Photo and Cover Photo");
                          }
                      }
                      else{
                          progressDialog.dismiss();
                          BasicUtility.showToast(RegPujoCommittee.this,"Please set Profile Photo and Cover Photo");
                      }
                }

            }
        });

    }

    private void chooseImage() {

        Intent intent= new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Image"),1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode== RESULT_OK){
            if(requestCode == 1 && data!=null && data.getData()!=null){
                filepath=data.getData();
                try {
                    if(filepath!=null) {
                        if(pictype==0){
                            CropImage.activity(filepath)
                                    .setActivityTitle("Crop Image")
                                    .setAllowRotation(TRUE)
                                    .setAllowCounterRotation(TRUE)
                                    .setAllowFlipping(TRUE)
                                    .setAspectRatio(1,1)
                                    .setAutoZoomEnabled(TRUE)
                                    .setMultiTouchEnabled(FALSE)
                                    .setGuidelines(CropImageView.Guidelines.ON)
                                    .start(RegPujoCommittee.this);
                        }
                        else if(pictype==1){
                            CropImage.activity(filepath)
                                    .setActivityTitle("Crop Image")
                                    .setAllowRotation(TRUE)
                                    .setAllowCounterRotation(TRUE)
                                    .setAllowFlipping(TRUE)
                                    .setAspectRatio(16,9)
                                    .setAutoZoomEnabled(TRUE)
                                    .setMultiTouchEnabled(FALSE)
                                    .setGuidelines(CropImageView.Guidelines.ON)
                                    .start(RegPujoCommittee.this);
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            ////////////////////////CROP//////////////////////
            else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Uri imageUri = result.getUri();
                Bitmap bitmap = null;
                Bitmap compressedBitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    compressedBitmap = BasicUtility.decodeSampledBitmapFromFile(bitmap, 612, 816);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                compressedBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

//                Bitmap bitmap = null;
//                try {
//                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                if(pictype==0){
                    pic = baos.toByteArray();
                    Bitmap bitmap1 = BitmapFactory.decodeByteArray(pic, 0 , pic.length);
                    dp_pc.setImageBitmap(bitmap1);
                }
                else if(pictype==1){
                    coverpicbyte = baos.toByteArray();
                    Bitmap bitmap2 = BitmapFactory.decodeByteArray(coverpicbyte, 0 , coverpicbyte.length);
                    cover_pc.setImageBitmap(bitmap2);
                }

                //new ImageCompressor().execute();

            }
            else {//CROP ERROR
                Toast.makeText(this, "+error", Toast.LENGTH_SHORT).show();
            }
            ////////////////////////CROP//////////////////////
        }
    }

    class ImageCompressor extends AsyncTask<Void, Void, byte[]> {

        private final float maxHeight = 1080.0f;
        private final float maxWidth = 720.0f;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        public byte[] doInBackground(Void... strings) {

            if(pictype==0) {
                Bitmap scaledBitmap = null;

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                Bitmap bmp = BitmapFactory.decodeByteArray(pic, 0, pic.length, options);

                int actualHeight = options.outHeight;
                int actualWidth = options.outWidth;

                float imgRatio = (float) actualWidth / (float) actualHeight;
                float maxRatio = maxWidth / maxHeight;

                if (actualHeight > maxHeight || actualWidth > maxWidth) {
                    if (imgRatio < maxRatio) {
                        imgRatio = maxHeight / actualHeight;
                        actualWidth = (int) (imgRatio * actualWidth);
                        actualHeight = (int) maxHeight;
                    } else if (imgRatio > maxRatio) {
                        imgRatio = maxWidth / actualWidth;
                        actualHeight = (int) (imgRatio * actualHeight);
                        actualWidth = (int) maxWidth;
                    } else {
                        actualHeight = (int) maxHeight;
                        actualWidth = (int) maxWidth;

                    }
                }

                options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
                options.inJustDecodeBounds = false;
                options.inDither = false;
                options.inPurgeable = true;
                options.inInputShareable = true;
                options.inTempStorage = new byte[16 * 1024];

                try {
                    bmp = BitmapFactory.decodeByteArray(pic, 0, pic.length, options);
                } catch (OutOfMemoryError exception) {
                    exception.printStackTrace();
                }

                try {
                    scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.RGB_565);
                } catch (OutOfMemoryError exception) {
                    exception.printStackTrace();
                }

                float ratioX = actualWidth / (float) options.outWidth;
                float ratioY = actualHeight / (float) options.outHeight;
                float middleX = actualWidth / 4.0f;
                float middleY = actualHeight / 4.0f;

                Matrix scaleMatrix = new Matrix();
                scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

                Canvas canvas = new Canvas(scaledBitmap);
                canvas.setMatrix(scaleMatrix);
                canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 4, middleY - bmp.getHeight() / 4, new Paint(Paint.FILTER_BITMAP_FLAG));

                if (bmp != null) {
                    bmp.recycle();
                }
                scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, out);
                byte[] by = out.toByteArray();
                return by;
            }
            else if(pictype==1){
                Bitmap scaledBitmap = null;

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                Bitmap bmp = BitmapFactory.decodeByteArray(coverpicbyte, 0, coverpicbyte.length, options);

                int actualHeight = options.outHeight;
                int actualWidth = options.outWidth;

                float imgRatio = (float) actualWidth / (float) actualHeight;
                float maxRatio = maxWidth / maxHeight;

                if (actualHeight > maxHeight || actualWidth > maxWidth) {
                    if (imgRatio < maxRatio) {
                        imgRatio = maxHeight / actualHeight;
                        actualWidth = (int) (imgRatio * actualWidth);
                        actualHeight = (int) maxHeight;
                    } else if (imgRatio > maxRatio) {
                        imgRatio = maxWidth / actualWidth;
                        actualHeight = (int) (imgRatio * actualHeight);
                        actualWidth = (int) maxWidth;
                    } else {
                        actualHeight = (int) maxHeight;
                        actualWidth = (int) maxWidth;

                    }
                }

                options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
                options.inJustDecodeBounds = false;
                options.inDither = false;
                options.inPurgeable = true;
                options.inInputShareable = true;
                options.inTempStorage = new byte[16 * 1024];

                try {
                    bmp = BitmapFactory.decodeByteArray(coverpicbyte, 0, coverpicbyte.length, options);
                } catch (OutOfMemoryError exception) {
                    exception.printStackTrace();
                }

                try {
                    scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.RGB_565);
                } catch (OutOfMemoryError exception) {
                    exception.printStackTrace();
                }

                float ratioX = actualWidth / (float) options.outWidth;
                float ratioY = actualHeight / (float) options.outHeight;
                float middleX = actualWidth / 4.0f;
                float middleY = actualHeight / 4.0f;

                Matrix scaleMatrix = new Matrix();
                scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

                Canvas canvas = new Canvas(scaledBitmap);
                canvas.setMatrix(scaleMatrix);
                canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 4, middleY - bmp.getHeight() / 4, new Paint(Paint.FILTER_BITMAP_FLAG));

                if (bmp != null) {
                    bmp.recycle();
                }
                scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, out);
                byte[] by = out.toByteArray();
                return by;
            }
            else{
                return new byte[0];
            }
        }

        @Override
        protected void onPostExecute(byte[] picCompressed) {
            if(picCompressed!= null) {

                if(pictype==0){
                    pic = picCompressed;
                    Bitmap bitmap = BitmapFactory.decodeByteArray(picCompressed, 0 , picCompressed.length);
                    dp_pc.setImageBitmap(bitmap);
                }
                else if(pictype==1){
                    coverpicbyte = picCompressed;
                    Bitmap bitmap = BitmapFactory.decodeByteArray(picCompressed, 0 , picCompressed.length);
                    cover_pc.setImageBitmap(bitmap);
                }
            }
        }

        private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                final int heightRatio = Math.round((float) height / (float) reqHeight);
                final int widthRatio = Math.round((float) width / (float) reqWidth);
                inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            }
            final float totalPixels = width * height;
            final float totalReqPixelsCap = reqWidth * reqHeight * 4;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }

            return inSampleSize;
        }

    }
    @Override
    protected void finalize() throws Throwable {
        Log.d("FINALIZE","called IN REG FORM 2!!!!!!!!!!!!!");
        super.finalize();
    }

}