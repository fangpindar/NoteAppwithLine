from flask import Flask, request, abort
import time
from linebot import (
    LineBotApi, WebhookHandler
)
from linebot.exceptions import (
    InvalidSignatureError
)
from linebot.models import (
    MessageEvent, TextMessage, TextSendMessage, ImageSendMessage
)
import dbModel
app = Flask(__name__)

# Channel Access Token
line_bot_api = LineBotApi('qYm6yF6paHWlzG7k30FORqr6C8WTjmfjxPGntpOTnuD85vYgxL3nCiWd3Iu5wv/kPLYyrdLdQfk2iBflXXx0RPF93A335AhB23kk1L/USssgUGcAgwHf4DIyaQ6Hqq8x0MKzgMG6ILBeDB6yl+ck+AdB04t89/1O/w1cDnyilFU=')
# Channel Secret
handler = WebhookHandler('d385a11617742b5d7d746aac0c3a18f0')

# 監聽所有來自 /callback 的 Post Request
@app.route("/callback", methods=['POST'])
def callback():
    # get X-Line-Signature header value
    signature = request.headers['X-Line-Signature']

    # get request body as text
    body = request.get_data(as_text=True)
    app.logger.info("Request body: " + body)

    # handle webhook body
    try:
        handler.handle(body, signature)
    except InvalidSignatureError:
        abort(400)

    return 'OK'

@handler.add(MessageEvent, message=TextMessage)
def handle_message(event):
    if event.message.text.lower()=="db" :
        userid=event.source.user_id
        reply=dbModel.download(userid)
        message = TextSendMessage(reply)
    elif event.message.text.lower()=="id" :
        reply=event.source.user_id
        message= TextSendMessage(reply)
    elif event.message.text.lower()=="log" :
        reply=dbModel.download("ALL")
        message = TextSendMessage(reply)
    elif "2018"in event.message.text:
        dbModel.update(event.source.user_id,event.message.text)
        message= TextSendMessage(event.message.text)
    else:
        reply="Wrong Command"
        message= TextSendMessage(reply)
    line_bot_api.reply_message(event.reply_token,message)
    
def thread_job():
    while 1:
        if time.strftime("%H",time.localtime())=='09':
            check=time.strftime("%Y-%m-%d",time.localtime())
            alarm=dbModel.alarm(check)
            for i in alarm:
                message=TextSendMessage(i[1])
                line_bot_api.push_message(i[0],message)
        time.sleep(3600)
import os
import threading

if __name__ == "__main__":
    thread = threading.Thread(target=thread_job,)
    thread.start()
    port = int(os.environ.get('PORT', 5000))
    app.run(host='0.0.0.0', port=port)

