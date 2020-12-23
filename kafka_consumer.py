#!/usr/bin/env python3.8
from kafka import KafkaConsumer
from sys import stderr
from json import loads

TOPIC = 'counts'
RULE = '#COVID19'
EXPANSIONS = 'author_id,geo.place_id'
PLACE_FIELDS = 'contained_within,country,country_code,full_name,geo,id,name,place_type'
USER_FIELDS = 'location'

kafka = KafkaConsumer(
    TOPIC,
    bootstrap_servers=['localhost:9092'],
    #value_deserializer=lambda x: loads(x)
)

for msg in kafka:
    print (msg)