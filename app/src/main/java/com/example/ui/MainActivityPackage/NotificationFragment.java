package com.example.ui.MainActivityPackage;

import static java.lang.System.exit;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ui.Adapter.NotificationModelAdapter;
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

import cn.pedant.SweetAlert.SweetAlertDialog;

public class NotificationFragment extends Fragment {
    private RecyclerView recyclerViewToday;
    private RecyclerView recyclerViewPrevious;
    private FirebaseFirestore db;
    private NotificationModelAdapter notificationModelAdapterToday;
    private NotificationModelAdapter notificationModelAdapterPrevious;
    private SweetAlertDialog sweetAlertDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notifcation, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        getNotificationModelList();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        notificationModelAdapterToday = new NotificationModelAdapter(getContext());
        notificationModelAdapterPrevious = new NotificationModelAdapter(getContext());

        sweetAlertDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();
        getNotificationModelList();

        recyclerViewToday = view.findViewById(R.id.notificationRecyclerViewToday);
        recyclerViewToday.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewToday.setAdapter(notificationModelAdapterToday);

        recyclerViewPrevious = view.findViewById(R.id.notificationRecyclerViewPrevious);
        recyclerViewPrevious.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewPrevious.setAdapter(notificationModelAdapterPrevious);
    }

    private void getNotificationModelList() {
        db = FirebaseFirestore.getInstance();
        List<NotificationModel> notificationModelListToday = new ArrayList<>();
        List<NotificationModel> notificationModelListPrevious = new ArrayList<>();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Date currentDate = new Date();

        db.collection("Notification")
                .whereEqualTo("user_id", currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (DocumentSnapshot doc: task.getResult()) {
//                            List<String> description = (List<String>) doc.get("description");
//                            Log.d("length discription", Integer.toString(description.size()));
//                            for (int i = 0; i < description.size(); ++i) {
//                                Log.d("doc" + Integer.toString(i), description.get(i));
//                            }
                            NotificationModel notificationModel =
                                    new NotificationModel(doc.getId(),
                                            doc.getString("image_path"),
                                            (List<String>) doc.get("description"),
                                            doc.getString("user_id"),
                                            doc.getBoolean("seen"),
                                            doc.getTimestamp("time"));
                            if (notificationModel.getTime().toDate().compareTo(currentDate) > 0) {
                                sweetAlertDialog.dismissWithAnimation();
                                Toast.makeText(getContext(),
                                        "There is a notification coming from future. Please check again",
                                        Toast.LENGTH_SHORT);
                                Log.e("Future notification", notificationModel.getId());
                                exit(0);
                            } else if (notificationModel.isSameday(currentDate, notificationModel.getTime().toDate())) {
                                notificationModelListToday.add(notificationModel);
                            } else {
                                notificationModelListPrevious.add(notificationModel);
                            }
                        }

                        Collections.sort(notificationModelListToday, new Comparator<NotificationModel>() {
                            public int compare(NotificationModel o1, NotificationModel o2) {
                                return o2.getTime().compareTo(o1.getTime());
                            }
                        });
                        notificationModelAdapterToday.setNotificationModelList(notificationModelListToday);

                        Collections.sort(notificationModelListPrevious, new Comparator<NotificationModel>() {
                            @Override
                            public int compare(NotificationModel o1, NotificationModel o2) {
                                return o2.getTime().compareTo(o1.getTime());
                            }
                        });
                        notificationModelAdapterPrevious.setNotificationModelList(notificationModelListPrevious);
//                        Log.d("current time", new Date().toString());
                        sweetAlertDialog.dismissWithAnimation();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        sweetAlertDialog.dismissWithAnimation();
                        Toast.makeText(getActivity(), "The server is experiencing an error. Please come back later", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}