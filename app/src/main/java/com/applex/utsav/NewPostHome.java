package com.applex.utsav;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.applex.utsav.LinkPreview.ApplexLinkPreview;
import com.applex.utsav.LinkPreview.ViewListener;
import com.applex.utsav.adapters.MultipleImageAdapter;
import com.applex.utsav.adapters.TagAdapter;
import com.applex.utsav.models.HomePostModel;
import com.applex.utsav.models.PujoTagModel;
import com.applex.utsav.models.ReelsPostModel;
import com.applex.utsav.models.TagModel;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.dialogs.BottomTagsDialog;
import com.applex.utsav.utility.BasicUtility;
import com.applex.utsav.utility.InternetConnection;
import com.applex.utsav.utility.StoreTemp;
import com.applex.utsav.videoCompressor.VideoCompress;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED;

import static com.applex.utsav.utility.BasicUtility.tsLong;

public class NewPostHome extends AppCompatActivity implements BottomTagsDialog.BottomSheetListener {

    private ArrayList<TagModel> selected_tags;

    private TextView postusername;
    private Button post;
    private LinearLayout cam, gallery,  videopost, videocam;
    private RelativeLayout addToPost, icons;
    private EditText postcontent, edtagtxt, head_content;
    private ImageView cross, user_image, video_cam_icon, video_gal_icon;
    private ImageView postimage;
    private LinearLayout customTag;

    private ApplexLinkPreview LinkPreview;
    private IntroPref introPref;
    private RecyclerView recyclerView;

    private String textdata = "", colorValue;
    private RecyclerView tags_selectedRecycler;
    private TagAdapter tagAdapter2;
    private ImageCompressor imageCompressor;

    private Uri filePath, finalUri, videoUri;
    private ArrayList<byte[]> imagelist = new ArrayList<>();
    private StorageReference storageReferenece;
    private ArrayList<String> generatedFilePath = new ArrayList<>();

    private Uri downloadUri;
    private String ts, USERNAME, PROFILEPIC;

    private FirebaseAuth mAuth;
    private FirebaseUser fireuser;

    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 2000;
    private static final int VIDEO_PICK_GALLERY_CODE = 3000;
    private static final int VIDEO_PICK_CAMERA_CODE = 4000;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int TAG_PUJO = 100;


    String[] cameraPermission;
    String[] storagePermission;
    private byte[] pic;
    private ProgressDialog progressDialog;

    private RelativeLayout container_image;
    private VideoView videoView;
    private int duration;

    private ReelsPostModel reelsPostModel;
    private HomePostModel homePostModel, editPostModel;
    private DocumentReference docRef;
    private byte[] frame;
    private FrameLayout videoframe;

    private TextView tagPujo;

    @SuppressLint("WrongThread")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        postusername = findViewById(R.id.post_username);
        user_image = findViewById(R.id.user_image99);
        cross = findViewById(R.id.cross99);
        cam= findViewById(R.id.camera);
        gallery = findViewById(R.id.Photos);
        postcontent = findViewById(R.id.post_content);
        postimage = findViewById(R.id.post_image);
        post = findViewById(R.id.post);
        recyclerView = findViewById(R.id.recyclerimages);
        icons = findViewById(R.id.icons);
        container_image = findViewById(R.id.image_container);
        LinkPreview = findViewById(R.id.LinkPreView);
        videoView = findViewById(R.id.videoview);
        videopost = findViewById(R.id.Video);
        videocam = findViewById(R.id.Recorder);
        customTag = findViewById(R.id.tag);
        addToPost = findViewById(R.id.add_to_post);
        videoframe = findViewById(R.id.videoframe);
        video_cam_icon = findViewById(R.id.video_cam_icon);
        video_gal_icon = findViewById(R.id.video_gal_icon);
        head_content = findViewById(R.id.post_headline);

        mAuth = FirebaseAuth.getInstance();
        fireuser = mAuth.getCurrentUser();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReferenece = storage.getReference();

        tags_selectedRecycler = findViewById(R.id.tags_selectedList) ;
        selected_tags = new ArrayList<>();
        tagPujo = findViewById(R.id.pujo_tag);

        buildRecyclerView_selectedtags();


        ///////////////////LOADING CURRENT USER DP AND UNAME//////////////////////

        if(Objects.requireNonNull(getIntent().getStringExtra("target")).matches("1")) { //committee
            tagPujo.setVisibility(View.GONE);

            video_cam_icon.setVisibility(View.VISIBLE);
            video_gal_icon.setVisibility(View.VISIBLE);
            videopost.setVisibility(View.VISIBLE);
            videocam.setVisibility(View.VISIBLE);
            head_content.setVisibility(View.VISIBLE);
        }
        else if(Objects.requireNonNull(getIntent().getStringExtra("target")).matches("2")) { //indi
            tagPujo.setVisibility(View.VISIBLE);

            video_cam_icon.setVisibility(View.GONE);
            video_gal_icon.setVisibility(View.GONE);
            head_content.setVisibility(View.GONE);
            videopost.setVisibility(View.GONE);
            videocam.setVisibility(View.GONE);
        }

        // get the bottom sheet view
        LinearLayout llBottomSheet = findViewById(R.id.new_post_bottomsheet);

        // init the bottom sheet behavior
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        //icons.setVisibility(View.GONE);

        llBottomSheet.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    //icons.setVisibility(View.GONE);

                }
                else{
                    bottomSheetBehavior.setState(STATE_COLLAPSED);
                    //icons.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });

        addToPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    icons.setVisibility(View.GONE);

                }
                else{
                    bottomSheetBehavior.setState(STATE_COLLAPSED);
                    icons.setVisibility(View.VISIBLE);
                }

            }
        });

        introPref = new IntroPref(NewPostHome.this);
        USERNAME = introPref.getFullName();
        postusername.setText(USERNAME);

        PROFILEPIC = introPref.getUserdp();
        if(PROFILEPIC!= null){
            Picasso.get().load(PROFILEPIC).into(user_image);
        }
        ///////////////////LOADING CURRENT USER DP AND UNAME//////////////////////


        editPostModel= new HomePostModel();
        reelsPostModel = new ReelsPostModel();
        homePostModel = new HomePostModel();


        ///////////////SHARED CONTENT///////////////
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if(type == null && intent.getStringExtra("target")!=null){
            List<String> postingIn = new ArrayList<>();

            if(intent.getStringExtra("target").matches("1")){
                postingIn.add("Your Campus");
                postingIn.add("Global");
            }
            else if(intent.getStringExtra("target").matches("2")){
                tagPujo.setVisibility(View.VISIBLE);
                postingIn.add("Global");
                postingIn.add("Your Campus");
            }

            else if(intent.getStringExtra("target").matches("11")){ //Challenge
                postingIn.add("Global");

            }

            else if(intent.getStringExtra("target").matches("4")){ //Community
                postingIn.add(intent.getStringExtra("comName"));

            }

            if(intent.getStringExtra("target").matches("100")){// EDIT POST
                if(intent.getStringExtra("usN")!=null){
                    editPostModel.setUsN(intent.getStringExtra("usN"));
                    postusername.setText(editPostModel.getUsN());

                }

                if(intent.getStringExtra("dp")!=null)
                    editPostModel.setDp(intent.getStringExtra("dp"));

                if(intent.getStringExtra("uid")!=null)
                    editPostModel.setUid(intent.getStringExtra("uid"));

                if(intent.getStringExtra("bool")!=null){
                    if(intent.getStringExtra("bool").matches("0")||intent.getStringExtra("bool").matches("2")){
                        postingIn.add("Global");
                    }
                    else if(intent.getStringExtra("bool").matches("3")){
                        postingIn.add("Your Campus");
                    }
                }

                if(intent.getStringExtra("challengeID")!=null){
                    postingIn.add("Global");

                    editPostModel.setChallengeID(intent.getStringExtra("challengeID"));
                }

                if(intent.getSerializableExtra("reportL")!=null)
                    editPostModel.setReportL((ArrayList<String>) intent.getSerializableExtra("reportL"));

                if(intent.getStringExtra("docID")!=null)
                    editPostModel.setDocID(intent.getStringExtra("docID"));

                if(intent.getStringExtra("type")!=null)
                    editPostModel.setType(intent.getStringExtra("type"));

                if(intent.getStringExtra("likeCheck")!=null)
                    editPostModel.setLikeCheck(Integer.parseInt(intent.getStringExtra("likeCheck")));

                if(intent.getSerializableExtra("likeL")!=null)
                    editPostModel.setLikeL((ArrayList<String>) intent.getSerializableExtra("likeL"));

                if(intent.getStringExtra("cmtNo")!=null)
                    editPostModel.setCmtNo(Long.parseLong(intent.getStringExtra("cmtNo")));

                if((StoreTemp.getInstance().getTagTemp())!=null){
                    editPostModel.setTagL(StoreTemp.getInstance().getTagTemp());
                    tags_selectedRecycler.setVisibility(View.VISIBLE);
                    selected_tags= editPostModel.getTagL();

                    tagAdapter2.notifyDataSetChanged();
                    buildRecyclerView_selectedtags();
                }


                if(intent.getStringExtra("txt")!=null){
                    editPostModel.setTxt(intent.getStringExtra("txt"));
                    postcontent.setText(editPostModel.getTxt());
                }

                if(intent.getStringExtra("comID")!=null){
                    postingIn.add(intent.getStringExtra("comName"));
                    editPostModel.setComID(intent.getStringExtra("comID"));
                }


                if(intent.getStringExtra("comName")!=null)
                    editPostModel.setComName(intent.getStringExtra("comName"));

                if(intent.getStringExtra("ts")!=null)
                    editPostModel.setTs(Long.parseLong(intent.getStringExtra("ts")));

                if(intent.getStringExtra("newTs")!=null)
                    editPostModel.setNewTs(Long.parseLong(intent.getStringExtra("newTs")));

            }


        }

        ///////////////////SHARED CONTENT////////////////////
        if(Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    postcontent.setText(sharedText);
                    if(postcontent.getUrls().length>0){
                        URLSpan urlSnapItem = postcontent.getUrls()[0];
                        String url = urlSnapItem.getURL();
                        if(url!= null && url.contains("http")){
                            LinkPreview.setLink(url ,new ViewListener() {
                                @Override
                                public void onSuccess(boolean status) { }

                                @Override
                                public void onError(Exception e) {
                                }
                            });
                        }
                    }
                }
            }
            else if (type.startsWith("image/")) {
                filePath = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                finalUri = filePath;
                //container_image.setVisibility(View.VISIBLE);
                postimage.setVisibility(View.VISIBLE);
                postimage.setImageURI(finalUri);

                Bitmap bitmap = null;
                try {
                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    options.inSampleSize = 2;
                    options.inJustDecodeBounds = false;
                    options.inTempStorage = new byte[16 * 1024];

                    InputStream input = this.getContentResolver().openInputStream(filePath);
                    bitmap = BitmapFactory.decodeStream(input, null, options);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos =new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                pic = baos.toByteArray();

            }

        }
        //////////////////SHARED CONTENT///////////////////


        ///////////////////////IMAGE HANDLING////////////////////////
        gallery.setOnClickListener(v -> {
            if (!checkStoragePermission()) {
                requestStoragePermission();
            }
            else {
                pickGallery();
            }
        });

        cam.setOnClickListener(v -> {
            if (!checkCameraPermission()) {
                requestCameraPermission();
            }
            else {
                pickCamera();
            }
        });

        videopost.setOnClickListener(v -> {
            if (!checkCameraPermission()) {
                requestCameraPermission();
            }
            else {
                pickVideo();
            }
        });

        videocam.setOnClickListener(v -> {
            if (!checkCameraPermission()) {
                requestCameraPermission();
            }
            else {
                pickVideoCam();
            }
        });

        ///////////////////////IMAGE HANDLING////////////////////////

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        ///////////////////////POST////////////////////////
        post.setOnClickListener(v -> {
            if(InternetConnection.checkConnection(getApplicationContext())){
                String text_content = postcontent.getText().toString();
                String headline = head_content.getText().toString();

                if(introPref.getType().matches("com") && (imagelist.size() == 0 && videoUri == null) && (headline.trim().isEmpty())) {
                    BasicUtility.showToast(getApplicationContext(),"Post has got no picture or video...");
                }
                else if(introPref.getType().matches("com") && (headline.trim().isEmpty()) && (imagelist.size()>0 || videoUri != null)) {
                    BasicUtility.showToast(getApplicationContext(),"Post has got no headline...");
                }
                else if(introPref.getType().matches("indi") && text_content.trim().isEmpty() && imagelist.size() == 0){
                    BasicUtility.showToast(getApplicationContext(),"Post has got nothing...");
                }
                else{
                    if(intent.getStringExtra("target")!=null && intent.getStringExtra("target").matches("100")){
                        progressDialog = new ProgressDialog(NewPostHome.this);
                        progressDialog.setTitle("Saving changes");
                        progressDialog.setMessage("Please wait...");
                        progressDialog.show();

                        docRef = firebaseFirestore.collection("Feeds").document(editPostModel.getDocID());

                        ts = Long.toString(editPostModel.getTs());

                        if(selected_tags!= null && selected_tags.size()>0 ) {
                            editPostModel.setTagL(selected_tags);
                        }
                        if(!text_content.isEmpty()) {
                            editPostModel.setTxt(text_content.trim());
                        }

                        if (imagelist != null && imagelist.size() > 0) {
                            for (int j = 0; j < imagelist.size(); j++) {
                                Long tsLong = System.currentTimeMillis();
                                ts = tsLong.toString();
                                StorageReference reference = storageReferenece.child("Feeds/").child(fireuser.getUid() +"_"+ ts + "post_img");

                                int finalJ = j;
                                reference.putBytes(imagelist.get(finalJ))
                                        .addOnSuccessListener(taskSnapshot ->
                                                reference.getDownloadUrl().addOnSuccessListener(uri -> {
                                                    downloadUri = uri;
                                                    generatedFilePath.add(downloadUri.toString());
                                                    Log.i("count", generatedFilePath.size() + " " + imagelist.size());
                                                    if (generatedFilePath.size() == imagelist.size()) {
                                                        homePostModel.setImg(generatedFilePath);
                                                        docRef.set(homePostModel).addOnCompleteListener(task -> {
                                                            if(task.isSuccessful()){
                                                                progressDialog.dismiss();
                                                                if(isTaskRoot()){
                                                                    startActivity(new Intent(NewPostHome.this, MainActivity.class));
                                                                }
                                                                else if(intent.getStringExtra("FromViewMoreHome")!=null){
                                                                    Intent i= new Intent(NewPostHome.this, ViewMoreHome.class);
                                                                    i.putExtra("username", editPostModel.getUsN());
                                                                    i.putExtra("userdp", editPostModel.getDp());
                                                                    i.putExtra("docID", editPostModel.getDocID());
                                                                    StoreTemp.getInstance().setTagTemp(editPostModel.getTagL());

                                                                    i.putExtra("likeL", editPostModel.getLikeL());
                                                                    i.putExtra("postPic", editPostModel.getImg());
                                                                    i.putExtra("postText", editPostModel.getTxt());
                                                                    i.putExtra("bool", "3");
                                                                    i.putExtra("commentNo", Long.toString(editPostModel.getCmtNo()));

                                                                    i.putExtra("uid", editPostModel.getUid());
                                                                    i.putExtra("timestamp", Long.toString(editPostModel.getTs()));
                                                                    i.putExtra("newTs", Long.toString(editPostModel.getNewTs()));
                                                                    startActivity(i);
                                                                    finish();

                                                                }
                                                                else {
                                                                    NewPostHome.super.onBackPressed();
                                                                }
                                                            } else {
                                                                BasicUtility.showToast(getApplicationContext(), "Something went wrong...");
                                                            }
                                                        });
                                                    }
                                                }))
                                        .addOnFailureListener(e -> {
                                            BasicUtility.showToast(getApplicationContext(), "Something went wrong");
                                            if (progressDialog != null)
                                                progressDialog.dismiss();
                                        });
                            }
                        }
                        else {
                            editPostModel.setImg(null);
                            docRef.set(editPostModel).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    progressDialog.dismiss();

                                    if (isTaskRoot()) {
                                        startActivity(new Intent(NewPostHome.this, MainActivity.class));
                                    } else if (intent.getStringExtra("FromViewMoreHome") != null) {
                                        Intent i = new Intent(NewPostHome.this, ViewMoreHome.class);
                                        i.putExtra("username", editPostModel.getUsN());
                                        i.putExtra("userdp", editPostModel.getDp());
                                        i.putExtra("docID", editPostModel.getDocID());
                                        StoreTemp.getInstance().setTagTemp(editPostModel.getTagL());

                                        i.putExtra("likeL", editPostModel.getLikeL());
                                        i.putExtra("postPic", editPostModel.getImg());
                                        i.putExtra("postText", editPostModel.getTxt());
                                        i.putExtra("bool", "3");
                                        i.putExtra("commentNo", Long.toString(editPostModel.getCmtNo()));

                                        i.putExtra("uid", editPostModel.getUid());
                                        i.putExtra("timestamp", Long.toString(editPostModel.getTs()));
                                        i.putExtra("newTs", Long.toString(editPostModel.getNewTs()));
                                        startActivity(i);
                                        finish();

                                    } else {
                                        NewPostHome.super.onBackPressed();
                                    }

                                } else {
                                    BasicUtility.showToast(getApplicationContext(), "Something went wrong.");

                                }
                            });
                        }
                    }
                    else {
                        long timestampLong = System.currentTimeMillis();
//                        ts = tsLong.toString();
                        progressDialog = new ProgressDialog(NewPostHome.this);
                        progressDialog.setTitle("Uploading");
                        progressDialog.setMessage("Please wait...");
                        progressDialog.show();

                        if(videoUri != null) {
                            docRef = firebaseFirestore.collection("Reels").document(String.valueOf(timestampLong));

                            reelsPostModel.setCommittee_name(introPref.getFullName());
                            reelsPostModel.setCommittee_dp(introPref.getUserdp());
                            reelsPostModel.setTs(timestampLong);

                            reelsPostModel.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            if(duration < 10) {
                                reelsPostModel.setDuration("0:0" + duration);
                            } else {
                                reelsPostModel.setDuration("0:" + duration);
                            }

                            if (!text_content.isEmpty()) {
                                reelsPostModel.setDescription(text_content.trim());
                            }

                            if(!head_content.getText().toString().isEmpty()) {
                                reelsPostModel.setHeadline(head_content.getText().toString().trim());
                            }

                            Long tsLong = System.currentTimeMillis();
                            ts = tsLong.toString();
                            StorageReference referenceVideo = storageReferenece.child("Reels/").child("Videos").child(fireuser.getUid() + "_" + ts + "post_vid");
                            StorageReference referenceImage = storageReferenece.child("Reels/").child("Images").child(fireuser.getUid() + "_" + ts + "post_img");

                            referenceImage.putBytes(frame)
                                    .addOnCompleteListener(task -> {
                                        if(task.isSuccessful()) {
                                            referenceImage.getDownloadUrl().addOnCompleteListener(task1 -> {
                                                reelsPostModel.setFrame(Objects.requireNonNull(task1.getResult()).toString());
                                            });
                                        }
                                        else {
                                            BasicUtility.showToast(getApplicationContext(), "Something went wrong");
                                        }
                                    });

                            referenceVideo.putFile(videoUri)
                                    .addOnSuccessListener(taskSnapshot ->
                                            referenceVideo.getDownloadUrl().addOnSuccessListener(uri -> {
                                                downloadUri = uri;
                                                generatedFilePath.add(downloadUri.toString());
                                                reelsPostModel.setVideo(generatedFilePath.get(0));
                                                docRef.set(reelsPostModel).addOnCompleteListener(task -> {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(getApplicationContext(), "Post Created", Toast.LENGTH_LONG).show();
                                                        progressDialog.dismiss();
                                                        if (isTaskRoot()) {
                                                            startActivity(new Intent(NewPostHome.this, MainActivity.class));
                                                            finish();
                                                        } else {
                                                            NewPostHome.super.onBackPressed();
                                                        }
                                                    } else {
                                                        BasicUtility.showToast(getApplicationContext(), "Something went wrong...");
                                                    }
                                                });
                                            }))
                                    .addOnFailureListener(e -> {
                                        BasicUtility.showToast(getApplicationContext(), "Something went wrong");
                                        if (progressDialog != null)
                                            progressDialog.dismiss();
                                    });
                        }
                        else {
                            docRef = firebaseFirestore.collection("Feeds").document();

                            homePostModel.setUsN(introPref.getFullName());

                            homePostModel.setDp(introPref.getUserdp());
                            if(getIntent().getStringExtra("target")!= null){
                                if(getIntent().getStringExtra("target").matches("11")){
                                    homePostModel.setChallengeID(getIntent().getStringExtra("challengeID"));
                                }
                                if(getIntent().getStringExtra("target").matches("4")){
                                    homePostModel.setComID(getIntent().getStringExtra("comID"));
                                    homePostModel.setComName(getIntent().getStringExtra("comName"));
                                }
                            }

                            homePostModel.setTs(timestampLong);
                            homePostModel.setNewTs(timestampLong);
                            homePostModel.setType(introPref.getType());

                            homePostModel.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());

                            if(selected_tags!= null && selected_tags.size()>0 ) {
                                homePostModel.setTagL(selected_tags);
                            }
                            if(!text_content.isEmpty()) {
                                homePostModel.setTxt(text_content.trim());
                            }
                            if(!head_content.getText().toString().isEmpty()) {
                                homePostModel.setHeadline(head_content.getText().toString().trim());
                            }

                            if (imagelist != null && imagelist.size() > 0) {
                                for (int j = 0; j < imagelist.size(); j++) {
                                    Long tsLong = System.currentTimeMillis();
                                    ts = tsLong.toString();
                                    StorageReference reference = storageReferenece.child("Feeds/").child(fireuser.getUid() +"_"+ ts + "post_img");

                                    int finalJ = j;
                                    reference.putBytes(imagelist.get(finalJ))
                                            .addOnSuccessListener(taskSnapshot ->
                                                    reference.getDownloadUrl().addOnSuccessListener(uri -> {
                                                        downloadUri = uri;
                                                        generatedFilePath.add(downloadUri.toString());
                                                        Log.i("count", generatedFilePath.size() + " " + imagelist.size());
                                                        if (generatedFilePath.size() == imagelist.size()) {
                                                            homePostModel.setImg(generatedFilePath);
                                                            docRef.set(homePostModel).addOnCompleteListener(task -> {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(getApplicationContext(), "Post Created", Toast.LENGTH_LONG).show();
                                                                    progressDialog.dismiss();
                                                                    if (isTaskRoot()) {
                                                                        startActivity(new Intent(NewPostHome.this, MainActivity.class));
                                                                        finish();
                                                                    } else {
                                                                        NewPostHome.super.onBackPressed();
                                                                    }
                                                                } else {
                                                                    BasicUtility.showToast(getApplicationContext(), "Something went wrong...");
                                                                }
                                                            });
                                                        }
                                                    }))
                                            .addOnFailureListener(e -> {
                                                BasicUtility.showToast(getApplicationContext(), "Something went wrong");
                                                if (progressDialog != null)
                                                    progressDialog.dismiss();
                                            });
                                }
                            }
                            else {
                                docRef.set(homePostModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Post Created", Toast.LENGTH_LONG).show();
                                            progressDialog.dismiss();
                                            startActivity(new Intent(NewPostHome.this, MainActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Post creation failed", Toast.LENGTH_LONG).show();
                                            progressDialog.dismiss();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), "Post creation failed", Toast.LENGTH_LONG).show();
                                        progressDialog.dismiss();
                                    }
                                });
                            }
                        }
                    }
                }
            }
            else {
                BasicUtility.showToast(getApplicationContext(), "Network unavailable...");
            }
        });

        customTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        cross.setOnClickListener(v -> {
            String text_content = postcontent.getText().toString();

            if(text_content.isEmpty() && pic==null){
                if(isTaskRoot()){
                    startActivity(new Intent(NewPostHome.this, MainActivity.class));
                    finish();
                }else {
                    super.onBackPressed();
                }
            }
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(NewPostHome.this);
                builder.setTitle("Are you sure?")
                        .setMessage("Changes will be discarded...")
                        .setPositiveButton("Sure", (dialog, which) -> {
                            if(isTaskRoot()){
                                startActivity(new Intent(NewPostHome.this, MainActivity.class));
                                finish();
                            }else {
                                super.onBackPressed();
                            }
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .setCancelable(true)
                        .show();
            }

        });


        tagPujo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(NewPostHome.this, ActivityTagPujo.class);
                startActivityForResult(intent1, TAG_PUJO);
            }
        });

    }

    ////////////TAGS////////////////
    private void openDialog() {
        AlertDialog.Builder dialog= new AlertDialog.Builder(NewPostHome.this);
        LayoutInflater inflater= LayoutInflater.from(NewPostHome.this);
        View view=inflater.inflate(R.layout.dialog_tag_spinner,null);
        edtagtxt =view.findViewById(R.id.addtag);
        dialog.setView(view)
                .setTitle("Add Tag")
                .setNegativeButton("Cancel", (dialog12, which) ->
                        dialog12.dismiss())
                .setPositiveButton("Done", (dialog1, which) -> {
                    textdata = edtagtxt.getText().toString().trim();
                    if(textdata.isEmpty()){
                        Toast.makeText(getApplicationContext(), "Empty tag", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        edtagtxt.setText("");

                        ArrayList<String> TagColorArray = new ArrayList<>();
                        TagColorArray.add("#f4b4ff");
                        TagColorArray.add("#aaf1ff");
                        TagColorArray.add("#ffdfad");
                        TagColorArray.add("#bcffa2");
                        TagColorArray.add("#cecbff");
                        TagColorArray.add("#cfffef");
                        TagColorArray.add("#ffc0bd");
                        TagColorArray.add("#faff9c");
                        TagColorArray.add("#7efdff");
                        TagColorArray.add("#ffe87b");

                        int pos= (int) (Math.random()* 10);
                        colorValue= TagColorArray.get(pos);

                        TagModel mytag = new TagModel();
                        mytag.setName_tag(textdata);
                        mytag.setColor_hex(colorValue);
                        selected_tags.add(mytag);

                        tagAdapter2.notifyDataSetChanged();
                        tags_selectedRecycler.setVisibility(View.VISIBLE);
                        Toast.makeText(NewPostHome.this,"New Tag Added", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void buildRecyclerView_selectedtags(){
        tags_selectedRecycler.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        tags_selectedRecycler.setLayoutManager(linearLayoutManager);
        tags_selectedRecycler.setItemAnimator(new DefaultItemAnimator());
      //  selected_tags = new ArrayList<>();

        tagAdapter2 = new TagAdapter(selected_tags, getApplicationContext());
        tags_selectedRecycler.setAdapter(tagAdapter2);

        tagAdapter2.onClickListener((position, tag, color) -> {
            Toast.makeText(getApplicationContext(), "Long Press to remove tag", Toast.LENGTH_SHORT).show();
        });
        tagAdapter2.onLongClickListener((position, tag_name, tag_color) ->{
            TagModel tagModel = new TagModel();
            tagModel.setName_tag(tag_name);
            tagModel.setColor_hex(tag_color);

            selected_tags.remove(position);
            tagAdapter2.notifyItemRemoved(position);

            if(selected_tags.size()==0)
                tags_selectedRecycler.setVisibility(View.GONE);

            //   tagAdapter.notifyDataSetChanged();
        });

//        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
//            @Override
//            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
//                return false;
//            }
//
//            @Override
//            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//                selected_tags.remove(viewHolder.getAdapterPosition());
//                tagAdapter2.notifyItemRemoved(viewHolder.getAdapterPosition());
//            }
//        });
//        helper.attachToRecyclerView(tags_selectedRecycler);

    }


    @Override
    public void onTagClicked(TagModel tagModel) {
//        tags_selectedRecycler.setVisibility(View.VISIBLE);
//        selected_tags.add(tagModel);
//        tagAdapter2.notifyDataSetChanged();
    }
    ////////////TAGS////////////////

    ///////////////////////HANDLE CAMERA AND GALLERY//////////////////////////
    private void pickGallery(){
        Intent intent= new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Image"),IMAGE_PICK_GALLERY_CODE);
    }

    private void pickVideo(){
        Intent intent= new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Video"), VIDEO_PICK_GALLERY_CODE);
    }

    private void pickCamera() {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
        }
    }

    private void pickVideoCam() {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, VIDEO_PICK_CAMERA_CODE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            if(requestCode == VIDEO_PICK_GALLERY_CODE) {
                videoUri = data.getData();
                BasicUtility.saveVideo(videoUri, NewPostHome.this);

                final String[] filePath = {getExternalFilesDir(null) + "/Utsav/" + "VID-" + tsLong + ".mp4"};
                final ProgressDialog[] progressDialog = new ProgressDialog[1];

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(NewPostHome.this, videoUri);
                String mVideoDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long mTimeInMilliseconds= Long.parseLong(Objects.requireNonNull(mVideoDuration));
                duration = (int)mTimeInMilliseconds/1000;
                Bitmap bitmap = retriever.getFrameAtTime(2000);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Objects.requireNonNull(bitmap).compress(Bitmap.CompressFormat.JPEG, 100, out);
                frame = out.toByteArray();

                final long[] size = {new File(filePath[0]).length() / (1024 * 1024)};

                if(size[0] < 100) {
                    VideoCompress.compressVideoLow(filePath[0], filePath[0].replace(".mp4", "_compressed.mp4"), new VideoCompress.CompressListener() {
                        @Override
                        public void onStart() {
                            //Start Compress
                            progressDialog[0] = new ProgressDialog(NewPostHome.this);
                            progressDialog[0].setTitle("Uploading your video");
                            progressDialog[0].setMessage("Please wait...");
                            progressDialog[0].setCancelable(false);
                            progressDialog[0].show();
                        }

                        @Override
                        public void onSuccess() {
                            //Finish successfully
//                            filePath[0] = filePath[0].replace(".mp4", "_compressed.mp4");
//                            size[0] = new File(filePath[0]).length()/(1024*1024);
                            if(progressDialog[0] != null && progressDialog[0].isShowing()) {
                                progressDialog[0].dismiss();
                            }

                            videoframe.setVisibility(View.VISIBLE);
                            videoView.setVideoURI(Uri.fromFile(new File(filePath[0].replace(".mp4", "_compressed.mp4"))));
                            videoView.start();
                            videoUri = Uri.fromFile(new File(filePath[0].replace(".mp4", "_compressed.mp4")));

                            MediaController mediaController = new MediaController(NewPostHome.this);
                            videoView.setMediaController(mediaController);
                            mediaController.setAnchorView(videoView);
                        }

                        @Override
                        public void onFail() {
                            //Failed
                        }

                        @Override
                        public void onProgress(float percent) {
                            //Progress
                        }
                    });
                }
                else {
                    BasicUtility.showToast(getApplicationContext(), "Video size too large");
                }
            }

            else if(requestCode == IMAGE_PICK_GALLERY_CODE) {
                if(data.getClipData()!= null)
                {
                    int count = data.getClipData().getItemCount();
                    for (int i =0; i < count; i++)
                    {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
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

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        compressedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] pic = stream.toByteArray();
                        compressedBitmap.recycle();
                        imagelist.add(pic);

                        if(imagelist != null && imagelist.size()>0){

                            container_image.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.VISIBLE);
                            recyclerView.setHasFixedSize(false);
                            final LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                            recyclerView.setLayoutManager(layoutManager);
                            recyclerView.setItemViewCacheSize(20);
                            recyclerView.setNestedScrollingEnabled(true);

                            MultipleImageAdapter multipleImageAdapter = new MultipleImageAdapter(imagelist, getApplicationContext());
                            recyclerView.setAdapter(multipleImageAdapter);
                            multipleImageAdapter.onClickListener(new MultipleImageAdapter.OnClickListener() {
                                @Override
                                public void onClickListener(int position) {
                                    imagelist.remove(position);
                                    multipleImageAdapter.notifyDataSetChanged();
                                }
                            });

                        }
                        else {
                            container_image.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.GONE);
                        }

                    }
                }
                else if (data.getData() != null)
                {
                    Bitmap bitmap = null;
                    Bitmap compressedBitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        compressedBitmap = BasicUtility.decodeSampledBitmapFromFile(bitmap, 612, 816);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    compressedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    compressedBitmap.recycle();
                    imagelist.add(byteArray);
                    if(imagelist != null && imagelist.size()>0){

                        container_image.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);
                        recyclerView.setHasFixedSize(false);
                        final LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setItemViewCacheSize(20);
                        recyclerView.setNestedScrollingEnabled(true);

                        MultipleImageAdapter multipleImageAdapter = new MultipleImageAdapter(imagelist, getApplicationContext());
                        recyclerView.setAdapter(multipleImageAdapter);
                        multipleImageAdapter.onClickListener(new MultipleImageAdapter.OnClickListener() {
                            @Override
                            public void onClickListener(int position) {
                                imagelist.remove(position);
                                multipleImageAdapter.notifyDataSetChanged();
                            }
                        });

                    }
                    else {
                        container_image.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                    }
                }

            }

            else if(requestCode == IMAGE_PICK_CAMERA_CODE){

                Bundle extras = data.getExtras();
                Bitmap bitmap = (Bitmap) extras.get("data");
                Bitmap compressedBitmap = null;

                try {
                    compressedBitmap = BasicUtility.decodeSampledBitmapFromFile(bitmap, 612, 816);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                compressedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                compressedBitmap.recycle();

                imagelist.add(byteArray);
                if(imagelist != null && imagelist.size()>0){

                    container_image.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView.setHasFixedSize(false);
                    final LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setItemViewCacheSize(20);
                    recyclerView.setNestedScrollingEnabled(true);

                    MultipleImageAdapter multipleImageAdapter = new MultipleImageAdapter(imagelist, getApplicationContext());
                    recyclerView.setAdapter(multipleImageAdapter);
                    multipleImageAdapter.onClickListener(new MultipleImageAdapter.OnClickListener() {
                        @Override
                        public void onClickListener(int position) {
                            imagelist.remove(position);
                            multipleImageAdapter.notifyDataSetChanged();
                        }
                    });

                }
                else {
                    container_image.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                }
            }

            else if(requestCode == VIDEO_PICK_CAMERA_CODE) {
                videoUri = data.getData();
                BasicUtility.saveVideo(videoUri, NewPostHome.this);

                final String[] filePath = {getExternalFilesDir(null) + "/Utsav/" + "VID-" + tsLong + ".mp4"};
                final ProgressDialog[] progressDialog = new ProgressDialog[1];

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(NewPostHome.this, videoUri);
                String mVideoDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long mTimeInMilliseconds= Long.parseLong(Objects.requireNonNull(mVideoDuration));
                duration = (int)mTimeInMilliseconds/1000;
                Bitmap bitmap = retriever.getFrameAtTime(2000);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Objects.requireNonNull(bitmap).compress(Bitmap.CompressFormat.JPEG, 100, out);
                frame = out.toByteArray();

                final long[] size = {new File(filePath[0]).length() / (1024 * 1024)};

                if(size[0] < 100) {
                    VideoCompress.compressVideoLow(filePath[0], filePath[0].replace(".mp4", "_compressed.mp4"), new VideoCompress.CompressListener() {
                        @Override
                        public void onStart() {
                            //Start Compress
                            progressDialog[0] = new ProgressDialog(NewPostHome.this);
                            progressDialog[0].setTitle("Uploading your video");
                            progressDialog[0].setMessage("Please wait...");
                            progressDialog[0].setCancelable(false);
                            progressDialog[0].show();
                        }

                        @Override
                        public void onSuccess() {
                            //Finish successfully
//                            filePath[0] = filePath[0].replace(".mp4", "_compressed.mp4");
//                            size[0] = new File(filePath[0]).length()/(1024*1024);
                            if(progressDialog[0] != null && progressDialog[0].isShowing()) {
                                progressDialog[0].dismiss();
                            }

                            videoframe.setVisibility(View.VISIBLE);
                            videoView.setVideoURI(Uri.fromFile(new File(filePath[0].replace(".mp4", "_compressed.mp4"))));
                            videoView.start();
                            videoUri = Uri.fromFile(new File(filePath[0].replace(".mp4", "_compressed.mp4")));

                            MediaController mediaController = new MediaController(NewPostHome.this);
                            videoView.setMediaController(mediaController);
                            mediaController.setAnchorView(videoView);
                        }

                        @Override
                        public void onFail() {
                            //Failed
                        }

                        @Override
                        public void onProgress(float percent) {
                            //Progress
                        }
                    });
                }
                else {
                    BasicUtility.showToast(getApplicationContext(), "Video size too large");
                }
            }

            else if(requestCode == TAG_PUJO){
                PujoTagModel pujoTag = new PujoTagModel();
                pujoTag.setPujoName(data.getStringExtra("name"));
                pujoTag.setPujoUid(data.getStringExtra("uid"));

                homePostModel.setPujoTag(pujoTag);
                editPostModel.setPujoTag(pujoTag);
                reelsPostModel.setPujoTag(pujoTag);

                tagPujo.setText(data.getStringExtra("name"));
            }

            ////////////////////////CROP//////////////////////
            else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                finalUri = result.getUri();

                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), finalUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos =new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                pic = baos.toByteArray();
                imageCompressor = new ImageCompressor(pic);
                imageCompressor.execute();

            }
            else {//CROP ERROR
                Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
            }
            ////////////////////////CROP//////////////////////

        }
    }
    ///////////////////////HANDLE CAMERA AND GALLERY///////////////////////////

    //////////////////////PREMISSIONS//////////////////////////
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(NewPostHome.this, storagePermission,STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission(){
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE )== (PackageManager.PERMISSION_GRANTED);
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(NewPostHome.this, cameraPermission,CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result= ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1= ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE )== (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case CAMERA_REQUEST_CODE:
                if(grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted) {
                        pickCamera();
                    }
                    else {
                        Toast.makeText(this,"permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case STORAGE_REQUEST_CODE:
                if(grantResults.length > 0){
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted) {
                        pickGallery();
                    }
                    else {
                        Toast.makeText(this,"permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    //////////////////////PREMISSIONS//////////////////////////

    class ImageCompressor extends AsyncTask<Void, Void, byte[]> {

        private final float maxHeight = 1080.0f;
        private final float maxWidth = 720.0f;
        private byte[] pic2;

        public ImageCompressor(byte[] pic) {
            this.pic2 = pic;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        public byte[] doInBackground(Void... strings) {
            Bitmap scaledBitmap = null;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Bitmap bmp = BitmapFactory.decodeByteArray(pic2, 0, pic2.length, options);

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
                bmp = BitmapFactory.decodeByteArray(pic2, 0, pic2.length, options);
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


            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, out);
            byte[] by = out.toByteArray();

            return by;
        }

        @Override
        protected void onPostExecute(byte[] picCompressed) {
            if(picCompressed!= null) {
                pic = picCompressed;
                imagelist.add(pic);

                if(imagelist != null && imagelist.size()>0){

                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView.setHasFixedSize(false);
                    final LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setItemViewCacheSize(20);
                    recyclerView.setNestedScrollingEnabled(true);

                    MultipleImageAdapter multipleImageAdapter = new MultipleImageAdapter(imagelist, getApplicationContext());
                    recyclerView.setAdapter(multipleImageAdapter);
                    multipleImageAdapter.onClickListener(new MultipleImageAdapter.OnClickListener() {
                        @Override
                        public void onClickListener(int position) {
                            imagelist.remove(position);
                            multipleImageAdapter.notifyDataSetChanged();
                        }
                    });

                }
                else {
                    recyclerView.setVisibility(View.GONE);
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
                inSampleSize = Math.min(heightRatio, widthRatio);
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
    public void onBackPressed() {
        String text_content = postcontent.getText().toString();

        if(text_content.isEmpty() && pic==null){
            if(isTaskRoot()){
                startActivity(new Intent(NewPostHome.this, MainActivity.class));
                finish();
            }else {
                super.onBackPressed();
            }
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(NewPostHome.this);
            builder.setTitle("Are you sure?")
                    .setMessage("Changes will be discarded...")
                    .setPositiveButton("Sure", (dialog, which) -> {
                        if(isTaskRoot()){
                            startActivity(new Intent(NewPostHome.this, MainActivity.class));
                            finish();
                        }else {
                            super.onBackPressed();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .setCancelable(true)
                    .show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(imageCompressor != null) {
            imageCompressor.cancel(true);
        }
    }

}