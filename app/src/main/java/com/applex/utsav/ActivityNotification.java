package com.applex.utsav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.applex.utsav.adapters.NotifAdapter;
import com.applex.utsav.models.NotifModel;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.utility.BasicUtility;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import static java.lang.Boolean.TRUE;

public class ActivityNotification extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView notifRecycler;
    private ProgressBar progressMore;
    private ShimmerFrameLayout shimmerFrameLayout;
    private ImageView noNotif;
    public static boolean active = false;
    private FirestorePagingAdapter adapter;

    private NotifAdapter notifAdapter;
    private DocumentSnapshot lastVisible;

    private int checkGetMore = -1;

    private ArrayList<NotifModel> notifModels;

    boolean loading = true;
    int pastVisiblesItems, visibleItemCount, totalItemCount;

    public static int removeNotif = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntroPref introPref = new IntroPref(ActivityNotification.this);
        String lang = introPref.getLanguage();
        Locale locale= new Locale(lang);
        Locale.setDefault(locale);
        Configuration config= new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        /////////////////DAY OR NIGHT MODE///////////////////
        if(introPref.getTheme() == 1) {
            FirebaseFirestore.getInstance().document("Mode/night_mode")
                    .get().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    if(task.getResult().getBoolean("night_mode")) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            });
        } else if(introPref.getTheme() == 2) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if(introPref.getTheme() == 3) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        /////////////////DAY OR NIGHT MODE///////////////////
        setContentView(R.layout.activity_notification);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        progressMore = findViewById(R.id.progress_more);
        progressMore.setVisibility(View.GONE);
        shimmerFrameLayout = findViewById(R.id.shimmerLayout);
        noNotif = findViewById(R.id.no_recent_notiff);
        notifRecycler = findViewById(R.id.recyclerNotif);

        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
        notifRecycler.setVisibility(View.GONE);

        notifRecycler.setHasFixedSize(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ActivityNotification.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        notifRecycler.setLayoutManager(linearLayoutManager);
        notifRecycler.setItemAnimator(new DefaultItemAnimator());
        notifRecycler.setItemViewCacheSize(10);
        notifModels = new ArrayList<>();

        if(introPref.getTheme() == 1) {
            FirebaseFirestore.getInstance().document("Users/"+ FirebaseAuth.getInstance().getUid())
                    .addSnapshotListener(ActivityNotification.this, (value, error) -> {
                        if(value != null) {
                            if(value.getBoolean("listener")) {
                                FirebaseFirestore.getInstance().document("Mode/night_mode")
                                        .get().addOnCompleteListener(task -> {
                                    if(task.isSuccessful()) {
                                        if(task.getResult().getBoolean("night_mode")) {
                                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                        } else {
                                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                        }
                                    } else {
                                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                    }
                                    new Handler().postDelayed(() -> {
                                        MainActivity.mode_changed = 1;
                                        FirebaseFirestore.getInstance().document("Users/"+ FirebaseAuth.getInstance().getUid()).update("listener", false);
                                        startActivity(new Intent(ActivityNotification.this, ActivityNotification.class));
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        finish();
                                    }, 200);
                                });
                            }
                        }
                    });
        }

        ///////////////Set Image Bitmap/////////////////////
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {

            Display display = getWindowManager().getDefaultDisplay();
            int displayWidth = display.getWidth();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), R.drawable.dark_mode_login, options);
            int width = options.outWidth;
            if (width > displayWidth) {
                int widthRatio = Math.round((float) width / (float) displayWidth);
                options.inSampleSize = widthRatio;
            }
            options.inJustDecodeBounds = false;
            Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.dark_mode_login, options);
            noNotif.setImageBitmap(scaledBitmap);
        }
        else if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
            Display display = getWindowManager().getDefaultDisplay();
            int displayWidth = display.getWidth();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), R.drawable.light_mode_login, options);
            int width = options.outWidth;
            if (width > displayWidth) {
                int widthRatio = Math.round((float) width / (float) displayWidth);
                options.inSampleSize = widthRatio;
            }
            options.inJustDecodeBounds = false;
            Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.light_mode_login, options);
            noNotif.setImageBitmap(scaledBitmap);
        }
        ///////////////Set Image Bitmap/////////////////////


        buildRecyclerView();

        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.darkpurple),getResources()
                .getColor(R.color.darkpurple));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            shimmerFrameLayout.startShimmer();
            notifRecycler.setVisibility(View.GONE);
            buildRecyclerView();
        });

        notifRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) { //check for scroll down
                    visibleItemCount = linearLayoutManager.getChildCount();
                    totalItemCount = linearLayoutManager.getItemCount();
                    pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition();

                    if (loading && checkGetMore != -1) {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            loading = false;
                            Log.v("...", "Last Item Wow !");
                            // Do pagination.. i.e. fetch new data
                            fetchMore();
                        }
                    }
                }
            }
        });

    }

    public void buildRecyclerView() {
        notifModels.clear();
        Query query = FirebaseFirestore.getInstance()
                .collection("Users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/Notifs/")
                .orderBy("ts", Query.Direction.DESCENDING)
                .limit(10);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        NotifModel notifModel = document.toObject(NotifModel.class);
                        notifModel.setDocID(document.getId());
                        notifModels.add(notifModel);
                    }

                    notifAdapter = new NotifAdapter(ActivityNotification.this, notifModels);
//                    notifAdapter.onClickListener(new NotifAdapter.OnClickListener() {
//                        @Override
//                        public void onClickListener(int value) {
//                            postMenuDialog = new BottomSheetDialog(ActivityNotification.this);
//                            postMenuDialog.setContentView(R.layout.dialog_notif);
//                            postMenuDialog.setCanceledOnTouchOutside(TRUE);
//
//                            postMenuDialog.findViewById(R.id.delete_notif).setOnClickListener(v -> {
//                                FirebaseFirestore.getInstance()
//                                        .collection("Users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/Notifs/").document(notifModels.get(value)
//                                        .getDocID()).delete()
//                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                            @Override
//                                            public void onSuccess(Void aVoid) {
//                                                postMenuDialog.dismiss();
//                                                notifModels.remove(value);
//                                                notifAdapter.notifyItemRemoved(value);
//                                            }
//                                        });
//                            });
//                            postMenuDialog.show();
//
//                        }
//                    });

                    notifRecycler.setAdapter(notifAdapter);

                    if(task.getResult().size()>0) {
                        lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);

                        if(notifModels.size()<10){
                            checkGetMore = -1;
                        }
                        else {
                            checkGetMore = 0;
                        }
                        noNotif.setVisibility(View.GONE);
                    }
                    else {
                        noNotif.setVisibility(View.VISIBLE);
                    }

                    if(swipeRefreshLayout.isRefreshing()){
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
                else {
                    if(swipeRefreshLayout.isRefreshing()){
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    BasicUtility.showToast(ActivityNotification.this, "Something went wrong...");
                }

                new Handler().postDelayed(() -> {
                    notifRecycler.setVisibility(View.VISIBLE);
                    progressMore.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                }, 250);
                if(swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    private void fetchMore(){

        progressMore.setVisibility(View.VISIBLE);
        Query nextQuery =  FirebaseFirestore.getInstance()
                .collection("Users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/Notifs/")
                .orderBy("ts", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(10);

        nextQuery.get().addOnCompleteListener(t -> {
            if (t.isSuccessful()) {
                ArrayList<NotifModel> notifModels2 = new ArrayList<>();
                for (DocumentSnapshot d : t.getResult()) {
                    NotifModel newNotifModel = d.toObject(NotifModel.class);
                    newNotifModel.setDocID(d.getId());
                    notifModels2.add(newNotifModel);
                }

                progressMore.setVisibility(View.GONE);
                if(notifModels2.size()>0){
                    int lastSize = notifModels.size();
                    notifModels.addAll(notifModels2);
                    notifAdapter.notifyItemRangeInserted(lastSize, notifModels2.size());
                    lastVisible = t.getResult().getDocuments().get(t.getResult().size() - 1);
                }

                if(notifModels.size()<10){
                    checkGetMore = -1;
                }
                else {
                    checkGetMore = 0;
                    loading = true;
                }
            }
        });
    }

//    public void buildRecyclerView() {
//        Query query = FirebaseFirestore.getInstance()
//                .collection("Users/" + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() + "/Notifs/")
//                .orderBy("ts", Query.Direction.DESCENDING);
//
//        PagedList.Config config = new PagedList.Config.Builder()
//                .setInitialLoadSizeHint(10)
//                .setPageSize(10)
//                .build();
//
//        FirestorePagingOptions<NotifModel> options = new FirestorePagingOptions.Builder<NotifModel>()
//                .setLifecycleOwner(this)
//                .setQuery(query, config, snapshot -> {
//                    NotifModel notifModel = snapshot.toObject(NotifModel.class);
//                    Objects.requireNonNull(notifModel).setDocID(snapshot.getId());
//                    return notifModel;
//                })
//                .build();
//
//        adapter = new FirestorePagingAdapter<NotifModel, ProgrammingViewHolder>(options) {
//            @NonNull
//            @Override
//            public ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
//                View v = layoutInflater.inflate(R.layout.item_notif, parent, false);
//                return new ProgrammingViewHolder(v);
//            }
//
//            @SuppressLint("SetTextI18n")
//            @Override
//            protected void onBindViewHolder(@NonNull ProgrammingViewHolder holder, int position, @NonNull NotifModel model) {
//
//                String userimage_url = model.getDp();
//                if(userimage_url!=null){
//                    Picasso.get().load(userimage_url).placeholder(R.drawable.ic_account_circle_black_24dp).into(holder.dp);
//                }
//                else{
//                    if(model.getGender()!=null){
//                        if (model.getGender().matches("Female") || model.getGender().matches("মহিলা")){
//                            holder.dp.setImageResource(R.drawable.ic_female);
//                        }
//                        else if (model.getGender().matches("Male") || model.getGender().matches("পুরুষ")){
//                            holder.dp.setImageResource(R.drawable.ic_male);
//                        }
//                        else if (model.getGender().matches("Others") || model.getGender().matches("অন্যান্য")){
//                            holder.dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
//                        }
//                    }
//                    else {
//                        holder.dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
//                    }
//                }
//
//                String titleString = "<b>"+model.getUsN()+"</b> "+model.getTitle();
//                holder.title.setText(Html.fromHtml(titleString));
//
//                holder.minsago.setText(BasicUtility.getTimeAgo(model.getTs()));
//
//                if(model.getTitle().contains("commented") || model.getTitle().contains("replied"))
//                {
//                    holder.bottomOfDp.setBackgroundResource(R.drawable.ic_conch_shell);
//                    holder.comment.setVisibility(View.VISIBLE);
//                    holder.comment.setText("\""+ model.getComTxt()+"\"");
//                }
//                if((model.getTitle().contains("liked")|| model.getTitle().contains("flamed")))
//                {
//                    holder.bottomOfDp.setBackgroundResource(R.drawable.ic_drum);
//                    holder.comment.setVisibility(View.GONE);
//                }
//                if(model.getTitle().contains("upvoted"))
//                {
//                    holder.bottomOfDp.setBackgroundResource(R.drawable.ic_baseline_stars_24);
//                    holder.comment.setVisibility(View.GONE);
//                }
//                if((model.getTitle().contains("liked")|| model.getTitle().contains("flamed")) && model.getTitle().contains("comment"))
//                {
//                    holder.bottomOfDp.setBackgroundResource(R.drawable.ic_drum);
//                    holder.comment.setVisibility(View.VISIBLE);
//                    holder.comment.setText("\""+ model.getComTxt()+"\"");
//                }
//
//                if(!model.isSeen()){
//                    holder.notifCard.setBackgroundColor(ActivityNotification.this.getResources().getColor(R.color.colorPrimaryLight));
//                }
//
//                holder.notifCard.setOnClickListener(v -> {
//                    String postID= model.getPostID();
//
//                    if(model.getBool() == 1){
//                        model.setSeen(true);
//                        FirebaseFirestore.getInstance()
//                                .document("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/Notifs/"+ model.getDocID()+"/")
//                                .update("seen", true).addOnCompleteListener(task -> {
//                                    if(!task.isSuccessful())
//                                        Log.d("Notif update", "updated"+ model.getDocID());
//                                });
//                        Intent i= new Intent(ActivityNotification.this, ViewMoreHome.class);
//                        i.putExtra("postID", postID);
//                        i.putExtra("type", model.getType());
//                        i.putExtra("from", "Image");
//                        i.putExtra("ts", Long.toString(model.getCom_ts()));
//                        i.putExtra("pCom_ts", Long.toString(model.getpCom_ts()));
//                        i.putExtra("gender",model.getGender());
//                        startActivity(i);
//                        notifyItemChanged(position);
//                    }
//
//                    else if(model.getBool() == 2){
//                        model.setSeen(true);
//                        FirebaseFirestore.getInstance()
//                                .document("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/Notifs/"+ model.getDocID()+"/")
//                                .update("seen", true).addOnCompleteListener(task -> {
//                                    if(!task.isSuccessful())
//                                        Log.d("Notif update", "updated"+ model.getDocID());
//                                });
//                        Intent i= new Intent(ActivityNotification.this, ViewMoreText.class);
//                        i.putExtra("postID", postID);
//                        i.putExtra("type", model.getType());
//                        i.putExtra("ts", Long.toString(model.getCom_ts()));
//                        i.putExtra("from", "Text");
//                        i.putExtra("pCom_ts", Long.toString(model.getpCom_ts()));
//                        i.putExtra("gender",model.getGender());
//
//                        startActivity(i);
//                        notifyItemChanged(position);
//                    }
//
//                    else if(model.getBool() == 3){
//                        model.setSeen(true);
//                        FirebaseFirestore.getInstance()
//                                .document("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/Notifs/"+ model.getDocID()+"/")
//                                .update("seen", true).addOnCompleteListener(task -> {
//                                    if(!task.isSuccessful())
//                                        Log.d("Notif update", "updated"+ model.getDocID());
//                                });
//                        Intent i= new Intent(ActivityNotification.this, ReelsActivity.class);
//                        i.putExtra("docID", postID);
//                        i.putExtra("bool", "1");
//                        i.putExtra("ts", Long.toString(model.getCom_ts()));
//                        i.putExtra("type", model.getType());
//                        i.putExtra("pCom_ts", Long.toString(model.getpCom_ts()));
//                        i.putExtra("gender",model.getGender());
//
//                        startActivity(i);
//                        notifyItemChanged(position);
//                    }
//                    else if(model.getTitle().contains("upvoted")){
//                        model.setSeen(true);
//                        FirebaseFirestore.getInstance()
//                                .document("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/Notifs/"+ model.getDocID()+"/")
//                                .update("seen", true).addOnCompleteListener(task -> {
//                                    if(!task.isSuccessful())
//                                        Log.d("Notif update", "updated"+ model.getDocID());
//                                });
////                        Intent i= new Intent(ActivityNotification.this, ActivityProfileCommittee.class);
////                        i.putExtra("uid", FirebaseAuth.getInstance().getUid());
////                        i.putExtra("to", "profile");
////                        startActivity(i);
//                        Intent i= new Intent(ActivityNotification.this, ActivityProfile.class);
//                        i.putExtra("uid", FirebaseAuth.getInstance().getUid());
//                        i.putExtra("to", "profile");
//                        startActivity(i);
//                        notifyItemChanged(position);
//                    }
//                });
//            }
//
//            @Override
//            public int getItemViewType(int position) {
//                return position;
//            }
//
//            @Override
//            protected void onLoadingStateChanged(@NonNull LoadingState state) {
//
//                super.onLoadingStateChanged(state);
//                switch (state) {
//                    case ERROR: BasicUtility.showToast(ActivityNotification.this, "Something went wrong..."); break;
//                    case LOADING_MORE: progressMore.setVisibility(View.VISIBLE); break;
//                    case LOADED:
//
//                        new Handler().postDelayed(() -> {
//                            notifRecycler.setVisibility(View.VISIBLE);
//                            progressMore.setVisibility(View.GONE);
//                            shimmerFrameLayout.stopShimmer();
//                            shimmerFrameLayout.setVisibility(View.GONE);
//                        }, 1000);
//                        if(swipeRefreshLayout.isRefreshing()) {
//                            swipeRefreshLayout.setRefreshing(false);
//                        }
//                        break;
//                    case FINISHED:
//                        shimmerFrameLayout.stopShimmer();
//                        shimmerFrameLayout.setVisibility(View.GONE);
//                        progressMore.setVisibility(View.GONE);
//                        if(swipeRefreshLayout.isRefreshing()) {
//                            swipeRefreshLayout.setRefreshing(false);
//                        }
//                        if(adapter!=null && adapter.getItemCount() == 0)
//                            noNotif.setVisibility(View.VISIBLE);
//                        break;
//                }
//            }
//        };
//
//        progressMore.setVisibility(View.GONE);
//        noNotif.setVisibility(View.GONE);
//        notifRecycler.setAdapter(adapter);
//
//    }
//
//    public static class ProgrammingViewHolder extends RecyclerView.ViewHolder{
//
//        ImageView dp, bottomOfDp, dots;
//        LinearLayout notifCard;
//        TextView title, minsago, comment;
//
//        ProgrammingViewHolder(@NonNull View itemView){
//            super(itemView);
//            dp = itemView.findViewById(R.id.dp);
//            bottomOfDp = itemView.findViewById(R.id.bottom_of_dp);
//            notifCard= itemView.findViewById(R.id.notif_card);
//            title= itemView.findViewById(R.id.notif_title);
//            minsago= itemView.findViewById(R.id.timestamp);
//            comment= itemView.findViewById(R.id.notif_comment);
////            dots = itemView.findViewById(R.id.notif_delete);
//        }
//    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        active = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(removeNotif > -1){
            FirebaseFirestore.getInstance()
                    .collection("Users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/Notifs/")
                    .document(notifModels.get(removeNotif).getDocID()).delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            notifModels.remove(removeNotif);
                            adapter.notifyItemRemoved(removeNotif);
                            removeNotif = -1;
                        }
                    });
        }
    }
}