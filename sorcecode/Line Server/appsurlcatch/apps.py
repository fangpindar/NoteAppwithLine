from flask import Flask, request
import dbModel 
import os

app = Flask(__name__)

@app.route('/')
def hello_world():
    usrid = request.args.get('usrid')
    date=request.args.get('date')
    if date!="" and usrid!="":
        temp=''
        temp="Your Line ID is:"+usrid+"\nYou append an event:"+date
        dbModel.update(usrid,date)
        return temp
    else:
        return "Error"
if __name__ == '__main__':
    app.run(port=os.getenv("PORT"))

