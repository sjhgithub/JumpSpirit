package cn.mailchat.chatting.protocol;

import cn.mailchat.Account;
import cn.mailchat.chatting.beans.CMessage;

public interface SuccessfulFailureStateCallBack {
	public void ConnectSuccess(MQTTCommand command);

	public void ConnectFail(MQTTCommand command);

	public void DisConnectSuccess();

	public void DisConnectFail();

	public void SubscribeSuccess(Account account, MQTTCommand command,Object data);

	public void SubscribeFail(Account account, MQTTCommand command,Object data);

	public void PublishSuccess(Account account, MQTTCommand command,Object data);

	public void PublishFail(Account account, MQTTCommand command,Object data);

	public void unSubscribeSuccess(Account account, MQTTCommand command,Object data);

	public void unSubscribeFail(Account account, MQTTCommand command,Object data);

	public void PublishSuccessByTime(Account account, MQTTCommand command,
			Object data, long time);
}
