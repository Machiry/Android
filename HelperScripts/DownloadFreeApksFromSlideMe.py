import os
import json
import subprocess
from bs4 import BeautifulSoup
import sys

#site: http://slideme.org/applications?page=[1-1563]&solrsort=tfs_price%20asc

def runProcess(commandString):
    p = subprocess.Popen(commandString, shell=True, stderr=subprocess.PIPE)
    output, err = p.communicate()

def urlFetch(targetFile,targetUrl):
    runProcess("wget --output-document \"" + targetFile + "\" \""+targetUrl+"\"")

def downloadApps(targetAppDownloadLinks,targetDir):
    for category in targetAppDownloadLinks.keys():
        targetCategoryDir = targetDir + "/" + category
        if not os.path.exists(targetCategoryDir):
            os.makedirs(targetCategoryDir)
        for app in targetAppDownloadLinks[category].keys():
            targetFile = targetCategoryDir + "/" + app + ".apk"
            if not os.path.exists(targetFile):
                print "Downloading:"+targetAppDownloadLinks[category][app]+" to "+targetFile
                urlFetch(targetFile, targetAppDownloadLinks[category][app])
    

def slideme_getAllAppLinks(targetAppLinkDict,targetFileName,topUrl):
    targetData = None
    with open (targetFileName, "r") as myfile:
        targetData=myfile.read()
    if targetData:
        parsed_html = BeautifulSoup(targetData)
        for childApp in parsed_html.find_all("div", { "class" : "node node-mobileapp" }):
            urlLink = childApp.find("h2",{"class":"title"}).find("a").get("href")
            targetUrl = topUrl + urlLink
            if not (targetUrl in targetAppLinkDict):
                targetAppLinkDict.append(targetUrl)
        
def slideme_getAppDownloadLinks(targetAppDownloadLinks,targetAppLinkDict,tempWorkingDir):
    targetData = None
    targetFileName = tempWorkingDir + "/dummyApp.html"
    for tempUrl in targetAppLinkDict:
        urlFetch(targetFileName,tempUrl)
        with open (targetFileName, "r") as myfile:
            targetData=myfile.read()
        if targetData:
            parsed_html = BeautifulSoup(targetData)
            for childApp in parsed_html.find_all("div", { "class" : "node node-mobileapp node-page" }):
                try:
                    appName = childApp.find("h1",{"class":"title"}).get_text()
                    urlLink = childApp.find("div",{"class":"qrcode"}).find("a").get("href")
                    category = childApp.find("li",{"class":"category"}).find("a").string
                    targetKey = category.lower()
                    appName = str(appName).strip()
                    if not (targetKey in targetAppDownloadLinks):
                        targetAppDownloadLinks[targetKey] = {}
                    if not (appName in targetAppDownloadLinks[targetKey]):
                        targetAppDownloadLinks[targetKey][appName] = urlLink
                except:
                    pass

if len(sys.argv) != 3:
    print "This script scraps slideme.org to get free apps."
    print "Usage:"+sys.argv[0] +" <downloadDirectory> <tempWorkingDir>"
    print "\n By m4kh1ry (@machiry_msidc)"
    sys.exit(-1)

tempWorkingDir = sys.argv[2]
downloadDir = sys.argv[1]
if not os.path.exists(tempWorkingDir):
    os.makedirs(tempWorkingDir)
if not os.path.exists(downloadDir):
    os.makedirs(downloadDir)
maxPageNumber = 1563
urlPrefix = "http://slideme.org/applications?page="
urlSuffix = "&solrsort=tfs_price%20asc"
currPageNo = 1
while currPageNo <= maxPageNumber:   
    targetAppLinks = []
    targetAppDownloadLinks = {}
    targetUrl = urlPrefix + str(currPageNo) + urlSuffix
    targetFileName = tempWorkingDir + "/dummy.html"
    currPageNo = currPageNo + 1
    urlFetch(targetFileName, targetUrl)
    #print toExecuteCommand + ":" + str(output)
    slideme_getAllAppLinks(targetAppLinks, targetFileName,"http://slideme.org")
    slideme_getAppDownloadLinks(targetAppDownloadLinks,targetAppLinks,tempWorkingDir)
    downloadApps(targetAppDownloadLinks,downloadDir)
    