#!/usr/bin/env bash

# pip3 install --user searchtwitter-v2
# cred-file is in ~/.twitter_keys.yaml
# Documentation:
# https://github.com/twitterdev/search-tweets-python/tree/v2
# https://developer.twitter.com/en/docs/twitter-api/data-dictionary/object-model/place

search_tweets.py \
	--query "#COVID19" \
	--max-tweets 10 \
  --tweet-fields "geo,lang,source" \
	--place-fields "geo,country,place_type" \
	--user-fields "location" \
	--expansions "geo.place_id,author_id" 
