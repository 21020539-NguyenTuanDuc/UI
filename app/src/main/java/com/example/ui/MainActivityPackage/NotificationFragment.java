package com.example.ui.MainActivityPackage;

import static java.lang.System.exit;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ui.Adapter.NotificationAdapter;
import com.example.ui.Adapter.NotificationModelAdapter;
import com.example.ui.MainActivity;
import com.example.ui.Model.NotificationModel;
import com.example.ui.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class NotificationFragment extends Fragment {
    private RecyclerView recyclerViewNotification;
    private FirebaseFirestore db;
    private NotificationModelAdapter notificationModelAdapter;
    private SweetAlertDialog sweetAlertDialog;
    String language = Locale.getDefault().getLanguage();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ActionBar actionBar = ((MainActivity) requireActivity()).getSupportActionBar();
        actionBar.show();
        actionBar.setTitle(R.string.notification);
        return inflater.inflate(R.layout.fragment_notifcation, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        sweetAlertDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();
        getNotificationModelList();
        sweetAlertDialog.dismissWithAnimation();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        notificationModelAdapter = new NotificationModelAdapter(getContext());

        sweetAlertDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();
        getNotificationModelList();

        recyclerViewNotification = view.findViewById(R.id.notificationRecyclerView);

        recyclerViewNotification.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewNotification.setAdapter(notificationModelAdapter);
        sweetAlertDialog.dismissWithAnimation();
    }

    private void getNotificationModelList() {
        db = FirebaseFirestore.getInstance();
        List<NotificationModel> notificationModelListToday = new ArrayList<>();
        List<NotificationModel> notificationModelListPrevious = new ArrayList<>();
        List<NotificationModel> notificationModelList = new ArrayList<>();
        List<String> timeMarks = new ArrayList<>();
//        List<Boolean> notifications = new ArrayList<>();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Date currentDate = new Date();

        db.collection("Notification")
                .whereEqualTo("user_id", currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (DocumentSnapshot doc: task.getResult()) {
                            List<String> description;
                            description = (List<String>) doc.get("description");
//                            if (language.equals("vi")) {
//                                description = (List<String>) doc.get("description");
//                            } else if (language.equals("en")) {
//                                description = (List<String>) doc.get("description_en");
//                            } else if (language.equals("ja")) {
//                                description = (List<String>) doc.get("description_ja");
//                            } else  {
//                                description = (List<String>) doc.get("description_zh");
//                            }
                            NotificationModel notificationModel =
                                    new NotificationModel(doc.getId(),
                                            doc.getString("image_path"),
                                            description,
                                            doc.getString("user_id"),
                                            doc.getBoolean("seen"),
                                            doc.getBoolean("sentNotification"),
                                            doc.getTimestamp("time"));
                            if (notificationModel.getTime().toDate().compareTo(currentDate) > 0) {
                                sweetAlertDialog.dismissWithAnimation();
                                Toast.makeText(getContext(),
                                        "There is a notification coming from future. Please check again",
                                        Toast.LENGTH_SHORT);
                                Log.e("Future notification", notificationModel.getId());
                                exit(0);
                            } else if (notificationModel.isSameday(currentDate)) {
                                notificationModelListToday.add(notificationModel);
                            } else {
                                notificationModelListPrevious.add(notificationModel);
                            }
                            timeMarks.add("");
                        }

                        timeMarks.add(0, getString(R.string.today));
//                        notificationModelList.add(null);
                        notificationModelList.add(new NotificationModel());
                        Collections.sort(notificationModelListToday, new Comparator<NotificationModel>() {
                            public int compare(NotificationModel o1, NotificationModel o2) {
                                return o2.getTime().compareTo(o1.getTime());
                            }
                        });
//                        notificationModelAdapterToday.setNotificationModelList(notificationModelListToday);
                        notificationModelList.addAll(notificationModelListToday);

                        timeMarks.add(notificationModelList.size(), getString(R.string.before));
//                        notificationModelList.add(null);
                        notificationModelList.add(new NotificationModel());
                        Collections.sort(notificationModelListPrevious, new Comparator<NotificationModel>() {
                            @Override
                            public int compare(NotificationModel o1, NotificationModel o2) {
                                return o2.getTime().compareTo(o1.getTime());
                            }
                        });
//                        notificationModelAdapterPrevious.setNotificationModelList(notificationModelListPrevious);
                        notificationModelList.addAll(notificationModelListPrevious);

                        Log.d("notificationModelList", Integer.toString(notificationModelList.size()));

                        notificationModelAdapter.setNotificationModelList(notificationModelList);
                        notificationModelAdapter.setTimeMarks(timeMarks);

//                        Log.d("current time", new Date().toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "The server is experiencing an error. Please come back later", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}