package com.example.ui.MainActivityPackage;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.ui.Helper.NewsHelper;
import com.example.ui.IntroContentActivity;
import com.example.ui.MainActivity;
import com.example.ui.MapActivity;
import com.example.ui.Model.ScoreModel;
import com.example.ui.NewsEventsActivity;
import com.example.ui.Quiz.GiftActivity;
import com.example.ui.Quiz.QuizActivity;
import com.example.ui.R;
import com.example.ui.TicketHandler.TicketActivity;
import com.example.ui.Utils;
import com.example.ui.databinding.FragmentHomeBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    private NewsHelper newsHelper;
    public String language = Locale.getDefault().getLanguage();
    SweetAlertDialog sweetAlertDialog;

    public static ScoreModel scoreModel;

    @Override
    public void onResume() {
        super.onResume();
        if (scoreModel != null) {
            binding.coinLayout.coin.setText(Integer.toString(
                    scoreModel.getScore()
            ));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).hide();

        sweetAlertDialog = new SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.setCancelable(true);
        sweetAlertDialog.show();

        getScore(FirebaseAuth.getInstance().getCurrentUser().getUid());
        newsHelper = new NewsHelper();
        initNews();
        binding = FragmentHomeBinding.bind(rootView);
        getIntroContentView();
        binding.ticketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.ticketButton.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        binding.ticketButton.animate().scaleX(1f).scaleY(1f).setDuration(100);
                    }
                });
                Intent intent = new Intent(getContext(), TicketActivity.class);
//                Intent intent = new Intent(getContext(), BoughtTicketActivity.class);
                startActivity(intent);
                requireActivity().overridePendingTransition(R.anim.animate_zoom_enter, R.anim.animate_zoom_exit);
            }
        });

        Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).hide();

        return rootView;
    }

    public void getScore(String user_id) {
        Log.e("user_id", user_id);
        FirebaseFirestore.getInstance().collection("Score")
                .document(user_id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Long score = documentSnapshot.getLong("score");
                        if (score == null) {
                            score = Long.valueOf(0);
                        }
                        scoreModel = new ScoreModel(user_id, score.intValue());
                        Utils.updateScore(scoreModel);

                        binding.coinLayout.coin.setText(Integer.toString(
                                scoreModel.getScore()
                        ));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("get score failed", user_id);
                    }
                });
    }

    protected void getIntroContentView() {
        binding.homeHeaderLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.homeHeaderLogo.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        binding.homeHeaderLogo.animate().scaleX(1f).scaleY(1f).setDuration(100);
                    }
                });
                Intent intent = new Intent(getContext(), QuizActivity.class);
                startActivity(intent);
            }
        });
        binding.coinLayout.coin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.coinLayout.coin.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        binding.coinLayout.coin.animate().scaleX(1f).scaleY(1f).setDuration(100);
                    }
                });
                Intent intent = new Intent(getContext(), GiftActivity.class);
                startActivity(intent);
            }
        });
        binding.coinLayout.arrow.setVisibility(View.VISIBLE);
        binding.homeMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.homeMap.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        binding.homeMap.animate().scaleX(1f).scaleY(1f).setDuration(100);
                    }
                });
                Intent intent = new Intent(getContext(), MapActivity.class);
                startActivity(intent);
            }
        });
        binding.homeIntroMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.homeIntroMore.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        binding.homeIntroMore.animate().scaleX(1f).scaleY(1f).setDuration(100);
                    }
                });
                Intent intent = new Intent(getContext(), IntroContentActivity.class);
                intent.putExtra("heading", "");
                startActivity(intent);
            }
        });
        binding.introCardview1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.introCardview1.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        binding.introCardview1.animate().scaleX(1f).scaleY(1f).setDuration(100);
                    }
                });
                Intent intent = new Intent(getContext(), IntroContentActivity.class);
                intent.putExtra("heading", "Chienluoc");
                startActivity(intent);
            }
        });
        binding.introCardview2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.introCardview2.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        binding.introCardview2.animate().scaleX(1f).scaleY(1f).setDuration(100);
                    }
                });
                Intent intent = new Intent(getContext(), IntroContentActivity.class);
                intent.putExtra("heading", "Lichsu");
                startActivity(intent);
            }
        });
        binding.introCardview3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.introCardview3.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        binding.introCardview3.animate().scaleX(1f).scaleY(1f).setDuration(100);
                    }
                });
                Intent intent = new Intent(getContext(), IntroContentActivity.class);
                intent.putExtra("heading", "Khonggian");
                startActivity(intent);
            }
        });
        binding.introCardview4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.introCardview4.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        binding.introCardview4.animate().scaleX(1f).scaleY(1f).setDuration(100);
                    }
                });
                Intent intent = new Intent(getContext(), IntroContentActivity.class);
                intent.putExtra("heading", "Nhansu");
                startActivity(intent);
            }
        });
        binding.introCardview5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.introCardview5.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        binding.introCardview5.animate().scaleX(1f).scaleY(1f).setDuration(100);
                    }
                });
                Intent intent = new Intent(getContext(), IntroContentActivity.class);
                intent.putExtra("heading", "Hoptac");
                startActivity(intent);
            }
        });
    }

    public void initNews() {
        newsHelper.fetchLatestNews(new NewsHelper.NewsDataCallback() {
            @Override
            public void onDataLoaded(List<DocumentSnapshot> newsList) {
                System.out.println(newsList.size());
                updateCardViews(newsList);
                sweetAlertDialog.dismiss();
            }
        });
    }

    private void updateCardViews(List<DocumentSnapshot> newsList) {
        View view = getView();
        // Iterate through the list of news and update CardViews
        for (int i = 0; i < Math.min(newsList.size(), 6); i++) {
            DocumentSnapshot document = newsList.get(i);
            Log.d("abc", document.getString("title"));

            // Update your CardView with Firestore data
            updateCardView(document, i + 1); // Assuming i + 1 is the correct index for your CardViews
        }
        for (int i = newsList.size(); i < 6; ++i) {
            CardView cardView = view.findViewById(getCardViewId(i + 1));
            cardView.setVisibility(View.GONE);
        }
    }

    private void updateCardView(DocumentSnapshot document, int cardIndex) {
        // Update your CardView with Firestore data
        View view = getView();
        if (view != null) {
            CardView cardView = view.findViewById(getCardViewId(cardIndex));
            ImageView img = cardView.findViewById(getImageId(cardIndex));
            TextView text = cardView.findViewById(getTextId(cardIndex));

            String title;
            List<String> description;
            if (language.equals("vi")) {
                title = document.getString("title");
                description = (List<String>) document.get("description");
            } else if (language.equals("en")) {
                title = document.getString("title_en");
                description = (List<String>) document.get("description_en");
            } else if (language.equals("ja")) {
                title = document.getString("title_ja");
                description = (List<String>) document.get("description_ja");
            } else {
                title = document.getString("title_zh");
                description = (List<String>) document.get("description_zh");
            }
            String imageName = document.getString("image_path");
            String time = Utils.formatDate(document.getTimestamp("time"));

            text.setText(title);
            StorageReference imageRef = FirebaseStorage.getInstance("gs://ui-123456.appspot.com").getReference().child("newsevents_images").child(imageName);
            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    if (uri != null) {
                        Glide.with(requireContext()).load(uri).into(img);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("Firebase Storage", "Error downloading image: " + e.getMessage());
                }
            });
//            // Sử dụng Glide để tải và hiển thị ảnh
//            Glide.with(this)
//                    .load(imageUrl)
//                    .into(img);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cardView.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            cardView.animate().scaleX(1f).scaleY(1f).setDuration(100);
                        }
                    });
                    Intent intent = new Intent(getContext(), NewsEventsActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("title", title);
                    bundle.putStringArrayList("description", (ArrayList<String>) description);
                    bundle.putString("time", time);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
        }
    }

    // Helper methods to get IDs based on card index
    private int getCardViewId(int index) {
        return getResources().getIdentifier("new_cardview" + index, "id", requireActivity().getPackageName());
    }

    private int getImageId(int index) {
        return getResources().getIdentifier("new_img" + index, "id", requireActivity().getPackageName());
    }

    private int getTextId(int index) {
        return getResources().getIdentifier("new_text" + index, "id", requireActivity().getPackageName());
    }

}
