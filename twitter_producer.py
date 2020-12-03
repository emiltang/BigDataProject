#!/usr/bin/env python3.8
from os import getenv
# note SimpleProducer is depreecated
from kafka import KafkaProducer
from TwitterAPI import TwitterAPI, TwitterRequestError, TwitterConnectionError
from sys import stderr
from json import dumps

TOPIC = 'twitter'
RULE = '#COVID19'
EXPANSIONS = 'author_id,geo.place_id'
PLACE_FIELDS = 'contained_within,country,country_code,full_name,geo,id,name,place_type'
USER_FIELDS = 'location'

kafka = KafkaProducer(
    bootstrap_servers=['localhost:9092'],
    # serialize json to string
    value_serializer=lambda x: dumps(x).encode('utf-8')
)

api = TwitterAPI(
    # get credentials form environment
    consumer_key=getenv('CONSUMER_KEY'),
    consumer_secret=getenv('CONSUMER_SECRET'),
    # access_token_key=getenv('ACCES_TOKEN_KEY'),
    # access_token_secret=getenv('ACCES_TOKEN_SECRET'),
    auth_type='oAuth2',
    api_version='2'
)

if __name__ == '__main__':

    print('API version', api.version)
    rule = api.request('tweets/search/stream/rules', {
        'add': [{'value': RULE}]
    })
    print('Applying rule', rule.text)

    rule = api.request('tweets/search/stream/rules', method_override='GET')
    print('Current rules', rule.text)

    while True:
        try:
            print('Starting stream')
            stream = api.request('tweets/search/stream', {
                'expansions': EXPANSIONS,
                'user.fields': USER_FIELDS,
                'place.fields': PLACE_FIELDS
            })

            # handle unsuccesfull http error such as 4xx and 5xx
            if stream.status_code != 200:
                print(stream.status_code, stream.text, file=stderr)
                exit()

            for result in stream:
                
                if 'disconnect' in result:
                    event = result['disconnect']
                    if event['code'] in [2, 5, 6, 7]:
                        # something needs to be fixed before re-connecting
                        print(event['reason'], file=stderr)
                        exit()
                    else:
                        # temporary interruption, re-try request
                        break

                print('Stream result', result)
                kafka.send(TOPIC, result)

        except TwitterRequestError as e:
            if e.status_code < 500:
                # something needs to be fixed before re-connecting
                print("Error detected, shutting down", e, file=stderr)
                exit()
            else:
                # temporary interruption, re-try request
                print("Error detected, restarting connection", e, file=stderr)
                pass

        except TwitterConnectionError as e:
            # temporary interruption, re-try request
            print("Error detected, restarting connection", e, file=stderr)
            pass
