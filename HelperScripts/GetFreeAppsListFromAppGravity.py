import os
import json
import subprocess
import sys
import tempfile
from bs4 import BeautifulSoup

def updateList(resultDict,targetHtmlFileName):
    #here we read the provided html file and update the dictionary
    targetData = None
    with open (targetHtmlFileName, "r") as myfile:
        targetData=myfile.read()
    if targetData:
        parsed_html = BeautifulSoup(targetData)
        for childApp in parsed_html.find_all("li", { "class" : "search-result free clearfix" }):
            targetCategory = childApp.find("span",{"class":"search-result-info-header-category"}).string
            packageName = (childApp.find_all("a", {"class":"lozenge-button s1 grey free download-link"})[0]).get("data-packagename")
            targetKey = targetCategory.lower()
            if not (targetKey in resultDict):
                resultDict[targetKey] = []
            targetList = resultDict[targetKey]
            if not (str(packageName) in targetList):
                targetList.append(str(packageName))

if len(sys.argv) != 3:
    print "This script scraps appgravity to get package names of free apps, it stores the results in the provided file in json format"
    print "Usage:"+sys.argv[0] +" <totalNumberofApps> <targetoutputJsonFile>"
    print "\n By m4kh1ry (@machiry_msidc)"
    sys.exit(-1)

#create the output parent directory, if it doesn't exist
if not os.path.exists(os.path.dirname(sys.argv[2])):
    os.makedirs(os.path.dirname(sys.argv[2]))

targetOutputJson = sys.argv[2]
resultDict = {}
urlPrefix = "http://m.appgravity.com/android-apps?open=1&p="
urlSuffix = "&price=free"
#Lets get 1000K apps
maxPageNo = (int(sys.argv[1])/10) + 1 # 1000000/10 (10 apps per page)
command_prefix = "wget --output-document "
tempFile = tempfile.mktemp()
currPageNo = 1
while currPageNo <= maxPageNo:
    targetUrl = urlPrefix + str(currPageNo) + urlSuffix
    targetFileName = tempFile
    currPageNo = currPageNo + 1
    toExecuteCommand = command_prefix + targetFileName + " \"" + targetUrl + "\""
    p = subprocess.Popen(toExecuteCommand, shell=True, stderr=subprocess.PIPE)
    output, err = p.communicate()
    print toExecuteCommand + ":" + str(output)
    updateList(resultDict, targetFileName)
with open(targetOutputJson, 'w') as f:
    json.dump(resultDict, f)
#print str(resultDict)


    