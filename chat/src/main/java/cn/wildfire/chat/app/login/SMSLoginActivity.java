package cn.wildfire.chat.app.login;

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

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import cn.wildfire.chat.app.Config;
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
        setStatusBarFullTransparent();
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

        String url = "http://" + Config.APP_SERVER_HOST + ":" + Config.APP_SERVER_PORT + "/send_code";
        Map<String, String> params = new HashMap<>();
        params.put("mobile", phoneNumberEditText.getText().toString());
        OKHttpHelper.post(url, params, new SimpleCallback<StatusResult>() {
            @Override
            public void onUiSuccess(StatusResult statusResult) {
                if (statusResult.getCode() == 0) {
                    Toast.makeText(SMSLoginActivity.this, "发送验证码成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SMSLoginActivity.this, "发送验证码失败: " + statusResult.getCode(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onUiFailure(int code, String msg) {
                Toast.makeText(SMSLoginActivity.this, "发送验证码失败: " + msg, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @OnClick(R.id.btn_login)
    void login() {
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
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
