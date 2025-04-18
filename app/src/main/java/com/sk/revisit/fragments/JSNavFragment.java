package com.sk.revisit.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sk.revisit.databinding.NavJsBinding;

public class JSNavFragment extends Fragment {

    NavJsBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = NavJsBinding.inflate(getLayoutInflater());
    }
}
