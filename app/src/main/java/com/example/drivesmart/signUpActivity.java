package com.example.drivesmart;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class signUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
    }

    public void showFragment1(){
        showNewFragmentInMainFragmentContrainerView(new Fragment1());

    }

    public void showFragment2(){
        showNewFragmentInMainFragmentContrainerView(new Fragment2());

    }

    private void showNewFragmentInMainFragmentContrainerView(Fragment newFragmentInstance) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.mainFragmentContainerView, newFragmentInstance);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

}