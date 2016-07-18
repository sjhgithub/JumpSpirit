package cn.mailchat.controller;

import cn.mailchat.mail.Message;

public interface MessageRemovalListener {
    public void messageRemoved(Message message);
}
