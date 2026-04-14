/*
 * This file is part of Tack Android.
 *
 * Tack Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tack Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tack Android. If not, see http://www.gnu.org/licenses/.
 *
 * Copyright (c) 2020-2026 by Patrick Zedler
 */

package xyz.zedler.patrick.tack.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.android.material.snackbar.Snackbar;
import xyz.zedler.patrick.tack.R;
import xyz.zedler.patrick.tack.databinding.FragmentActivationBinding;
import xyz.zedler.patrick.tack.util.ActivationUtil;

public class ActivationFragment extends Fragment {

  private FragmentActivationBinding binding;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
  ) {
    binding = FragmentActivationBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    binding.buttonActivationActivate.setOnClickListener(v -> {
      String code = binding.editTextActivationCode.getText() != null
          ? binding.editTextActivationCode.getText().toString().trim() : "";
      if (code.isEmpty()) {
        binding.textInputActivation.setError(getString(R.string.activation_hint));
        return;
      }
      binding.textInputActivation.setError(null);
      if (ActivationUtil.activate(requireContext(), code)) {
        Snackbar.make(view, R.string.activation_success, Snackbar.LENGTH_SHORT).show();
        Navigation.findNavController(view)
            .navigate(R.id.action_activation_to_main);
      } else {
        binding.textInputActivation.setError(getString(R.string.activation_failed));
      }
    });
  }
}
