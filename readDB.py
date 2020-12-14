import pprint
import datetime
import sys
import os
from sqlalchemy.ext.automap import automap_base
from sqlalchemy.orm import Session
from sqlalchemy import create_engine
from math import sin, cos, sqrt, atan2, radians

coordinates = os.path.dirname(os.path.abspath(__file__))
Users = []

class Person:

    def __init__(self, userID, fromDate, toDate, infectedUser = False):
        self.userID = userID
        self.fromDate = fromDate
        self.toDate = toDate
        self.infectedUser = infectedUser
        self.Base = automap_base()
        self.fetchUser()
        self.timeComb = self.calcTimeComb()
        self.contacted_ids = []

    def fetchUser(self):
        sqlite_path = "sqlite:///" + coordinates + "/LifeMap_GS" + str(self.userID) + ".db"
        self.engine = create_engine(sqlite_path)
        self.session = Session(self.engine)
        self.Base.prepare(self.engine, reflect=True)
        self.apTable = self.Base.classes.apTable
        self.batteryTable = self.Base.classes.batteryTable
        self.categorySetTable = self.Base.classes.categoryTable
        self.cellEdgeTable = self.Base.classes.cellEdgeTable
        self.cellNodeTable = self.Base.classes.cellNodeTable
        self.cellTable = self.Base.classes.cellTable
        self.configureTable = self.Base.classes.configureTable
        self.edgeTable = self.Base.classes.edgeTable
        self.locationTable = self.Base.classes.locationTable
        self.noRadioTable = self.Base.classes.noRadioTable
        self.sensorUsageTable = self.Base.classes.sensorUsageTable
        self.stayCommentTable = self.Base.classes.stayCommentTable
        self.stayTable = self.Base.classes.stayTable

    def dateFormat(self, date):
        return datetime.datetime.strptime(date[:-3], '%Y%m%d%H%M%S')
    
    def calcTimeComb(self):
        timeComb = []
        times = self.session.query(self.stayTable).all()
        times = times if times else []
        for stay in sorted(times, key=lambda x: self.dateFormat(x._stay_start_time)):
            stay_start_time = self.dateFormat(stay._stay_start_time)
            if self.fromDate <= stay_start_time and stay_start_time < self.toDate:
                timeComb.append(stay)
        return timeComb
    
    def kmRange(self, startLat, startLon, toLat, toLon, kms = 5):
        #Multiplying coordinate values into Earth's radius
        R = 6373.0
        lat1 = radians(startLat)
        lon1 = radians(startLon)
        lat2 = radians(toLat)
        lon2 = radians(toLon)

        lonDiff = lon2 - lon1
        latDiff = lat2 - lat1

        a = sin(latDiff/2)**2 + cos(lat1) * cos(lat2) * sin(lonDiff/2)**2
        c = 2*atan2(sqrt(a), sqrt(1-a))

        distance = R * c
        return distance < kms

    
    def contactedWith(self, person, timeCombinations):
        if person.userID in self.contacted_ids:
            return True
        for timeC in timeCombinations:
            for stay_time in person.timeComb:
                start1 = self.dateFormat(timeC._stay_start_time) - datetime.timedelta(minutes = 30)
                end1 = self.dateFormat(timeC._time_stay) + datetime.timedelta(minutes = 30)

                start2 = self.dateFormat(stay_time._stay_start_time)
                end2 = self.dateFormat(stay_time._time_stay)

                lastStart = max(start1, start2)
                firstEnd = min(end1, end2)
                delta = (firstEnd - lastStart)
                if delta.total_seconds() > 0:
                    targetPos = self.session.query(self.locationTable).filter(self.locationTable._node_id == timeC._node_id).first()
                    visPos = person.session.query(person.locationTable).filter(person.locationTable._node_id == stay_time._node_id).first()
                    startLat, startLon = targetPos._latitude / 10 ** 6, targetPos._longitude / 10 ** 6
                    toLat, toLon = visPos._latitude / 10 ** 6, visPos._longitude / 10 ** 6
                    if self.kmRange(startLat, startLon, toLat, toLon, kms = 5):
                        person.contacted_ids.append(self.userID)
                        self.contacted_ids.append(person.userID)

                        start_idx = [self.dateFormat(x._stay_start_time) for x in person.timeComb].index(start2)
                        for new_target in [x for x in Users if x.userID not in person.contacted_ids and x.userID != person.userID]:
                            person.contactedWith(new_target, person.timeComb[start_idx:])
                        return

if __name__ == '__main__':
    userID = int(sys.argv[1])
    date = datetime.datetime.strptime(sys.argv[2], '%m/%d/%Y')
    fromDate = date + datetime.timedelta(days =- 7) 
    toDate = date 
    target = Person(userID, fromDate, toDate, infectedUser = True)
    Users = [Person(x, fromDate, toDate) for x in range(1,12) if x != userID]

    for person in Users:
        target.contactedWith(person, target.timeComb)

    Users.append(target)

    adjGraph = [[0]*len(Users) for x in range(len(Users))]
    for person in Users:
        for userdbID in range(1, 12):
            adjGraph[person.userID - 1][userdbID - 1] =  1 if userdbID in person.contacted_ids else 0
        adjGraph[person.userID - 1][person.userID - 1] =  1 if len(person.contacted_ids) > 0 else 0

    pprint.pprint(adjGraph)