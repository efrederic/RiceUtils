from flask import Flask, request
from lxml import etree
app = Flask(__name__)

# because we're bad people, we don't use a database
posts = []

# maximum number of posts to store
NUMPOSTS = 60

@app.route("/")
def hello():
    return "Hi! You look human! We only expected computers to come here..."

@app.route("/addpost", methods=["POST"])
def addpost():
    text = request.form.get("text", None)
    latitude = request.form.get("latitude", None)
    longitude = request.form.get("longitude", None)
    if text and latitude and longitude:
        posts.append((text, latitude, longitude))
        if len(posts) > NUMPOSTS:
            posts.pop(0)
        return "Success!"
    return "Failure..."

@app.route("/getposts")
def getposts():
    root = etree.Element('root')
    for post in posts:
        xpost = etree.Element('post')
        xtext = etree.Element('text')
        xtext.text = post[0]
        xpost.append(xtext)
        xlatitude = etree.Element('latitude')
        xlatitude.text = post[1]
        xpost.append(xlatitude)
        xlongitude = etree.Element('longitude')
        xlongitude.text = post[2]
        xpost.append(xlongitude)
        root.append(xpost)
    xmlstr = etree.tostring(root)
    return xmlstr

@app.errorhandler(404)
def page_not_found(e):
    """Return a custom 404 error."""
    return "Sorry, Nothing at this URL.", 404

@app.errorhandler(500)
def application_error(e):
    """Return a custom 500 error."""
    return "Sorry, unexpected error: {}".format(e), 500
