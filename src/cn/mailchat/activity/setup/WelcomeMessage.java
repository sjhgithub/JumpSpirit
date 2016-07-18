package cn.mailchat.activity.setup;

import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import cn.mailchat.R;
import cn.mailchat.activity.Accounts;
import cn.mailchat.activity.MailChatActivity;
import cn.mailchat.helper.HtmlConverter;

/**
 * Displays a welcome message when no accounts have been created yet.
 */
public class WelcomeMessage extends MailChatActivity implements OnClickListener{

    public static void showWelcomeMessage(Context context) {
        Intent intent = new Intent(context, WelcomeMessage.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.welcome_message);

        TextView welcome = (TextView) findViewById(R.id.welcome_message);
        welcome.setText(HtmlConverter.htmlToSpanned(getString(R.string.accounts_welcome)));
        welcome.setMovementMethod(LinkMovementMethod.getInstance());

        ((Button) findViewById(R.id.next)).setOnClickListener(this);
        ((Button) findViewById(R.id.import_settings)).setOnClickListener(this);
        MobclickAgent.updateOnlineConfig( this );
        /** 设置是否对日志信息进行加密, 默认false(不加密). */
        AnalyticsConfig.enableEncrypt(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.next: {
                AccountSetupBasics.actionNewAccount(this);
                finish();
                break;
            }
            case R.id.import_settings: {
                Accounts.importSettings(this);
                finish();
                break;
            }
        }
    }
}
