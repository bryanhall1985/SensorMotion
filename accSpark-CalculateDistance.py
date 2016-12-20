#!/usr/bin/env python
"""
Copyright Google Inc. 2016
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""
from __future__ import print_function

import sys
import itertools
from math import sqrt
from operator import add
from os.path import join, isfile, dirname
from pyspark import SparkContext, SparkConf, SQLContext
from pyspark.mllib.recommendation import ALS, MatrixFactorizationModel, Rating
from pyspark.sql.types import StructType
from pyspark.sql.types import StructField
from pyspark.sql.types import StringType
from pyspark.sql.types import FloatType

from datetime import datetime, timedelta, tzinfo
from pyspark.sql import functions as F
from pyspark.sql import Row
from pyspark.sql.window import Window
from pyspark.sql.functions import from_unixtime, unix_timestamp
from pyspark.sql.types import TimestampType
import calendar
import time


conf = SparkConf().setAppName("acc_spark")
sc = SparkContext(conf=conf)
sqlContext = SQLContext(sc)



USER_ID = 0
#CLOUDSQL_INSTANCE_IP = 173.194.251.148
#BEST_RANK = 20
#BEST_ITERATION = 10
#BEST_REGULATION = 0.1

#CLOUDSQL_INSTANCE_IP = sys.argv[1]
#CLOUDSQL_DB_NAME = sys.argv[2]
#CLOUDSQL_USER = sys.argv[3]
#CLOUDSQL_PWD  = sys.argv[4]


CLOUDSQL_INSTANCE_IP = "130.211.131.99"
CLOUDSQL_DB_NAME = "accsparkDB"
CLOUDSQL_USER = "root"
CLOUDSQL_PWD  = "Anfrag139"


#BEST_RANK = int(sys.argv[5])
#BEST_ITERATION = int(sys.argv[6])
#BEST_REGULATION = float(sys.argv[7])

#TABLE_ITEMS  = "Accommodation"
#TABLE_RATINGS = "Rating"
#TABLE_RECOMMENDATIONS = "Recommendation"

ACC_TABLE = "accTable" 
ACC_RESULT = "accResult"

# Read the data from the Cloud SQL
# Create dataframes
#[START read_from_sql]
jdbcDriver = 'com.mysql.jdbc.Driver'
jdbcUrl    = 'jdbc:mysql://%s:3306/%s?user=%s&password=%s' % (CLOUDSQL_INSTANCE_IP, CLOUDSQL_DB_NAME, CLOUDSQL_USER, CLOUDSQL_PWD)

#jdbcUrlWrite= 'jdbc:mysql://%s:3306/%s?user=%s&password=%s' % (CLOUDSQL_INSTANCE_IP, CLOUDSQL_DB_NAME, CLOUDSQL_USER, CLOUDSQL_PWD)


#dfAccos = sqlContext.load(source='jdbc', driver=jdbcDriver, url=jdbcUrl, dbtable=ACC_TABLE)
#dfRates = sqlContext.load(source='jdbc', driver=jdbcDriver, url=jdbcUrl, dbtable=TABLE_RATINGS)
#[END read_from_sql]

dataframe_mysql = sqlContext.read.format("jdbc").options(
    url=jdbcUrl,
    driver = jdbcDriver,
    dbtable = ACC_TABLE,
    user=CLOUDSQL_USER,
    password=CLOUDSQL_PWD).load()

dataframe_mysql.printSchema()
#dataframe_mysql.registerTempTable("temp")

#change dataframe columns types
tempDF = dataframe_mysql.withColumn("accX", dataframe_mysql["accX"].cast("float"))
tempDF = tempDF.withColumn("accY", tempDF["accY"].cast("float"))
tempDF = tempDF.withColumn("accZ", tempDF["accZ"].cast("float"))
tempDF = tempDF.withColumn("timestamp", tempDF["timestamp"].cast("long"))

tempDF.show()

tempDF.printSchema()

#getting current day and tomorrow
i = datetime.now()

dayToday = i.day
monthToday = i.month
yearToday = i.year
hourToday = i.hour
minuteToday = i.minute
secondToday = i.second
millisecondToday = (i.microsecond)*1000

print("day Today %s" %dayToday)
print("month Today %s" %monthToday)
print("year Today %s" %yearToday)
print("hour today %s" %hourToday)
print("minute today %s" %minuteToday)
print("second today %s" %secondToday)
print("millisecond Today %s" %millisecondToday)





#w = Window.partitionBy("user").rangeBetween(0,3)

#res = tempDF.orderBy(tempDF.timestamp).select(F.sum(tempDF.accX).over(w).alias('accX'))

#create a new DF with result from acceleration
tempDF.createOrReplaceTempView("temporary")
accFrame = sqlContext.sql("SELECT visit_id, user, accX, accY, accZ, timestamp, SQRT(accX*accX+accY*accY)*(0.4*0.4) AS instantAccXY, SQRT(accZ*accZ)*(0.4*0.4) AS instantAccZ FROM temporary")

#timestamp since epoch in seconds
timestampNow = long(time.time())
print("time NOW = %s" %timestampNow)

#select time from 2 hours ago
ts = (timestampNow-7200)*1000
print("ts = %s" %ts)

dfFinal = accFrame.filter(accFrame.instantAccXY>0.5)
dff = dfFinal.filter(dfFinal.timestamp>ts)
dff.show()

tempWalk = dff.groupBy().sum('instantAccXY').collect()
print("meters walked %s" %tempWalk)




#res = tempDF.select([
#  F.lit('All').alias('user'),
#  F.sum(dff.instantAccXY).alias('distance XY'),
#  F.sum(dff.instantAccZ).alias('distance Z'),
#  F.lit('All').alias('final')
#  ])
#res.show()


dfFinal = accFrame.filter(accFrame.instantAccZ>0.5)
dff = dfFinal.filter(dfFinal.timestamp>ts)
dff.show()

tempJump = dff.groupBy().sum('instantAccZ').collect()
print("meters Jumped %s" %tempJump)



#sum columns and show value
#res = tempDF.select([
#  F.lit('All').alias('user'),
#  F.sum(dff.instantAccZ).alias('distance Z'),
#  F.lit('All').alias('final')
#  ])
#res.show()

#row = Row(visit_id = "", user = "Bryan", accXY = tempWalk, accZ = tempJump, timeStart = ts, timeEnd = timestampNow)

#row = [{'visit_id':'', 'user':'Bryan','accXY':tempWalk,'accZ':tempJump,'timeStart':ts,'timeEnd':timestampNow}]

#Row('','Bryan', tempWalk, tempJump, ts, timestampNow)

#schema = StructType([
#    StructField("visit_id", StringType(), True),
#    StructField("user", StringType(), True),
#    StructField("accXY", StringType(), True),
#    StructField("accZ", StringType(), True),
#    StructField("timeStart", StringType(), True),
#    StructField("timeEnd", StringType(), True)
#])#



#finalRow = sqlContext.createDataFrame(row).collect()

#finalRow.show()

#write data to mysql server
#df.write.jdbc(url=url, table="baz", mode=mode, properties=properties)


#sele = dataframe_mysql.filter(dataframe_mysql.accX > 0)
#sele.take(100)
#sele.show()

print("last entered line")

#timestamp = dt.replace(tzinfo=timezone.utc).timestamp()






#final = dataframe_mysql.withColumn(F.sum(CAST('accX' AS DOUBLE)) AS 'smooth')
#final.show()

#wr = Window.rowsBetween(-2,2)
#wr



#rangeDF = dataframe_mysql.range(-2,2,1).collect()
#rangeDF

#windowSpec = Window.rangeBetween(-2,2)
#windowSpec



#tempDF.groupBy("user").agg(func.col("user"), func.sum("accX")),show()





#day19 = calculate day 19/12/2016 in miliseconds, counting from january 1st 1970
#used for filtering day activities

# Applying day filter
#df2 = tempDF.filter(tempDF["timestamp"]>day19Init)
#df2 = df2.filter(tempDF["timestamp"]<day19End)







#rows = sqlContext.sql("select 'user' AS user, 'accX' AS AccX, 'accY' AS AccY FROM temp limit 20").collect()
 
#dataframe_mysql.filter(dataframe_mysql['name']=='Bryan')

#for row in rows:
#  print("first 20 rows of accX = {0} and accY = {1}".format(row.AccX, row.AccY))

#print("this works")
#print("IP " + CLOUDSQL_INSTANCE_IP)
#print("Instance " + CLOUDSQL_DB_NAME)
