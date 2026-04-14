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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.List;
import xyz.zedler.patrick.tack.BuildConfig;
import xyz.zedler.patrick.tack.Constants.PREF;
import xyz.zedler.patrick.tack.R;
import xyz.zedler.patrick.tack.activity.MainActivity;
import xyz.zedler.patrick.tack.behavior.ScrollBehavior;
import xyz.zedler.patrick.tack.behavior.SystemBarBehavior;
import xyz.zedler.patrick.tack.databinding.FragmentAboutBinding;
import xyz.zedler.patrick.tack.util.ActivationUtil;
import xyz.zedler.patrick.tack.util.ResUtil;
import xyz.zedler.patrick.tack.util.UnlockUtil;
import xyz.zedler.patrick.tack.util.ViewUtil;
import xyz.zedler.patrick.tack.util.dialog.TextDialogUtil;

public class AboutFragment extends BaseFragment implements OnClickListener {

  private FragmentAboutBinding binding;
  private MainActivity activity;
  private TextDialogUtil textDialogUtilMdc, textDialogUtilMds, textDialogUtilGoogleSansFlex;
  private int longClickCount = 0;
  private int versionClickCount = 0;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
  ) {
    binding = FragmentAboutBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    textDialogUtilMdc.dismiss();
    textDialogUtilMds.dismiss();
    textDialogUtilGoogleSansFlex.dismiss();
    binding = null;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    activity = (MainActivity) requireActivity();

    SystemBarBehavior systemBarBehavior = new SystemBarBehavior(activity);
    systemBarBehavior.setAppBar(binding.appBarAbout);
    systemBarBehavior.setScroll(binding.scrollAbout, binding.linearAboutContainer);
    systemBarBehavior.setUp();

    new ScrollBehavior().setUpScroll(
        binding.appBarAbout, binding.scrollAbout, ScrollBehavior.LIFT_ON_SCROLL
    );

    binding.buttonAboutBack.setOnClickListener(getNavigationOnClickListener());
    binding.buttonAboutMenu.setOnClickListener(v -> {
      performHapticClick();
      ViewUtil.showMenu(v, R.menu.menu_about, item -> {
        int id = item.getItemId();
        if (getViewUtil().isClickDisabled(id)) {
          return false;
        }
        performHapticClick();
        if (id == R.id.action_feedback) {
          activity.showFeedbackDialog();
        } else if (id == R.id.action_help) {
          activity.showHelpDialog();
        } else if (id == R.id.action_recommend) {
          String text = getString(R.string.msg_recommend, getString(R.string.app_vending_app));
          ResUtil.share(activity, text);
        }
        return true;
      });
    });
    ViewUtil.setTooltipText(binding.buttonAboutBack, R.string.action_back);
    ViewUtil.setTooltipText(binding.buttonAboutMenu, R.string.action_more);

    binding.textAboutVersion.setText(BuildConfig.VERSION_NAME);
    binding.textAboutVersion.setOnClickListener(v -> {
      versionClickCount++;
      if (versionClickCount >= 7) {
        versionClickCount = 0;
        showPasswordDialog();
      }
    });

    updateUnlockItem();

    textDialogUtilMdc = new TextDialogUtil(
        activity,
        R.string.license_material_components,
        R.raw.license_apache,
        R.string.license_material_components_link
    );
    textDialogUtilMdc.showIfWasShown(savedInstanceState);

    textDialogUtilMds = new TextDialogUtil(
        activity,
        R.string.license_material_icons,
        R.raw.license_apache,
        R.string.license_material_icons_link
    );
    textDialogUtilMds.showIfWasShown(savedInstanceState);

    textDialogUtilGoogleSansFlex = new TextDialogUtil(
        activity,
        R.string.license_google_sans_flex,
        R.raw.license_ofl,
        R.string.license_google_sans_flex_link
    );
    textDialogUtilGoogleSansFlex.showIfWasShown(savedInstanceState);

    ViewUtil.setOnClickListeners(
        this,
        binding.linearAboutChangelog,
        binding.linearAboutVending,
        binding.linearAboutKey,
        binding.linearAboutGithub,
        binding.linearAboutTranslation,
        binding.linearAboutPrivacy,
        binding.linearAboutLicenseMaterialComponents,
        binding.linearAboutLicenseMaterialIcons,
        binding.linearAboutLicenseGoogleSansFlex
    );
  }

  @Override
  public void onResume() {
    super.onResume();
    updateUnlockItem();
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    if (textDialogUtilMdc != null) {
      textDialogUtilMdc.saveState(outState);
    }
    if (textDialogUtilMds != null) {
      textDialogUtilMds.saveState(outState);
    }
    if (textDialogUtilGoogleSansFlex != null) {
      textDialogUtilGoogleSansFlex.saveState(outState);
    }
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (getViewUtil().isClickDisabled(id)) {
      return;
    } else {
      performHapticClick();
    }

    if (id == R.id.linear_about_changelog) {
      ViewUtil.startIcon(binding.imageAboutChangelog);
      activity.showChangelogDialog();
    } else if (id == R.id.linear_about_vending) {
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_vending_dev))));
    } else if (id == R.id.linear_about_key) {
      if (UnlockUtil.isKeyInstalled(activity)) {
        UnlockUtil.openPlayStore(activity);
      } else {
        activity.showUnlockDialog();
      }
    } else if (id == R.id.linear_about_github) {
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_github))));
    } else if (id == R.id.linear_about_translation) {
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_translate))));
    } else if (id == R.id.linear_about_privacy) {
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_privacy))));
    } else if (id == R.id.linear_about_license_google_sans_flex) {
      ViewUtil.startIcon(binding.imageAboutLicenseGoogleSansFlex);
      textDialogUtilGoogleSansFlex.show();
    } else if (id == R.id.linear_about_license_material_components) {
      ViewUtil.startIcon(binding.imageAboutLicenseMaterialComponents);
      textDialogUtilMdc.show();
    } else if (id == R.id.linear_about_license_material_icons) {
      ViewUtil.startIcon(binding.imageAboutLicenseMaterialIcons);
      textDialogUtilMds.show();
    }
  }

  private void updateUnlockItem() {
    binding.linearAboutKey.setVisibility(View.GONE);
  }

  private void showPasswordDialog() {
    TextInputLayout inputLayout = new TextInputLayout(
        activity, null, com.google.android.material.R.attr.textInputOutlinedStyle
    );
    inputLayout.setHint(getString(R.string.generator_password_hint));
    int padding = (int) (24 * getResources().getDisplayMetrics().density);
    inputLayout.setPadding(padding, padding / 2, padding, 0);
    TextInputEditText editText = new TextInputEditText(inputLayout.getContext());
    editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER
        | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
    editText.setMaxLines(1);
    inputLayout.addView(editText);

    new MaterialAlertDialogBuilder(activity)
        .setTitle(R.string.generator_password_title)
        .setView(inputLayout)
        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
          String password = editText.getText() != null
              ? editText.getText().toString() : "";
          if (password.equals(ActivationUtil.MASTER_PASSWORD)) {
            showGeneratorDialog();
          } else {
            Toast.makeText(activity, R.string.generator_password_wrong, Toast.LENGTH_SHORT).show();
          }
        })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
  }

  private void showGeneratorDialog() {
    TextInputLayout inputLayout = new TextInputLayout(
        activity, null, com.google.android.material.R.attr.textInputOutlinedStyle
    );
    inputLayout.setHint(getString(R.string.generator_count_hint));
    int padding = (int) (24 * getResources().getDisplayMetrics().density);
    inputLayout.setPadding(padding, padding / 2, padding, 0);
    TextInputEditText editText = new TextInputEditText(inputLayout.getContext());
    editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
    editText.setText("10");
    editText.setMaxLines(1);
    inputLayout.addView(editText);

    new MaterialAlertDialogBuilder(activity)
        .setTitle(R.string.generator_title)
        .setView(inputLayout)
        .setPositiveButton(R.string.generator_button, (dialog, which) -> {
          String countStr = editText.getText() != null
              ? editText.getText().toString() : "10";
          int count;
          try {
            count = Integer.parseInt(countStr);
            if (count <= 0) count = 10;
            if (count > 100) count = 100;
          } catch (NumberFormatException e) {
            count = 10;
          }
          List<String> codes = ActivationUtil.generateCodes(count);
          StringBuilder sb = new StringBuilder();
          for (String code : codes) {
            sb.append(code).append("\n");
          }
          String result = sb.toString().trim();
          showCodesResultDialog(result);
        })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
  }

  private void showCodesResultDialog(String codes) {
    new MaterialAlertDialogBuilder(activity)
        .setTitle(R.string.generator_title)
        .setMessage(codes)
        .setPositiveButton(R.string.generator_copied, (dialog, which) -> {
          ClipboardManager clipboard = (ClipboardManager)
              activity.getSystemService(Context.CLIPBOARD_SERVICE);
          ClipData clip = ClipData.newPlainText("activation_codes", codes);
          clipboard.setPrimaryClip(clip);
          Toast.makeText(activity, R.string.generator_copied, Toast.LENGTH_SHORT).show();
        })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
  }
}