package cn.wildfire.chat.app.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import cn.wildfire.chat.app.Config;
import cn.wildfire.chat.app.login.model.LoginResult;
import cn.wildfire.chat.app.main.MainActivity;
import cn.wildfire.chat.app.utils.LogHelper;
import cn.wildfire.chat.kit.ChatManagerHolder;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.net.OKHttpHelper;
import cn.wildfire.chat.kit.net.SimpleCallback;
import cn.wildfire.chat.kit.net.base.StatusResult;
import cn.wildfirechat.chat.R;

public class SMSLoginActivity extends WfcBaseActivity {

    @Bind(R.id.phone_number_editText)
    EditText phoneNumberEditText;

    @Bind(R.id.verify_code_editText)
    EditText verifyCodeEditText;

    @Bind(R.id.get_verify_code)
    TextView getVerifyCode;

    @Bind(R.id.btn_login)
    Button loginButton;

    private Handler handler = new Handler();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setStatusBarFullTransparent();
    }

    //监听手机号的变化
    @OnTextChanged(value = R.id.phone_number_editText, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void inputPhoneNumber(Editable editable) {
        String phone = editable.toString().trim();
        if (phone.length() == 11) {

            loginButton.setClickable(true);
            getVerifyCode.setClickable(true);
        } else {

            loginButton.setClickable(false);
            getVerifyCode.setClickable(false);
        }
    }

    @OnClick(R.id.get_verify_code)
    void getVerifyCode() {

        getVerifyCode.setEnabled(false);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    getVerifyCode.setEnabled(true);
                }
            }
        }, 60 * 1000);

        Map<String, String> params = new HashMap<>();
        params.put("mobile", phoneNumberEditText.getText().toString());

        OKHttpHelper.post(Config.GET_VERIFY_CODE_URL, params, new SimpleCallback<StatusResult>() {
            @Override
            public void onUiSuccess(StatusResult statusResult) {
                if (statusResult.getCode() == ResultCode.SUCCESS) {

                    Toast.makeText(SMSLoginActivity.this, "发送验证码成功", Toast.LENGTH_SHORT).show();
                } else {

                    LogHelper.d("SMSLoginActivity:getVerifyCode|" + statusResult.getMessage());
                    Toast.makeText(SMSLoginActivity.this, "发送验证码失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onUiFailure(int code, String msg) {

                LogHelper.d("SMSLoginActivity:getVerifyCode|" + msg);
                Toast.makeText(SMSLoginActivity.this, "发送验证码失败", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @OnClick(R.id.btn_login)
    void login() {
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        String authCode = verifyCodeEditText.getText().toString().trim();

        Map<String, String> params = new HashMap<>();
        params.put("mobile", phoneNumber);
        params.put("code", authCode);
        try {
            params.put("clientId", ChatManagerHolder.gChatManager.getClientId());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(SMSLoginActivity.this, "网络出来问题了。。。", Toast.LENGTH_SHORT).show();
            return;
        }

        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .content("登录中...")
                .progress(true, 100)
                .cancelable(false)
                .build();
        dialog.show();
        OKHttpHelper.post(Config.SMS_LOGIN_URL, params, new SimpleCallback<LoginResult>() {
            @Override
            public void onUiSuccess(LoginResult loginResult) {
                if (isFinishing()) {
                    return;
                }
                dialog.dismiss();
                ChatManagerHolder.gChatManager.connect(loginResult.getUserId(), loginResult.getToken());
                SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
                sp.edit()
                        .putString("id", loginResult.getUserId())
                        .putString("token", loginResult.getToken())
                        .apply();
                Intent intent = new Intent(SMSLoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onUiFailure(int code, String msg) {
                if (isFinishing()) {
                    return;
                }
                Toast.makeText(SMSLoginActivity.this, "登录失败：" + code + " " + msg, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    /**
     * 设置状态栏透明同时设置虚拟按钮隐藏
     */
    protected void setStatusBarFullTransparent() {

        //5.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= 19) {//19表示4.4
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    protected boolean showHomeMenuItem() {
        return false;
    }

    @Override
    protected int contentLayout() {
        return R.layout.login_activity_sms;
    }
}
