package cn.mailchat.view;

import cn.mailchat.MailChat;
import cn.mailchat.R;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class GenericDialog extends Dialog {

    public GenericDialog(Context context) {
        super(context, R.style.dialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_generic);

        TextView title = (TextView) findViewById(R.id.title);
        title.setText(R.string.login_forgot_password_title);

        TextView content = (TextView) findViewById(R.id.content);
        content.setText(Html.fromHtml(MailChat.app.getString(R.string.login_forgot_password_content)));

        Button button = (Button) findViewById(R.id.button);
        button.setText(R.string.login_forgot_password_button);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                GenericDialog.this.cancel();
            }

        });
    }

}
