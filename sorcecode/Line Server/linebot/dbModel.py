import pymongo

from pymongo import MongoClient
uri ="mongodb+srv://db:ppap@appsdb-gro3c.mongodb.net/test?retryWrites=true" 
client = MongoClient(uri)

def download(userid):
    db=client.connection.test
    posts=db.posts
    reply=""
    if userid == "ALL":
        for i in posts.find():
            reply+=i["usrid"]+"\n"+i["date"]+"\n"
    else:
        for i in posts.find({"usrid":userid}):
            reply+=i["date"]+"\n"
    return reply

def update(usrid,date):
    post={"usrid":usrid,"date":date}
    db=client.connection.test
    posts=db.posts
    posts.insert(post)
def alarm(check):
    db=client.connection.test
    posts=db.posts
    reply=[]
    for i in posts.find():
        if check in i["date"]:
            usrid=i["usrid"]
            date=i["date"]
            reply.append([usrid,date])
    return reply
