package lk.jiat.luxara.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import lk.jiat.luxara.databinding.ActivitySignInBinding;

public class SignInActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private ActivitySignInBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();

        binding.signinBtnSignup.setOnClickListener(view -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        binding.signinBtnSignin.setOnClickListener(view -> {
            String email = binding.signinInputEmail.getText().toString().trim();
            String password = binding.signinInputPassword.getText().toString().trim();

            if (email.isEmpty()) {
                binding.signinInputEmail.setError("Email Is required !");
                binding.signinInputEmail.requestFocus();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.signinInputEmail.setError("Enter Valid Email !");
                binding.signinInputEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                binding.signinInputPassword.setError("Passsword Is required !");
                binding.signinInputPassword.requestFocus();
                return;
            }
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                updateUI(firebaseAuth.getCurrentUser());
                            } else {
                                Toast.makeText(SignInActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
//                                updateUI(null);
                            }
                        }
                    });
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
