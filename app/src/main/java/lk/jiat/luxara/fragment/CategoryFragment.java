package lk.jiat.luxara.fragment;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

import lk.jiat.luxara.R;
import lk.jiat.luxara.adapter.CategoryAdapter;
import lk.jiat.luxara.databinding.FragmentCategoryBinding;
import lk.jiat.luxara.model.Category;
import lombok.NonNull;

public class CategoryFragment extends Fragment {
    private FragmentCategoryBinding binding;
    private CategoryAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentCategoryBinding.inflate(inflater, container, false);


        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@androidx.annotation.NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerViewCategories.setLayoutManager(new GridLayoutManager(getContext(),3));
        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        Category c1 = new Category("cat1","Men","");
//        Category c2 = new Category("cat2","Women","");
//        Category c3 = new Category("cat3","Kids","");
//        Category c4 = new Category("cat4","Shoes","");
//        Category c5 = new Category("cat5","Accessories","");
//        Category c6 = new Category("cat6","New Arrivals","");
//        Category c7 = new Category("cat7","Trending","");
//        Category c8 = new Category("cat8","Offers","");
//
//        List<Category>cats = List.of(c1,c2,c3,c4,c5,c6,c7,c8);
//        WriteBatch batch = db.batch();
//
//        for (Category c: cats){
//         DocumentReference ref = db.collection("categories").document();
//         batch.set(ref,c);
//        }
//
//        batch.commit();
//        db.collection("categories").add()



        db.collection("categories").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@androidx.annotation.NonNull Task<QuerySnapshot> task) {
                            QuerySnapshot result = task.getResult();
//                        List<Category> categories  = result.toObjects(Category.class);
                       List<Category> categories = task.getResult().toObjects(Category.class);
                        adapter = new CategoryAdapter(categories,  category -> {


                            Bundle bundle = new Bundle();
                            bundle.putString("categoryId" , category.getCategoryId());


                            ListingFragment fragment = new ListingFragment();
                            fragment.setArguments(bundle);

                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        });


                        binding.recyclerViewCategories.setAdapter(adapter);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@androidx.annotation.NonNull Exception e) {
                        Log.e("Firestore","Error:"+e.getMessage());
                    }
                });
        getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

    }
}