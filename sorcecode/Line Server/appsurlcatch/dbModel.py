import pymongo

from pymongo import MongoClient
uri ="mongodb+srv://db:ppap@appsdb-gro3c.mongodb.net/test?retryWrites=true" 
client = MongoClient(uri)

def update(usrid,date):
    post={"usrid":usrid,"date":date}
    db=client.connection.test
    posts=db.posts
    posts.insert(post)
